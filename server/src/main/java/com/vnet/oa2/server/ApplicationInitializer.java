package com.vnet.oa2.server;

import com.vnet.oa2.Utils;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

@Configuration
@ComponentScan(basePackages = "com.vnet.oa2")
public class ApplicationInitializer extends SpringBootServletInitializer {

    @Override
    public SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        // return builder.sources(Application.class);
        // Customize the application or call application.sources(...) to add sources.
        // With @Configuration class we actually don't need to override this method.
        return builder;
    }

    @Override
    public void onStartup(ServletContext container) throws ServletException {
        super.onStartup(container);
        Utils.dumpServletContext(container);
    }
}