package com.vnet.oa2.setup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ArrayList;


/**
 * Just a utility class to add more users
 */

public class Users implements AuthenticationProvider {

    public enum Role {
        USER("USER"), ADMIN("ADMIN");
        private String value;
        Role(String value) {this.value = value;}
        public String getValue() {return this.value;}
        public String getPrefixedValue() {return "ROLE_" + this.value;}
    }

    static final public String ADMIN_ACCESS_TOKEN = "ADMIN_ACCESS_TOKEN";
    static final private HashMap<String,User> userMap = createUsers();

    final private HashMap<String,User> users;
    final private Log logger = LogFactory.getLog(getClass());

    public Users() {
        users = userMap;
    }

    public static boolean isAdminName(String name) {
        if (name == null)
            return false;

        final User user = userMap.get(name);
        if (user == null)
            return false;

        final Iterator<GrantedAuthority> iterator = user.getAuthorities().iterator();
        while (iterator.hasNext()) {
            final GrantedAuthority authority = iterator.next();
            if (Role.ADMIN.getPrefixedValue().equals(authority.getAuthority()))
                return true;
        }
        return false;
    }

    static private HashMap<String,User> createUsers() {
        final HashMap<String,User> map = new HashMap<>();
        map.put("admin", createUser("admin", "admin", new String[] {Role.ADMIN.getPrefixedValue()}));
        for (int i=1; i<=10; i++) {
            final String name = "user" + i;
            map.put(name, createUser(name, "password" + i, new String[] {Role.USER.getPrefixedValue()}));
        }
        return map;
    }

    static private User createUser(String name, String pass, String[] roles) {
        final Collection<SimpleGrantedAuthority> authorities = new LinkedList<>();
        for (int i=0; i<roles.length; i++)
            authorities.add(new SimpleGrantedAuthority(roles[i]));
        return new User(name, pass, authorities);
    }

    public Collection<String> names() {
        final Collection<String> names = new LinkedList<>();
        final Iterator<String> iterator = users.keySet().iterator();
        while (iterator.hasNext()) names.add(iterator.next());
        return names;
    }

    public Collection<UserDetails> listUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        final User user = users.get(authentication.getName());
        if (user == null)
            throw new BadCredentialsException("Invalid Credentials");

        if (user.getPassword().equals(authentication.getCredentials().toString())) {
            logger.info("Authenticated " + authentication.getName());
            return new UsernamePasswordAuthenticationToken(
                    authentication.getPrincipal(),
                    authentication.getCredentials(),
                    user.getAuthorities());
        }

        throw new BadCredentialsException("Invalid Credentials");
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(UsernamePasswordAuthenticationToken.class);
    }
}
