package com.vnet.oa2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import sun.reflect.annotation.ExceptionProxy;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


/**
 * Created by olivier on 7/6/16.
 */
public class Utils {

    static final private Log logger = LogFactory.getLog(Utils.class);

    /**
     * Dump Servlet Context
     * @param container Servlet Context
     */
    public static void dumpServletContext(ServletContext container) {
        logger.info("REGISTERED SERVLETS:" + container.getServletRegistrations().size());
        final Iterator iterator = container.getServletRegistrations().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            logger.info("Registration Key:"+entry.getKey());

            final ServletRegistration registration = container.getServletRegistration(entry.getKey().toString());
            final Collection<String> mappings = registration.getMappings();
            final Iterator<String> i = mappings.iterator();
            while (i.hasNext()) {
                logger.info(">> Mapping : " + i.next());
            }
        }


        // No need to do this : see @Component annotation in CORS filter
        // final FilterRegistration.Dynamic cors = container.addFilter("corsFilter", new SimpleCORSFilter());
        // cors.addMappingForUrlPatterns(null, false, "/*");
        // cors.setAsyncSupported(true);

        logger.info("REGISTERED FILTERS:" + container.getFilterRegistrations().size());
        final Iterator<? extends FilterRegistration> itr = container.getFilterRegistrations().values().iterator();
        while (itr.hasNext()) {
            final FilterRegistration r = itr.next();
            logger.info(">> " + r.getName());

            // final Collection<String> urlPatternMappings = r.getUrlPatternMappings();
            // for (String m : urlPatternMappings) {
            //     Log.log(getClass(), "M> " + m);
            // }

            // final Map<String, String> params = r.getInitParameters();
            // if (params!=null && params.keySet() != null) {
            //     final Iterator<String> s = params.keySet().iterator();
            //    while (s.hasNext()) {
            //        final String param = s.next();
            //        Log.log(getClass(), ">> " + param + ":" + params.get(param));
            //    }
            // }
        }

        // FilterRegistration filterRegistration = container.getFilterRegistration("springSecurityFilterChain");

        final WebApplicationContext web = WebApplicationContextUtils.getWebApplicationContext(container);

        // final String[] names = web.getBeanDefinitionNames();
        // Log.log(getClass(), "WEB APP BEAN DEFINITION NAMES: " + names.length);
        // for (String name : names)
        //     Log.log(getClass(), ">> " + name);

        logger.info("SERVLET FILTERS");
        final Map<String, Filter> filters = web.getBeansOfType(Filter.class);
        for (String name : filters.keySet()) {
            final Filter filter = filters.get(name);
            logger.info(">> " + name + ":" + filter.getClass().getName());
            if (filter instanceof FilterChainProxy)
                dumpFilterChainProxy((FilterChainProxy) filter);
        }
    }

    /**
     * Dump Filter Chain Proxy
     * @param proxy filter chain proxy
     */
    public static void dumpFilterChainProxy(FilterChainProxy proxy) {
        final List<SecurityFilterChain> chains = proxy.getFilterChains();
        for (SecurityFilterChain chain : chains) {
            final List<Filter> filters = chain.getFilters();
            logger.info(">>>> " + filters.size() + " filters");
            for (Filter filter : filters) {
                logger.info(">>>> " + filter.getClass().getName());
            }
        }
    }

    private static String makeURL(HttpServletRequest request, String path, boolean local) {
        if (local) {
            return new StringBuilder("http://")
                    .append(request.getLocalName()).append(":").append(request.getLocalPort())
                    .append(request.getContextPath()).append(path.startsWith("/") ? path : "/" + path)
                    .toString();
        } else {
            return new StringBuilder(request.getScheme())
                    .append("://").append(request.getHeader("host"))
                    .append(request.getContextPath()).append(path.startsWith("/") ? path : "/" + path)
                    .toString();
        }
    }

    public static String sameHostRedirectURL(HttpServletRequest request, String path) {
        return makeURL(request,path,false);
    }

    public static String makeLocalURL(HttpServletRequest request, String path) {
        return makeURL(request,path,true);
    }

    /**
     * Dump Http Servlet Request Headers
     * @param request http servlet request
     */
    public static void dumpHttpServletRequestHeaders(HttpServletRequest request) {
        logger.info(">> REQUEST HEADERS " + request.getMethod() + " " + request.getRequestURI());
        final Enumeration names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            logger.info("H> " + name + ":" + request.getHeader(name));
        }
    }

    /**
     * Dump Http Session Attributes
     * @param request http servlet request
     */
    public static void dumpHttpSessionAttributes(HttpServletRequest request) {
        logger.info(">> SESSION ATTRIBUTES");
        final HttpSession session = request.getSession(false);
        if (session == null)
            return;

        final Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            logger.info("A> " + name + ":" + session.getAttribute(name));
        }
    }

    static private class Sequence {
        private int value;
        public Sequence() {this.value = 0;}
        public Sequence(int value) {this.value = value;}
        public synchronized int nextValue() {return value++;}
    }

    // ------------------------------------
    // Utility class to groups inputs
    // when posting the "manage" JSP that
    // handles removals. Input keys are
    // encoded in the JSP and decoded in
    // the endpoint.
    // ------------------------------------
    static public class InputKey {
        static private Sequence sequence = new Sequence();
        static private String SEP = ":";

        public String getClientId() {return clientId;}
        public String getTag() {return tag;}

        public boolean forApproval() {return "approval".equals(this.prefix);}
        public boolean forToken() {return "token".equals(this.prefix);}

        private final String prefix;
        private final String clientId;
        private final String tag;
        private final int index;

        public InputKey(String prefix, String clientId, String tag) {
            this.prefix = prefix;
            this.clientId = clientId;
            this.tag = tag;
            this.index = sequence.nextValue();
        }

        public InputKey(String value) throws Exception {
            final StringTokenizer tokenizer = new StringTokenizer(value,SEP);
            if (tokenizer.countTokens()==4) {
                this.prefix = tokenizer.nextToken();
                this.clientId = tokenizer.nextToken();
                this.tag = tokenizer.nextToken();
                this.index = Integer.parseInt(tokenizer.nextToken());
            } else throw new Exception("Invalid Removal Key");
        }
        public String getValue() {
            return this.prefix + SEP + this.clientId + SEP + this.tag + SEP + this.index;
        }
    }

}
