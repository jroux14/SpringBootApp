package com.smarthome.webapp.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.smarthome.webapp.services.UserService;
import com.smarthome.webapp.jwt.JwtRequestFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    
    @Autowired
    private UserService userService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;
    
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200")); // Replace with your frontend origin
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true); // If you need to send cookies

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable())
            .cors().and()
            .authorizeHttpRequests()
                .requestMatchers("/smarthome/auth/**").permitAll()  // Public endpoints
                .requestMatchers("/smarthome/user/**").authenticated()  // Secured endpoints
                .anyRequest().authenticated()
                .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);  // No sessions

        // Basic Auth Configuration
        // http.csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable())
        // .cors().and()
        // .httpBasic(Customizer.withDefaults())
        // .authorizeHttpRequests(
        //     authorizeRequest -> authorizeRequest
        //             .requestMatchers("/smarthome/test").hasAuthority("defaultUser")
        //             .anyRequest().permitAll()
        // ).formLogin(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
