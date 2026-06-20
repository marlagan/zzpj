package com.zzpj.purrsuit.userservice.kafka;

import com.zzpj.purrsuit.common.events.UserProfileEvent;
import com.zzpj.purrsuit.userservice.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserProfileKafkaProducer {

    static final String TOPIC = "user-profile-events";

    private final KafkaTemplate<String, UserProfileEvent> kafkaTemplate;

    public void sendUserProfile(User user) {
        try {
            var event = new UserProfileEvent(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName()
            );
            kafkaTemplate.send(TOPIC, user.getId().toString(), event);
            log.info("Kafka [{}] key={} email={}", TOPIC, user.getId(), user.getEmail());
        } catch (Exception e) {
            log.error("Failed to publish to Kafka topic={} key={}", TOPIC, user.getId(), e);
        }
    }
}
