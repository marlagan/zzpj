package com.zzpj.purrsuit.petservice.client;

import com.zzpj.purrsuit.petservice.dto.NoticeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeServiceClient {

    private final WebClient.Builder webClientBuilder;

    public NoticeDto getNotice(UUID noticeId){
//        return webClientBuilder.build()
//                .get()
//                .uri("http://notice-service/api/notices/{id}", noticeId)
//                .retrieve()
//                .bodyToMono(NoticeDto.java.class)
//                .block();
//        todo odkomentować jak notice-service zosatnie zaimplementowany
        log.warn("NoticeServiceClient: using stub for noticeId={}", noticeId);
        return new NoticeDto(
                noticeId,
                "cat",
                "Orange tabby cat with white paws, friendly, wearing blue collar",
                52.2297,
                21.0122,
                "LOST"
        );
    }

    public List<NoticeDto> getConfirmedNoticesByType(String type){
//        return webClientBuilder.build()
//                .get()
//                .uri("http://notice-service/api/notices?type={type}&confirmed=true", type)
//                .retrieve()
//                .bodyToFlux(NoticeDto.java.class)
//                .collectList()
//                .block();
//        todo odkomentować jak notice-service zosatnie zaimplementowany
        log.warn("NoticeServiceClient: using stub for type={}", type);
        return List.of(
                new NoticeDto(
                        UUID.randomUUID(),
                        "cat",
                        "Light red-brown cat seen near park, white paws, had a collar",
                        52.2310,
                        21.0145,
                        type
                ),
                new NoticeDto(
                        UUID.randomUUID(),
                        "dog",
                        "Small brown dog, lost near train station",
                        52.2280,
                        21.0100,
                        type
                )
        );
    }

}
