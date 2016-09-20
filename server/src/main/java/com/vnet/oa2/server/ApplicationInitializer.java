package com.vnet.oa2.server;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.vnet.oa2")
public class ApplicationInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        // Customize the application or call application.sources(...) to add sources :
        // return builder.sources(Application.class);
        // With @Configuration and @ComponentScan class we don't even need to add sources explicitly
        return builder;
    }

    // @Override
    // public void onStartup(ServletContext container) throws ServletException {
    //     super.onStartup(container);
    //    Utils.dumpServletContext(container);
    // }
}