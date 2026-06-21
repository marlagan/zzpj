package com.zzpj.purrsuit.petservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    /**
     * Tworzy klienta HTTP z wbudowanym mechanizmem Load Balancingu.
     * Wykorzystywany do komunikacji wewnętrznej między mikroserwisami.
     *
     * @return builder dla WebClienta z adnotacją @LoadBalanced
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder(){
        return WebClient.builder();
    }

    /**
     * Tworzy dedykowanego klienta HTTP do komunikacji z zewnętrznym API Groq.
     *
     * @return instancja WebClient
     */
    @Bean
    public WebClient groqWebClient(){
        return WebClient.builder().build();
    }
}
