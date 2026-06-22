package com.zzpj.purrsuit.noticeservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnimalVisionServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private AnimalVisionService service;

    @BeforeEach
    void setUp() {
        service = new AnimalVisionService(restTemplate);
    }

    private void setApiKey(String key) {
        ReflectionTestUtils.setField(service, "apiKey", key);
    }

    private ResponseEntity<Map> groqResponse(String content) {
        Map<?, ?> message  = Map.of("content", content);
        Map<?, ?> choice   = Map.of("message", message);
        Map<String, Object> body = Map.of("choices", List.of(choice));
        return ResponseEntity.ok((Map) body);
    }

    @Test
    void generateDescription_returnsNull_whenApiKeyBlank() {
        setApiKey("");
        String result = service.generateDescription("kot", "europejski", "rudy", null);
        assertThat(result).isNull();
        verifyNoInteractions(restTemplate);
    }

    @Test
    void generateDescription_returnsNull_whenApiKeyNull() {
        setApiKey(null);
        String result = service.generateDescription("kot", null, null, null);
        assertThat(result).isNull();
        verifyNoInteractions(restTemplate);
    }

    @Test
    void generateDescription_returnsContentFromGroqResponse() {
        setApiKey("test-key");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(groqResponse("Rudy kot europejski"));

        String result = service.generateDescription("kot", "europejski", "rudy", null);

        assertThat(result).isEqualTo("Rudy kot europejski");
    }

    @Test
    void generateDescription_sendsAuthorizationHeader() {
        setApiKey("my-secret-key");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(groqResponse("opis"));

        service.generateDescription("pies", null, "czarny", null);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(Map.class));
        HttpHeaders headers = captor.getValue().getHeaders();
        assertThat(headers.getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer my-secret-key");
    }

    @Test
    void generateDescription_returnsNull_whenRestTemplateThrows() {
        setApiKey("test-key");
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("connection refused"));

        String result = service.generateDescription("kot", null, null, null);

        assertThat(result).isNull();
    }

    @Test
    void generateDescription_returnsNull_whenChoicesEmpty() {
        setApiKey("test-key");
        Map<String, Object> body = Map.of("choices", List.of());
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok((Map) body));

        String result = service.generateDescription("kot", null, null, null);

        assertThat(result).isNull();
    }

    @Test
    void generateDescription_returnsNull_whenResponseBodyNull() {
        setApiKey("test-key");
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(null));

        String result = service.generateDescription("kot", null, null, null);

        assertThat(result).isNull();
    }

    @Test
    void generateDescription_includesSpeciesInRequest() {
        setApiKey("test-key");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(groqResponse("opis"));

        service.generateDescription("papuga", "ara", "zielona", "śpiewa");

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), any(), captor.capture(), eq(Map.class));
        String body = captor.getValue().getBody().toString();
        assertThat(body).contains("papuga").contains("ara").contains("zielona").contains("śpiewa");
    }
}