package com.zzpj.purrsuit.noticeservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Generuje tekstowy opis zwierzęcia przy użyciu Groq LLM
 * na podstawie danych z formularza (gatunek, rasa, kolor, notatki).
 *
 * Groq używa API kompatybilnego z OpenAI — format identyczny
 * jak SemanticMatchService w pet-service.
 */
@Slf4j
@Service
public class AnimalVisionService {

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL   = "llama-3.3-70b-versatile";

    @Value("${groq.api-key:}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public AnimalVisionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Generuje opis zwierzęcia na podstawie danych z formularza.
     * Zwraca null gdy brak klucza API lub wywołanie się nie powiedzie.
     *
     * @param species          gatunek (np. "kot", "pies")
     * @param breed            rasa (może być null)
     * @param colorDescription kolor / umaszczenie (może być null)
     * @param additionalNotes  dodatkowe uwagi użytkownika (może być null)
     */
    public String generateDescription(String species, String breed,
                                      String colorDescription, String additionalNotes) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GROQ_API_KEY not set — skipping AI description generation");
            return null;
        }

        log.info("Requesting Groq description for species={} breed={}", species, breed);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", MODEL,
                "max_tokens", 300,
                "messages", List.of(
                        Map.of("role", "system", "content", buildSystemPrompt()),
                        Map.of("role", "user",   "content", buildUserPrompt(
                                species, breed, colorDescription, additionalNotes))
                )
        );

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    API_URL, HttpMethod.POST,
                    new HttpEntity<>(body, headers), Map.class);
            return extractText(response.getBody());
        } catch (Exception e) {
            log.error("Groq API call failed", e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<?, ?> body) {
        // OpenAI-compatible: choices[0].message.content
        if (body == null) return null;
        var choices = (List<?>) body.get("choices");
        if (choices == null || choices.isEmpty()) return null;
        var message = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
        return (String) message.get("content");
    }

    private String buildSystemPrompt() {
        return """
                Jesteś asystentem pomagającym w odnajdywaniu zagubionych zwierząt.
                Piszesz zwięzłe opisy zwierząt po polsku na podstawie danych z formularza.
                Jeden akapit, bez nagłówków, bez wyliczania punktów.
                Opis ma pomóc innej osobie rozpoznać to konkretne zwierzę.
                """;
    }

    private String buildUserPrompt(String species, String breed,
                                   String colorDescription, String additionalNotes) {
        return String.format("""
                Napisz opis zwierzęcia na podstawie poniższych danych:
                Gatunek: %s
                Rasa: %s
                Kolor / umaszczenie: %s
                Dodatkowe informacje: %s
                """,
                nvl(species),
                nvl(breed),
                nvl(colorDescription),
                nvl(additionalNotes));
    }

    private String nvl(String s) {
        return (s != null && !s.isBlank()) ? s : "brak informacji";
    }
}
