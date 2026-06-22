package com.zzpj.purrsuit.noticeservice.controller;

import com.zzpj.purrsuit.noticeservice.domain.MatchStatus;
import com.zzpj.purrsuit.noticeservice.dto.NoticeMatchDto;
import com.zzpj.purrsuit.noticeservice.entity.NoticeMatch;
import com.zzpj.purrsuit.noticeservice.service.NoticeMatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeMatchControllerTest {

    @Mock private NoticeMatchService noticeMatchService;
    @InjectMocks private NoticeMatchController controller;

    private UUID matchId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        matchId = UUID.randomUUID();
        userId  = UUID.randomUUID();
    }

    private Jwt jwt(UUID sub) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", sub.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    private NoticeMatch match(MatchStatus status) {
        return NoticeMatch.builder()
                .id(matchId)
                .lostNoticeId(UUID.randomUUID())
                .seenNoticeId(UUID.randomUUID())
                .lostOwnerId(userId)
                .similarityScore(0.9)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void confirmMatch_extractsUserIdFromJwt() {
        when(noticeMatchService.confirmMatch(eq(matchId), eq(userId))).thenReturn(match(MatchStatus.CONFIRMED));

        controller.confirmMatch(matchId, jwt(userId));

        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);
        verify(noticeMatchService).confirmMatch(eq(matchId), captor.capture());
        assertThat(captor.getValue()).isEqualTo(userId);
    }

    @Test
    void confirmMatch_returnsDtoWithConfirmedStatus() {
        when(noticeMatchService.confirmMatch(any(), any())).thenReturn(match(MatchStatus.CONFIRMED));

        var response = controller.confirmMatch(matchId, jwt(userId));

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(MatchStatus.CONFIRMED);
    }

    @Test
    void rejectMatch_extractsUserIdFromJwt() {
        when(noticeMatchService.rejectMatch(eq(matchId), eq(userId))).thenReturn(match(MatchStatus.REJECTED));

        controller.rejectMatch(matchId, jwt(userId));

        verify(noticeMatchService).rejectMatch(matchId, userId);
    }

    @Test
    void rejectMatch_returnsDtoWithRejectedStatus() {
        when(noticeMatchService.rejectMatch(any(), any())).thenReturn(match(MatchStatus.REJECTED));

        var response = controller.rejectMatch(matchId, jwt(userId));

        assertThat(response.getBody().getStatus()).isEqualTo(MatchStatus.REJECTED);
    }

    @Test
    void getMatchesForNotice_returnsMappedList() {
        UUID noticeId = UUID.randomUUID();
        when(noticeMatchService.getMatchesForNotice(noticeId)).thenReturn(List.of(match(MatchStatus.PENDING)));

        var response = controller.getMatchesForNotice(noticeId);

        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getId()).isEqualTo(matchId);
        assertThat(response.getBody().get(0).getSimilarityScore()).isEqualTo(0.9);
    }

    @Test
    void confirmAndReject_withDifferentTokens_passeDifferentUserIds() {
        UUID otherUser = UUID.randomUUID();
        when(noticeMatchService.confirmMatch(any(), any())).thenReturn(match(MatchStatus.CONFIRMED));

        controller.confirmMatch(matchId, jwt(userId));
        controller.confirmMatch(matchId, jwt(otherUser));

        verify(noticeMatchService).confirmMatch(matchId, userId);
        verify(noticeMatchService).confirmMatch(matchId, otherUser);
    }
}