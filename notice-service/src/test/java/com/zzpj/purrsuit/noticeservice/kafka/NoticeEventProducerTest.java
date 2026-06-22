package com.zzpj.purrsuit.noticeservice.kafka;

import com.zzpj.purrsuit.common.events.NoticeCreatedEvent;
import com.zzpj.purrsuit.common.events.NoticeLocationEvent;
import com.zzpj.purrsuit.common.events.NoticeStatus;
import com.zzpj.purrsuit.noticeservice.domain.NoticeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeEventProducerTest {

    @Mock private KafkaTemplate<String, NoticeLocationEvent>  kafkaMapTemplate;
    @Mock private KafkaTemplate<String, NoticeCreatedEvent>   kafkaDescriptionTemplate;

    @InjectMocks
    private NoticeEventProducer producer;

    private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);

    private Point point(double lon, double lat) {
        return GF.createPoint(new Coordinate(lon, lat));
    }

    // --- sendDescriptionEvent ---

    @Test
    void sendDescriptionEvent_sendsToCorrectTopic() {
        UUID noticeId = UUID.randomUUID();
        UUID userId   = UUID.randomUUID();

        producer.sendDescriptionEvent(noticeId, userId, "kot", "rudy kot", NoticeType.LOST, NoticeStatus.ACTIVE);

        verify(kafkaDescriptionTemplate).send(
                eq(NoticeEventProducer.TOPIC_DESCRIPTION), eq(noticeId.toString()), any(NoticeCreatedEvent.class));
    }

    @Test
    void sendDescriptionEvent_payloadContainsCorrectFields() {
        UUID noticeId = UUID.randomUUID();
        UUID userId   = UUID.randomUUID();

        producer.sendDescriptionEvent(noticeId, userId, "pies", "duży czarny pies", NoticeType.FOUND, NoticeStatus.ACTIVE);

        ArgumentCaptor<NoticeCreatedEvent> captor = ArgumentCaptor.forClass(NoticeCreatedEvent.class);
        verify(kafkaDescriptionTemplate).send(any(), any(), captor.capture());
        NoticeCreatedEvent event = captor.getValue();
        assertThat(event.noticeId()).isEqualTo(noticeId);
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.species()).isEqualTo("pies");
        assertThat(event.description()).isEqualTo("duży czarny pies");
        assertThat(event.type()).isEqualTo(NoticeType.FOUND.toString());
    }

    @Test
    void sendDescriptionEvent_doesNotThrow_whenKafkaFails() {
        when(kafkaDescriptionTemplate.send(any(), any(), any()))
                .thenThrow(new RuntimeException("broker down"));

        assertThatCode(() ->
                producer.sendDescriptionEvent(UUID.randomUUID(), UUID.randomUUID(),
                        "kot", "opis", NoticeType.LOST, NoticeStatus.ACTIVE))
                .doesNotThrowAnyException();
    }

    // --- sendLocationEvent ---

    @Test
    void sendLocationEvent_sendsToCorrectTopic() {
        UUID noticeId = UUID.randomUUID();

        producer.sendLocationEvent(noticeId, NoticeType.LOST, point(19.45, 51.75),
                LocalDateTime.now(), NoticeStatus.ACTIVE, "kot");

        verify(kafkaMapTemplate).send(
                eq(NoticeEventProducer.TOPIC_LOCATION), eq(noticeId.toString()), any(NoticeLocationEvent.class));
    }

    @Test
    void sendLocationEvent_mapsLatLonCorrectly() {
        UUID noticeId = UUID.randomUUID();
        // JTS: x=lon, y=lat — producent powinien zamienić: lat=y, lon=x
        producer.sendLocationEvent(noticeId, NoticeType.LOST, point(19.45, 51.75),
                LocalDateTime.now(), NoticeStatus.ACTIVE, "kot");

        ArgumentCaptor<NoticeLocationEvent> captor = ArgumentCaptor.forClass(NoticeLocationEvent.class);
        verify(kafkaMapTemplate).send(any(), any(), captor.capture());
        NoticeLocationEvent event = captor.getValue();
        assertThat(event.latitude()).isEqualTo(51.75);
        assertThat(event.longitude()).isEqualTo(19.45);
    }

    @Test
    void sendLocationEvent_doesNotThrow_whenKafkaFails() {
        when(kafkaMapTemplate.send(any(), any(), any()))
                .thenThrow(new RuntimeException("broker down"));

        assertThatCode(() ->
                producer.sendLocationEvent(UUID.randomUUID(), NoticeType.FOUND, point(0, 0),
                        LocalDateTime.now(), NoticeStatus.ACTIVE, "pies"))
                .doesNotThrowAnyException();
    }
}