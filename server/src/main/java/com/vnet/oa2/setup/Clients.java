package com.vnet.oa2.setup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.ClientAlreadyExistsException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientRegistrationService;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

/**
 * Created by olivier
 */
public class Clients {

    public enum Role {
        CLIENT("CLIENT"), ADMIN_APP("ADMIN_APP");
        private String value;
        Role(String value) {this.value = value;}
        public String getPrefixedValue() {return "ROLE_" + value;}
    }

    static final private Log logger = LogFactory.getLog(Clients.class);

    static class InMemoryClient extends BaseClientDetails {
        @JsonProperty("client_number")
        @com.fasterxml.jackson.annotation.JsonProperty("client_number")
        private Integer clientNumber;

        @JsonIgnore
        @com.fasterxml.jackson.annotation.JsonIgnore
        public Integer getClientNumber() {return clientNumber;}

        public void setClientNumber(Integer clientNumber) {this.clientNumber = clientNumber;}
    }

    static private final Map<String, ClientDetails> clients = createClients();

    static public Map<String, ClientDetails> getClients() {return clients;}

    /**
     * 11 Clients : client0 to client10
     * client0 is reserved and represents the authorization server admin UI.
     * @return Client Map
     */
     static private Map<String, ClientDetails> createClients() {
        logger.info("Create Clients");
        final Map<String, ClientDetails> map = new HashMap<>();
        for (int i=0; i<=10; i++) {
            final Clients.InMemoryClient client = new InMemoryClient();
            client.setClientNumber(i);
            client.setClientId("client" + i);
            client.setClientSecret("P@55w0rd" + i);
            client.setAuthorizedGrantTypes(Arrays.asList("authorization_code", "refresh_token", "password"));
            client.setResourceIds(Arrays.asList(Resources.getIdentifiers()));
            final Collection<SimpleGrantedAuthority> authorities = new LinkedList<>();
            if (i==0) {
                authorities.add(new SimpleGrantedAuthority(Role.ADMIN_APP.getPrefixedValue()));
                client.setScope(Scopes.getAdminScopes());
            } else {
                authorities.add(new SimpleGrantedAuthority(Role.CLIENT.getPrefixedValue()));
                client.setScope(Scopes.getClientScopes());
            }
            client.setAuthorities(authorities);
            map.put(client.getClientId(), client);
            logger.info(client.getClientId());
        }
        return map;
    }

    static public String getAdminClientBasicAuthorization() {
        // "Basic Y2xpZW50MDpQQDU1dzByZDA="
        final String clientId = "client0";
        final ClientDetails client = clients.get(clientId);
        return "Basic " + Base64.getEncoder().encodeToString((clientId+":"+client.getClientSecret()).getBytes());
    }

    // ---------------------------------
    // Client Registration Service
    // ---------------------------------
    @Service
    static public class RegistrationService implements ClientRegistrationService {

        private Map<String, ClientDetails> clients = Clients.getClients();

        public RegistrationService() {}

        @Override
        public void addClientDetails(ClientDetails clientDetails) throws ClientAlreadyExistsException {
            if (clients.get(clientDetails.getClientId())!=null)
                throw new ClientAlreadyExistsException(clientDetails.getClientId());

            clients.put(clientDetails.getClientId(), clientDetails);
        }

        @Override
        public void updateClientDetails(ClientDetails clientDetails) throws NoSuchClientException {
            if (clients.get(clientDetails.getClientId())==null)
                throw new NoSuchClientException(clientDetails.getClientId());

            clients.put(clientDetails.getClientId(), clientDetails);
        }

        @Override
        public void updateClientSecret(String clientId, String secret) throws NoSuchClientException {
            final ClientDetails client = clients.get(clientId);
            if (client==null)
                throw new NoSuchClientException(clientId);

            if (client instanceof BaseClientDetails) {
                ((BaseClientDetails) client).setClientSecret(secret);
            } else {
                throw new NoSuchClientException("Unsupported Client Type " + client.getClass().getName());
            }
        }

        @Override
        public void removeClientDetails(String clientId) throws NoSuchClientException {
            if (clients.get(clientId)==null)
                throw new NoSuchClientException(clientId);

            clients.remove(clientId);
        }

        public List<ClientDetails> listClientDetails() {
            return new ArrayList<>(clients.values());
        }
    }

}
