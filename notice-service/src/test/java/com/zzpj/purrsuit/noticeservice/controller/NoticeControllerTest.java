package com.zzpj.purrsuit.noticeservice.controller;

import com.zzpj.purrsuit.common.events.NoticeStatus;
import com.zzpj.purrsuit.noticeservice.domain.NoticeType;
import com.zzpj.purrsuit.noticeservice.dto.NoticeDto.CreateNoticeRequest;
import com.zzpj.purrsuit.noticeservice.dto.NoticeDto.NoticeResponse;
import com.zzpj.purrsuit.noticeservice.service.NoticeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeControllerTest {

    @Mock private NoticeService noticeService;
    @InjectMocks private NoticeController controller;

    private CreateNoticeRequest request;

    @BeforeEach
    void setUp() {
        request = new CreateNoticeRequest();
        request.type = NoticeType.LOST;
        request.species = "kot";
        request.latitude = 51.75;
        request.longitude = 19.45;
        request.eventDate = LocalDateTime.now();
    }

    private Jwt jwt(UUID sub) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", sub.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    private NoticeResponse response(UUID id, UUID owner) {
        return NoticeResponse.builder()
                .id(id).type(NoticeType.LOST).status(NoticeStatus.ACTIVE)
                .reportedByUserId(owner).build();
    }

    @Test
    void createNotice_extractsUUIDFromJwtSubClaim() {
        UUID userId = UUID.randomUUID();
        UUID noticeId = UUID.randomUUID();
        when(noticeService.createNotice(eq(request), eq(userId))).thenReturn(response(noticeId, userId));

        var result = controller.createNotice(request, jwt(userId));

        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);
        verify(noticeService).createNotice(eq(request), captor.capture());
        assertThat(captor.getValue()).isEqualTo(userId);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void createNotice_differentTokens_passeDifferentUserIds() {
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();
        when(noticeService.createNotice(any(), any())).thenReturn(NoticeResponse.builder().build());

        controller.createNotice(request, jwt(userA));
        controller.createNotice(request, jwt(userB));

        verify(noticeService).createNotice(request, userA);
        verify(noticeService).createNotice(request, userB);
    }
}