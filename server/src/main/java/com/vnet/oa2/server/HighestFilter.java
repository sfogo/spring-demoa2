package com.vnet.oa2.server;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HighestFilter implements Filter {

    public void doFilter(ServletRequest rq,
                         ServletResponse rs,
                         FilterChain chain) throws IOException, ServletException {

        // Sort of dirty Filter that handles both :
        // - CORS
        // - Creation of an Oauth2 Token when user has admin authority.
        //   Grants access to /app/admin Angular JS application which needs
        //   an Oauth2 token with ROLE_ADMIN authority to access /admin/**
        //   endpoints (client list, user list, approvals, tokens)

        final HttpServletRequest request = (HttpServletRequest) rq;
        final HttpServletResponse response = (HttpServletResponse) rs;

        // Utils.dumpHttpServletRequestHeaders(request);
        // Utils.dumpHttpServletRequestAttributes(request);
        // Utils.dumpHttpServletResponseHeaders(response);

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");

        // Without authorization in the list, I get the following error :
        // Request header field Authorization is not allowed by Access-Control-Allow-Headers.
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with, authorization");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            if (UserHelper.isAdminPostLogin(request)) {
                // Not really the ideal place to do this as it
                // happens before the spring framework security check.
                // However, it is an easy way to intercept the resource owner password.
                // Well. It's OK as token will not be set if credentials are incorrect.
                // Conclusion, Admin credentials are verified twice :
                // 1 - When getting oauth2 token from resource owner credentials
                // 2 - With regular Spring security
                // Token is placed in a cookie to be accessible from
                // Angular JS admin application.
                UserHelper.setAdminAccessToken(request,response);
            }
            chain.doFilter(rq, rs);
        }
    }

    public void init(FilterConfig filterConfig) {}

    public void destroy() {}

}