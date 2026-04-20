package com.zzpj.purrsuit.perservice.client;

import com.zzpj.purrsuit.perservice.dto.NoticeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NoticeServiceClient {

    private final WebClient.Builder webClientBuilder;

    public NoticeDto getNotice(UUID noticeId){
        return webClientBuilder.build()
                .get()
                .uri("http://notice-service/api/v1/notices/{id}", noticeId)
                .retrieve()
                .bodyToMono(NoticeDto.class)
                .block();
    }

    public List<NoticeDto> getConfirmedNoticesByType(String type){
        return webClientBuilder.build()
                .get()
                .uri("http://notice-service/api/v1/notices?type={type}&confirmed=true", type)
                .retrieve()
                .bodyToFlux(NoticeDto.class)
                .collectList()
                .block();
    }

}
