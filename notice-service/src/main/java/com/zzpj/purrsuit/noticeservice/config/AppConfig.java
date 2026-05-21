package com.zzpj.purrsuit.noticeservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.NoSuchElementException;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Slf4j
    @RestControllerAdvice
    public static class GlobalExceptionHandler {

        @ExceptionHandler(NoSuchElementException.class)
        public ProblemDetail handleNotFound(NoSuchElementException ex) {
            log.warn("Not found: {}", ex.getMessage());
            return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ProblemDetail handleBadRequest(IllegalArgumentException ex) {
            log.warn("Bad request: {}", ex.getMessage());
            return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        @ExceptionHandler(IllegalStateException.class)
        public ProblemDetail handleConflict(IllegalStateException ex) {
            log.warn("Conflict: {}", ex.getMessage());
            return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        }
    }
}
