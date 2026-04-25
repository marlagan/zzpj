package com.zzpj.purrsuit.perservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SemanticMatchService {
    private final WebClient.Builder webClientBuilder;

    @Value("${llm.api.url}")
    private String llmApiUrl;

    @Value("${llm.api.key}")
    private String llmApiKey;

    @Value("${llm.api.model}")
    private String llmModel;

    public double comparePetDescription(String descriptionA, String descriptionB){
        var prompt = """
                 You are an expert at identifying animals from descriptions.
                Compare these two animal sighting descriptions and determine
                the probability (0.0 to 1.0) that they refer to the same animal.
                
                Description 1: %s
                
                Description 2: %s
                
                Respond with ONLY a decimal number between 0.0 and 1.0.
                Example: 0.85
                """.formatted(descriptionA, descriptionB);

        var requestedBody = Map.of(
                "model", llmModel,
                "messages", List.of(
                        Map.of("role","user","content",prompt)
                ),
                "temperature", 0.1,
                "max_tokens",15
        );
        var response = webClientBuilder.build()
                .post()
                .uri(llmApiUrl + "/chat/completions")
                .header("Authorization", "Bearer " + llmApiKey)
                .bodyValue(requestedBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return parseScore(response);
    }

    private double parseScore(Map<?, ?> response){
        try {
            var choices = (List<Map<?, ?>>) response.get("choices");
            var message = (Map<?, ?>) choices.get(0).get("message");
            var content = (String) message.get("content");
            return Double.parseDouble(content.trim());
        } catch (Exception e) {
            log.warn("Could not parse Groq response, defaulting to 0.0");
            return 0.0;
        }
    }



}
