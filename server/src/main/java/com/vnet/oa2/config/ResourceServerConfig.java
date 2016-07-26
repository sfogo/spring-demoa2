package com.vnet.oa2.config;

import com.vnet.oa2.setup.Resources;
import com.vnet.oa2.setup.Scopes;
import com.vnet.oa2.setup.Users;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

// -----------------------------------
// Resource Server Configuration
// -----------------------------------
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    private final String ACCESS = "hasRole('ROLE_<r>') and #oauth2.hasScope('<s>')";

    private final String getAccess(Users.Role role, String scope) {
        return ACCESS.replaceFirst("<r>", role.getValue()).replaceFirst("<s>", scope);
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId(Resources.getIdentifiers()[0]);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.requestMatchers().antMatchers("/user", "/admin/**", "/things/**").and()
            .authorizeRequests().antMatchers(HttpMethod.GET, "/admin/**").access(getAccess(Users.Role.ADMIN, Scopes.ADMIN_READ)).and()
            .authorizeRequests().antMatchers(HttpMethod.DELETE, "/admin/**").access(getAccess(Users.Role.ADMIN, Scopes.ADMIN_WRITE)).and()
            .authorizeRequests().antMatchers(HttpMethod.GET, "/things/A/**").access(getAccess(Users.Role.USER, "A")).and()
            .authorizeRequests().antMatchers(HttpMethod.GET, "/things/B/**").access(getAccess(Users.Role.USER, "B")).and()
            .authorizeRequests().antMatchers(HttpMethod.GET, "/things/C/**").access(getAccess(Users.Role.USER, "C")).and()
            .authorizeRequests().antMatchers("/user").authenticated();
    }
}