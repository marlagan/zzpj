package com.zzpj.purrsuit.petservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Map;

/**
 * Serwis odpowiedzialny za analizę semantyczną opisów zgłoszeń.
 * Wykorzystuje zewnętrzne API Groq do oceny prawdopodobieństwa,
 * że dwa różne opisy dotyczą tego samego zwierzęcia.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SemanticMatchService {
    private final WebClient groqWebClient;

    @Value("${llm.api.url}")
    private String llmApiUrl;

    @Value("${llm.api.key}")
    private String llmApiKey;

    @Value("${llm.api.model}")
    private String llmModel;

    private record Messages(String role, String content){}
    private record GroqRequest(String model, List<Messages> messages, double temperature, int max_tokens){}

    /**
     * Porównuje dwa opisy zwierząt i zwraca wyliczony współczynnik podobieństwa.
     * Generuje odpowiedni prompt dla modelu językowego i parsuje odpowiedź.
     *
     * @param descriptionA opis pierwszego zwierzęcia
     * @param descriptionB opis drugiego zwierzęcia
     * @return wartość zmiennoprzecinkowa z zakresu 0.0 - 1.0 określająca pewność dopasowania
     */
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


        var request = new GroqRequest(
                llmModel,
                List.of(new Messages("user",prompt)),
                0.1,
                15
        );

        var response = groqWebClient
                .post()
                .uri(llmApiUrl + "/chat/completions")
                .header("Authorization", "Bearer " + llmApiKey)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .doOnNext(body -> log.error("Groq error response: {}", body))
                                .map(body -> new RuntimeException("Groq 400: " + body))
                )
                .bodyToMono(Map.class)
                .block();

        return parseScore(response);
    }

    /**
     * Parsuje odpowiedź JSON z API Groq w celu wydobycia samej liczby.
     * W przypadku błędu parsowania, bezpiecznie loguje ostrzeżenie i zwraca 0.0.
     *
     * @param response zdeserializowana odpowiedź z API w formie mapy
     * @return współczynnik prawdopodobieństwa
     */
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
