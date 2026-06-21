package com.zzpj.purrsuit.mapservice.kafka;

import com.zzpj.purrsuit.mapservice.repository.GeoLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchingKafkaListener {
    private final GeoLocationRepository repository;

    @KafkaListener(topics = "match-found-map", groupId = "todo")
    public void consumeMatchEvent(MatchFoundNoticeEvent event) {

    }

}
