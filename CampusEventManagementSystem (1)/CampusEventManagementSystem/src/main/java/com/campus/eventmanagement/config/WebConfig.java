package com.campus.eventmanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Maps simple page URLs straight to Thymeleaf templates. These pages are
 * static shells - all data on them (event lists, registrations, etc.) is
 * loaded client-side via fetch() calls to the /api/** REST endpoints in
 * app.js, so no @Controller methods with Model attributes are needed here.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/index").setViewName("index");
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/register").setViewName("register");
        registry.addViewController("/dashboard").setViewName("dashboard");
        registry.addViewController("/events").setViewName("events");
        registry.addViewController("/my-events").setViewName("my-events");
        registry.addViewController("/admin-dashboard").setViewName("admin-dashboard");
        registry.addViewController("/create-event").setViewName("create-event");
        registry.addViewController("/edit-event/{id}").setViewName("edit-event");
    }
}
