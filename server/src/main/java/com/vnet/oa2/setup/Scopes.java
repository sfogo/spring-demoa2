package com.vnet.oa2.setup;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by olivier on 6/29/16.
 */
public class Scopes {
    public static String ADMIN_READ = "ADMIN_READ";
    public static String ADMIN_WRITE = "ADMIN_WRITE";

    private static final String[] clientScopes = {"A","B","C"};
    private static final String[] adminScopes = {ADMIN_READ,ADMIN_WRITE};

    static public Collection<String> getClientScopes() {
        return Arrays.asList(clientScopes);
    }

    static public Collection<String> getAdminScopes() {
        return Arrays.asList(adminScopes);
    }
}
