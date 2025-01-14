package com.smarthome.webapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
 
@Configuration
@Profile("development")
public class AppConfiguration implements WebMvcConfigurer {

}