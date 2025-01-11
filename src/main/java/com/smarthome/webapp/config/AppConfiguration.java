package com.smarthome.webapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
 
@Configuration
@Profile("development")
public class AppConfiguration implements WebMvcConfigurer {

    // @Override
    // public void addCorsMappings(CorsRegistry registry) {
    //     registry.addMapping("/**")
    //         .allowedOrigins("http://localhost:4200")
    //         .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
    //         .allowedHeaders("*")
    //         .allowCredentials(true);
    // }

}