package com.vnet.oa2.delegate;

import com.vnet.oa2.setup.Users;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientRegistrationService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.approval.Approval;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by olivier on 7/26/16.
 */
@Component
public class Delegate {
    final private Log logger = LogFactory.getLog(getClass());

    @Autowired
    private ClientRegistrationService clientRegistrationService;

    @Autowired
    private ApprovalStore approvalStore;

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private Users users;

    /**
     * Get Users
     * @return all users
     */
    public Collection<UserDetails> getUsers() {
        return users.listUsers();
    }

    /**
     * Get Clients
     * @return all clients
     */
    public Collection<ClientDetails> getClients() {
        return clientRegistrationService.listClientDetails();
    }

    /**
     * Get user approvals
     *
     * @param username user name
     * @return approvals for one user across all registered clients
     */
    public Collection<Approval> getApprovals(String username) {
        final Collection<Approval> approvals = new LinkedList<>();
        final Collection<ClientDetails> clients = clientRegistrationService.listClientDetails();
        for (ClientDetails client : clients) {
            approvals.addAll(approvalStore.getApprovals(username, client.getClientId()).stream().collect(Collectors.toList()));
        }
        return approvals;
    }

    /**
     * Get user approvals
     *
     * @return all approvals (all users, all clients)
     */
    public Collection<Approval> getApprovals() {
        final Collection<Approval> approvals = new LinkedList<>();
        for (String username : users.names()) {
            approvals.addAll(getApprovals(username));
        }
        return approvals;
    }

    /**
     * Get user tokens
     *
     * @param username
     * @return user tokens
     */
    public Collection<OAuth2AccessToken> getTokens(String username) {
        final Collection<OAuth2AccessToken> out = new LinkedList<>();
        final Collection<ClientDetails> clients = clientRegistrationService.listClientDetails();
        for (ClientDetails client : clients) {
            final Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientIdAndUserName(client.getClientId(), username);
            for (OAuth2AccessToken token : tokens) {
                if (token instanceof DefaultOAuth2AccessToken) {
                    final DefaultOAuth2AccessToken t = (DefaultOAuth2AccessToken) token;
                    final Map<String, Object> info = new HashMap<>();
                    info.put("clientId", client.getClientId());
                    info.put("username", username);
                    t.setAdditionalInformation(info);
                }
                out.add(token);
            }
        }
        return out;
    }

    /**
     * Get all tokens
     *
     * @return
     */
    public Collection<OAuth2AccessToken> getTokens() {
        final Collection<OAuth2AccessToken> tokens = new LinkedList<>();
        for (String username : users.names()) {
            tokens.addAll(getTokens(username));
        }
        return tokens;
    }

    /**
     * Revoke consent
     * @param username
     * @param clientId
     * @param scope
     */
    public void revokeApprovals(String username, String clientId, String scope) {
        final Collection<Approval> approvals = approvalStore.getApprovals(username, clientId);
        final Collection<Approval> revoked = approvals.stream()
                .filter(approval -> approval.getScope().equals(scope))
                .collect(Collectors.toCollection(LinkedList::new));

        if (revoked.size() > 0) {
            approvalStore.revokeApprovals(revoked);
            logger.info(String.format("Revoked [%s,%s,%s]", clientId, username, scope));
        }
    }

    /**
     * Remove token from store
     * @param username user requesting the removal
     * @param tokenValue token value
     * @param checkUser true for regular users, false when request by admin
     * @throws Exception thrown when user (not admin) attempts to remove a token they do not 'own'.
     */
    public void removeToken(String username, String tokenValue, boolean checkUser) throws Exception {
        final OAuth2Authentication oAuth2Authentication = tokenStore.readAuthentication(tokenValue);

        if (checkUser && !username.equals(oAuth2Authentication.getName())) {
            final String m = username + " not allowed to revoke " + oAuth2Authentication.getName() + "'s tokens.";
            logger.error(m);
            throw new Exception(m);
        }

        final OAuth2AccessToken token = tokenStore.readAccessToken(tokenValue);
        logger.info("Removed Token " + tokenValue);
        tokenStore.removeAccessToken(token);
    }
}