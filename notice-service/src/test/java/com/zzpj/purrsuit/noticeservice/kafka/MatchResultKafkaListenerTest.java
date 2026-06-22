package com.zzpj.purrsuit.noticeservice.kafka;

import com.zzpj.purrsuit.common.events.MatchResultEvent;
import com.zzpj.purrsuit.noticeservice.service.NoticeMatchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchResultKafkaListenerTest {

    @Mock private NoticeMatchService noticeMatchService;
    @InjectMocks private MatchResultKafkaListener listener;

    private MatchResultEvent event() {
        return new MatchResultEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 0.87);
    }

    @Test
    void consumeMatchResult_delegatesToService() {
        MatchResultEvent e = event();
        listener.consumeMatchResult(e);
        verify(noticeMatchService).handleMatchFound(e);
    }

    @Test
    void consumeMatchResult_swallowsRuntimeException() {
        doThrow(new RuntimeException("DB error")).when(noticeMatchService).handleMatchFound(any());
        assertThatCode(() -> listener.consumeMatchResult(event())).doesNotThrowAnyException();
    }

    @Test
    void consumeMatchResult_swallowsIllegalStateException() {
        doThrow(new IllegalStateException("bad state")).when(noticeMatchService).handleMatchFound(any());
        assertThatCode(() -> listener.consumeMatchResult(event())).doesNotThrowAnyException();
    }
}