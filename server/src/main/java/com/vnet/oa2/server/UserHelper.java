package com.vnet.oa2.server;

import com.vnet.oa2.Utils;
import com.vnet.oa2.setup.Clients;
import com.vnet.oa2.setup.Scopes;
import com.vnet.oa2.setup.Users;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class UserHelper {
    static final private Log logger = LogFactory.getLog(UserHelper.class);

    private final String username;
    private final boolean isAdmin;

    public boolean isAdmin() {return this.isAdmin;}
    public boolean isThere() {return this.username != null;}
    public String getUsername() {return this.username;}

    /**
     * Helps display JSP header
     * @param request
     */
    public UserHelper(HttpServletRequest request) {
        final Object springSecurityContext = (request.getSession(false) == null)
                ? null
                : request.getSession(false).getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);

        if (springSecurityContext == null || !(springSecurityContext instanceof SecurityContext)) {
            username = null;
            isAdmin = false;
        } else {
            final Authentication authentication = ((SecurityContext) springSecurityContext).getAuthentication();
            username = (authentication == null || authentication instanceof AnonymousAuthenticationToken)
                    ? null
                    : authentication.getName();
            isAdmin = Users.isAdminName(username);
        }
    }

    /**
     * Detect Admin POST /login
     * @param request Http Servlet Request
     * @return true if it's POST /login with an admin name
     */
    public static boolean isAdminPostLogin(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) &&
                request.getRequestURI().endsWith("/login") &&
                Users.isAdminName(request.getParameter("username"));
    }

    /**
     * Grant Admin OAuth2 token with
     * Resource Owner credentials
     * @param request Http Servlet Request
     * @param response Http Servlet Response
     */
    public static void setAdminAccessToken(HttpServletRequest request,
                                           HttpServletResponse response) {
        final String url = Utils.makeLocalURL(request, "/oauth/token");
        final String data = "grant_type=password" +
                "&username=" + request.getParameter("username") +
                "&password=" + request.getParameter("password") +
                "&scope=" + (Users.isSuperAdminName(request.getParameter("username")) ?
                            (Scopes.ADMIN_READ + " " + Scopes.ADMIN_WRITE) : Scopes.ADMIN_READ);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("authorization", Clients.getAdminClientBasicAuthorization());

        final HttpEntity<String> entity = new HttpEntity<>(data, headers);
        final RestTemplate t = new RestTemplate();
        t.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        t.getMessageConverters().add(new StringHttpMessageConverter());

        try {
            final ResponseEntity<String> responseEntity = t.exchange(url, HttpMethod.POST, entity, String.class);
            logger.info("setAdminAccessToken:" + responseEntity.getBody());
            if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                final Map<String,String> map = new ObjectMapper().readValue(responseEntity.getBody(), Map.class);
                // response.addCookie(new Cookie(Users.ADMIN_ACCESS_TOKEN, new ObjectMapper().writeValueAsString(map)));
                response.addCookie(new Cookie(Users.ADMIN_ACCESS_TOKEN, map.get("access_token")));
            }
        } catch (Exception e) {
            logger.info("setAdminAccessToken:" + e.toString());
        }
    }
}
