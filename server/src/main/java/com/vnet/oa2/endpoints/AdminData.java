package com.vnet.oa2.endpoints;

import com.vnet.oa2.setup.Users;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientRegistrationService;
import org.springframework.security.oauth2.provider.approval.Approval;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;


@RestController
public class AdminData {

    final private Log logger = LogFactory.getLog(getClass());

    @Autowired
    private ClientRegistrationService clientRegistrationService;

    @Autowired
    private ApprovalStore approvalStore;

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private Users users;

    @RequestMapping(value="/admin/users", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<UserDetails> getUsers() {
        return users.listUsers();
    }

    @RequestMapping(value="/admin/clients", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<ClientDetails> getClients() {
        return clientRegistrationService.listClientDetails();
    }

    @RequestMapping(value="/admin/approvals", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<Approval> getApprovals() {
        final Collection<Approval> out = new LinkedList<>();

        final Iterator<String> nameIterator = users.names().iterator();
        while (nameIterator.hasNext()) {
            final String user = nameIterator.next();
            final Collection<ClientDetails> clients = clientRegistrationService.listClientDetails();
            final Iterator<ClientDetails> clientIterator = clients.iterator();
            while (clientIterator.hasNext()) {
                final ClientDetails client = clientIterator.next();
                final Collection<Approval> approvals = approvalStore.getApprovals(user, client.getClientId());
                final Iterator<Approval> iterator = approvals.iterator();
                while (iterator.hasNext()) {
                    final Approval approval = iterator.next();
                    out.add(approval);
                }
            }
        }
        return out;
    }

    @RequestMapping(value="/admin/tokens", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<OAuth2AccessToken> getTokens() {
        final Collection<OAuth2AccessToken> out = new LinkedList<>();

        final Iterator<String> nameIterator = users.names().iterator();
        while (nameIterator.hasNext()) {
            final String user = nameIterator.next();
            final Collection<ClientDetails> clients = clientRegistrationService.listClientDetails();
            final Iterator<ClientDetails> clientIterator = clients.iterator();
            while (clientIterator.hasNext()) {
                final ClientDetails client = clientIterator.next();
                final Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientIdAndUserName(client.getClientId(), user);
                final Iterator<OAuth2AccessToken> iterator = tokens.iterator();
                while (iterator.hasNext()) {
                    final OAuth2AccessToken token = iterator.next();
                    if (token instanceof DefaultOAuth2AccessToken) {
                        final DefaultOAuth2AccessToken t = (DefaultOAuth2AccessToken)token;
                        final Map<String, Object> info = new HashMap<>();
                        info.put("clientId", client.getClientId());
                        info.put("username", user);
                        t.setAdditionalInformation(info);
                    }
                    out.add(token);
                }
            }
        }
        return out;
    }

    @RequestMapping(value="/admin/approvals", method = RequestMethod.DELETE)
    public void revoke(@RequestParam("user") String username,
                       @RequestParam("client") String clientId,
                       @RequestParam("scope") String scope) {
        final Collection<Approval> approvals = approvalStore.getApprovals(username, clientId);
        final Collection<Approval> revoked = new LinkedList<>();
        final Iterator<Approval> iterator = approvals.iterator();
        while (iterator.hasNext()) {
            final Approval approval = iterator.next();
            if (approval.getScope().equals(scope))
                revoked.add(approval);
        }

        if (revoked.size() > 0) {
            approvalStore.revokeApprovals(revoked);
            logger.info(String.format("Revoked [%s,%s,%s]", clientId, username, scope));
        }
    }

    @RequestMapping(value="/admin/tokens", method = RequestMethod.DELETE)
    public void removeToken(@RequestParam("user") String username,
                            @RequestParam("client") String clientId,
                            @RequestParam("token") String token) {
        final Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientIdAndUserName(clientId, username);
        final Iterator<OAuth2AccessToken> iterator = tokens.iterator();
        while (iterator.hasNext()) {
            final OAuth2AccessToken t = iterator.next();
            if (t.getValue().equals(token)) {
                tokenStore.removeAccessToken(t);
                logger.info("Removed Token " + t.getValue());
            }
        }
    }
}
