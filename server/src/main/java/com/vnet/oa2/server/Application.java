package com.vnet.oa2.server;

import com.vnet.oa2.Utils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Created by olivier on 7/6/16.
 */

@SpringBootApplication(scanBasePackages = "com.vnet.oa2")
@Configuration
public class Application extends WebMvcConfigurerAdapter {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public InternalResourceViewResolver getInternalResourceViewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/jsp/");
        resolver.setSuffix(".jsp");
        return resolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**","").addResourceLocations("/WEB-INF/css/");
        registry.addResourceHandler("/app/**","").addResourceLocations("/WEB-INF/pages/");
        registry.addResourceHandler("/js/**","").addResourceLocations("/WEB-INF/js/");
        registry.addResourceHandler("/images/**","").addResourceLocations("/WEB-INF/images/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/get_login").setViewName("get_login");
        registry.addViewController("/").setViewName("message");
    }

    // -----------------------------------
    // Initializer
    // -----------------------------------
    static public class AppInitializer extends SpringBootServletInitializer {
        @Override
        public SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
            return builder.sources(Application.class);
        }

        @Override
        public void onStartup(ServletContext container) throws ServletException {
            super.onStartup(container);
            Utils.dumpServletContext(container);
        }
    }
}
