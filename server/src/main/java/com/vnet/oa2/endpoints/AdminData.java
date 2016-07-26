package com.vnet.oa2.endpoints;

import com.vnet.oa2.delegate.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.approval.Approval;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
public class AdminData {

    @Autowired
    private Delegate delegate;

    @RequestMapping(value="/admin/users", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<UserDetails> getUsers() {
        return delegate.getUsers();
    }

    @RequestMapping(value="/admin/clients", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<ClientDetails> getClients() {
        return delegate.getClients();
    }

    @RequestMapping(value="/admin/approvals", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<Approval> getApprovals() {
        return delegate.getApprovals();
    }

    @RequestMapping(value="/admin/tokens", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<OAuth2AccessToken> getTokens() {
        return delegate.getTokens();
    }

    @RequestMapping(value="/admin/approvals", method = RequestMethod.DELETE)
    public void revoke(@RequestParam("user") String username,
                       @RequestParam("client") String clientId,
                       @RequestParam("scope") String scope) {
        delegate.revokeApprovals(username, clientId, scope);
    }

    @RequestMapping(value="/admin/tokens", method = RequestMethod.DELETE)
    public void removeToken(@RequestParam("user") String username,
                            @RequestParam("client") String clientId,
                            @RequestParam("token") String token) {
        delegate.removeTokens(username, clientId, token);
    }
}
