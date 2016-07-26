package com.vnet.oa2.config;

import com.vnet.oa2.setup.Users;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.oauth2.config.annotation.builders.ClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientRegistrationService;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.InMemoryApprovalStore;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

import java.util.Iterator;
import java.util.List;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    final private Log logger = LogFactory.getLog(getClass());

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    public ClientRegistrationService clientRegistrationService;

    @Bean
    public ApprovalStore getApprovalStore() {return new InMemoryApprovalStore();}

    @Bean
    public TokenStore getTokenStore() {return new InMemoryTokenStore();}

    @Bean
    public Users getAdditionalUsers() {return new Users();}

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        logger.info("Configure Authorization Server Endpoints");
        if (this.authenticationManager instanceof ProviderManager) {
            logger.info("Adding Provider:" + Users.class.getName());
            final ProviderManager manager = (ProviderManager) this.authenticationManager;
            manager.getProviders().add(getAdditionalUsers());
        }
        endpoints.authenticationManager(this.authenticationManager);
        endpoints.approvalStore(getApprovalStore());
        endpoints.tokenStore(getTokenStore());
    }


    @Override
    public void configure(ClientDetailsServiceConfigurer configurer) throws Exception {
        final List<ClientDetails> clients = clientRegistrationService.listClientDetails();
        final Iterator<ClientDetails> iterator = clients.iterator();

        // Just copy the clients hard coded in Clients
        // Clients.RegistrationService is auto-wired into clientRegistrationService
        ClientDetailsServiceBuilder builder = null;
        while (iterator.hasNext()) {
            final ClientDetails client = iterator.next();
            builder = (builder==null
                    ? configurer.inMemory().withClient(client.getClientId()).secret(client.getClientSecret())
                    : builder.withClient(client.getClientId()).secret(client.getClientSecret()))
                    .authorizedGrantTypes(client.getAuthorizedGrantTypes().toArray(new String[]{})).scopes(client.getScope().toArray(new String[]{}))
                    .resourceIds(client.getResourceIds().toArray(new String[]{})).and();
        }
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()");
    }
}
