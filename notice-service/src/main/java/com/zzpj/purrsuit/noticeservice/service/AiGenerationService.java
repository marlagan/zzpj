package com.zzpj.purrsuit.noticeservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiGenerationService {

    private final RestClient restClient;
    private final String model;

    public AiGenerationService(
            @Value("${groq.api.key:}") String apiKey,
            @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}") String apiUrl,
            @Value("${groq.api.model:llama3-70b-8192}") String model) {

        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.model = model;
    }

    public String generateNoticeDescription(String traits, String location, String name) {
        String systemPrompt = "Jesteś asystentem pomagającym znaleźć zagubione zwierzęta. Generuj emocjonalne, rzeczowe ogłoszenia o zaginionych/znalezionych zwierzętach po polsku.";
        String userPrompt = String.format("Wygeneruj ogłoszenie dla zwierzaka. Imię: %s, Cechy: %s, Ostatnia lokalizacja: %s.",
                name != null ? name : "Nieznane", traits, location);

        Map<String, Object> requestBody = Map.of(
                "model", this.model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        try {
            var response = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            log.error("Błąd podczas generowania opisu przez AI", e);
            return "Zaginął zwierzak. Prosimy o kontakt. (Błąd generowania opisu)";
        }
    }
}