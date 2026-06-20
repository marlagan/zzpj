package com.zzpj.purrsuit.noticeservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Wyłączenie ochrony CSRF dla API stanowego/bezsesyjnego
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated() // Każde żądanie musi być zalogowane
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {})); // Włączenie obsługi tokenów JWT z application.yml
        
        return http.build();
    }
}