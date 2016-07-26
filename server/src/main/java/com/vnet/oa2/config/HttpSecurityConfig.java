package com.vnet.oa2.config;

import com.vnet.oa2.setup.Users;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Created by olivier on 7/23/16.
 */
@Configuration
@EnableWebSecurity
public class HttpSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.requestMatchers().antMatchers("/", "/get_login", "/login", "/logout", "/oauth/authorize", "/oauth/confirm_access", "/home", "/test", "/app/**").and()
            .authorizeRequests().antMatchers(HttpMethod.GET, "/", "/get_login", "/logout", "/test").permitAll().and()
            .authorizeRequests().antMatchers(HttpMethod.POST, "/login").permitAll().and()
            .authorizeRequests().antMatchers("/app/**").hasRole(Users.Role.ADMIN.getValue()).and()
            .authorizeRequests().anyRequest().fullyAuthenticated().and()
            .formLogin().loginPage("/get_login").loginProcessingUrl("/login").and()
            .logout().deleteCookies(Users.ADMIN_ACCESS_TOKEN);
    }
}