package com.zzpj.purrsuit.noticeservice.kafka;

import com.zzpj.purrsuit.common.events.NoticeStatus;
import com.zzpj.purrsuit.common.events.NoticeStatusUpdateEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeUpdateProducerTest {

    @Mock
    private KafkaTemplate<String, NoticeStatusUpdateEvent> kafkaNoticeUpdateTemplate;

    @InjectMocks
    private NoticeUpdateProducer producer;

    // --- sendNoticeStatusUpdate ---

    @Test
    void sendNoticeStatusUpdate_sendsToCorrectTopic() {
        UUID noticeId = UUID.randomUUID();
        producer.sendNoticeStatusUpdate(noticeId, NoticeStatus.RESOLVED);
        verify(kafkaNoticeUpdateTemplate).send(
                eq(NoticeUpdateProducer.TOPIC_NOTICE_UPDATE), eq(noticeId.toString()), any());
    }

    @Test
    void sendNoticeStatusUpdate_payloadContainsStatus() {
        UUID noticeId = UUID.randomUUID();
        producer.sendNoticeStatusUpdate(noticeId, NoticeStatus.RESOLVED);

        ArgumentCaptor<NoticeStatusUpdateEvent> captor = ArgumentCaptor.forClass(NoticeStatusUpdateEvent.class);
        verify(kafkaNoticeUpdateTemplate).send(any(), any(), captor.capture());
        assertThat(captor.getValue().noticeId()).isEqualTo(noticeId);
        assertThat(captor.getValue().newStatus()).isEqualTo(NoticeStatus.RESOLVED.toString());
    }

    @Test
    void sendNoticeStatusUpdate_doesNotThrow_whenKafkaFails() {
        when(kafkaNoticeUpdateTemplate.send(any(), any(), any()))
                .thenThrow(new RuntimeException("broker down"));

        assertThatCode(() -> producer.sendNoticeStatusUpdate(UUID.randomUUID(), NoticeStatus.ACTIVE))
                .doesNotThrowAnyException();
    }

    // --- sendMapNoticeStatusUpdate ---

    @Test
    void sendMapNoticeStatusUpdate_sendsToMapTopic() {
        UUID noticeId = UUID.randomUUID();
        producer.sendMapNoticeStatusUpdate(noticeId, NoticeStatus.ACTIVE);
        verify(kafkaNoticeUpdateTemplate).send(
                eq(NoticeUpdateProducer.TOPIC_NOTICE_UPDATE_MAP), eq(noticeId.toString()), any());
    }

    @Test
    void sendMapNoticeStatusUpdate_doesNotThrow_whenKafkaFails() {
        when(kafkaNoticeUpdateTemplate.send(any(), any(), any()))
                .thenThrow(new RuntimeException("broker down"));

        assertThatCode(() -> producer.sendMapNoticeStatusUpdate(UUID.randomUUID(), NoticeStatus.RESOLVED))
                .doesNotThrowAnyException();
    }

    @Test
    void sendNoticeStatusUpdate_andMap_useDistinctTopics() {
        UUID noticeId = UUID.randomUUID();
        producer.sendNoticeStatusUpdate(noticeId, NoticeStatus.RESOLVED);
        producer.sendMapNoticeStatusUpdate(noticeId, NoticeStatus.RESOLVED);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaNoticeUpdateTemplate, times(2)).send(topicCaptor.capture(), any(), any());
        assertThat(topicCaptor.getAllValues())
                .containsExactlyInAnyOrder(
                        NoticeUpdateProducer.TOPIC_NOTICE_UPDATE,
                        NoticeUpdateProducer.TOPIC_NOTICE_UPDATE_MAP);
    }
}