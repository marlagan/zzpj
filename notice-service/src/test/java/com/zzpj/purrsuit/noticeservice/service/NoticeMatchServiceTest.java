package com.zzpj.purrsuit.noticeservice.service;

import com.zzpj.purrsuit.common.events.MatchResultEvent;
import com.zzpj.purrsuit.common.events.NoticeStatus;
import com.zzpj.purrsuit.noticeservice.domain.MatchStatus;
import com.zzpj.purrsuit.noticeservice.domain.NoticeType;
import com.zzpj.purrsuit.noticeservice.entity.Notice;
import com.zzpj.purrsuit.noticeservice.entity.NoticeMatch;
import com.zzpj.purrsuit.noticeservice.kafka.MatchDecisionProducer;
import com.zzpj.purrsuit.noticeservice.kafka.NoticeUpdateProducer;
import com.zzpj.purrsuit.noticeservice.repository.NoticeMatchRepository;
import com.zzpj.purrsuit.noticeservice.repository.NoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NoticeMatchServiceTest {

    @Mock private NoticeMatchRepository noticeMatchRepository;
    @Mock private NoticeRepository noticeRepository;
    @Mock private MatchDecisionProducer matchDecisionProducer;
    @Mock private NoticeUpdateProducer noticeUpdateProducer;

    @InjectMocks
    private NoticeMatchService noticeMatchService;

    private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);

    private UUID lostNoticeId;
    private UUID seenNoticeId;
    private UUID lostOwnerId;
    private UUID seenReporterId;
    private UUID matchId;
    private UUID strangerId;

    @BeforeEach
    void setUp() {
        lostNoticeId  = UUID.randomUUID();
        seenNoticeId  = UUID.randomUUID();
        lostOwnerId   = UUID.randomUUID();
        seenReporterId = UUID.randomUUID();
        matchId       = UUID.randomUUID();
        strangerId    = UUID.randomUUID();

        when(noticeMatchRepository.save(any())).thenAnswer(inv -> {
            NoticeMatch m = inv.getArgument(0);
            if (m.getId() == null) m.setId(UUID.randomUUID());
            if (m.getCreatedAt() == null) m.setCreatedAt(LocalDateTime.now());
            if (m.getStatus() == null) m.setStatus(MatchStatus.PENDING);
            return m;
        });
        when(noticeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // --- Fixtures ---

    private Notice lostNotice(NoticeStatus status) {
        return Notice.builder()
                .id(lostNoticeId).type(NoticeType.LOST).status(status)
                .reportedByUserId(lostOwnerId).species("kot")
                .location(GF.createPoint(new Coordinate(19.0, 51.0)))
                .eventDate(LocalDateTime.now()).createdAt(LocalDateTime.now())
                .build();
    }

    private Notice seenNotice(NoticeStatus status) {
        return Notice.builder()
                .id(seenNoticeId).type(NoticeType.FOUND).status(status)
                .reportedByUserId(seenReporterId).species("kot")
                .location(GF.createPoint(new Coordinate(19.1, 51.1)))
                .eventDate(LocalDateTime.now()).createdAt(LocalDateTime.now())
                .build();
    }

    private NoticeMatch pendingMatch() {
        return NoticeMatch.builder()
                .id(matchId).lostNoticeId(lostNoticeId).seenNoticeId(seenNoticeId)
                .lostOwnerId(lostOwnerId).similarityScore(0.88)
                .status(MatchStatus.PENDING).createdAt(LocalDateTime.now())
                .build();
    }

    private MatchResultEvent event() {
        return new MatchResultEvent(lostNoticeId, seenNoticeId, lostOwnerId, 0.88);
    }

    // --- handleMatchFound ---

    @Test
    void handleMatchFound_createsMatchAndSetsBothNoticesToPendingMatch() {
        when(noticeMatchRepository.findByLostNoticeIdAndSeenNoticeId(lostNoticeId, seenNoticeId))
                .thenReturn(Optional.empty());
        when(noticeRepository.findById(lostNoticeId)).thenReturn(Optional.of(lostNotice(NoticeStatus.ACTIVE)));
        when(noticeRepository.findById(seenNoticeId)).thenReturn(Optional.of(seenNotice(NoticeStatus.ACTIVE)));

        noticeMatchService.handleMatchFound(event());

        verify(noticeMatchRepository).save(any(NoticeMatch.class));
        verify(noticeRepository, times(2)).save(any(Notice.class));
    }

    @Test
    void handleMatchFound_skipsIfMatchAlreadyExists() {
        when(noticeMatchRepository.findByLostNoticeIdAndSeenNoticeId(lostNoticeId, seenNoticeId))
                .thenReturn(Optional.of(pendingMatch()));

        noticeMatchService.handleMatchFound(event());

        verify(noticeMatchRepository, never()).save(any());
        verify(noticeRepository, never()).save(any());
    }

    @Test
    void handleMatchFound_doesNotOverrideNonActiveNoticeStatus() {
        when(noticeMatchRepository.findByLostNoticeIdAndSeenNoticeId(any(), any()))
                .thenReturn(Optional.empty());
        when(noticeRepository.findById(lostNoticeId)).thenReturn(Optional.of(lostNotice(NoticeStatus.RESOLVED)));
        when(noticeRepository.findById(seenNoticeId)).thenReturn(Optional.of(seenNotice(NoticeStatus.ACTIVE)));

        noticeMatchService.handleMatchFound(event());

        // tylko seen notice (ACTIVE) powinien być zapisany
        verify(noticeRepository, times(1)).save(any());
    }

    // --- confirmMatch ---

    @Test
    void confirmMatch_setsConfirmedAndResolvesNotices_whenLostOwnerDecides() {
        when(noticeMatchRepository.findById(matchId)).thenReturn(Optional.of(pendingMatch()));
        when(noticeRepository.findById(lostNoticeId)).thenReturn(Optional.of(lostNotice(NoticeStatus.PENDING_MATCH)));
        when(noticeRepository.findById(seenNoticeId)).thenReturn(Optional.of(seenNotice(NoticeStatus.PENDING_MATCH)));

        NoticeMatch result = noticeMatchService.confirmMatch(matchId, lostOwnerId);

        assertThat(result.getStatus()).isEqualTo(MatchStatus.CONFIRMED);
        assertThat(result.getDecidedAt()).isNotNull();
        verify(noticeUpdateProducer).sendNoticeStatusUpdate(lostNoticeId, NoticeStatus.RESOLVED);
        verify(noticeUpdateProducer).sendNoticeStatusUpdate(seenNoticeId, NoticeStatus.RESOLVED);
        verify(matchDecisionProducer).notifyMatchConfirmed(result);
    }

    @Test
    void confirmMatch_succeedsAlso_whenSeenReporterDecides() {
        when(noticeMatchRepository.findById(matchId)).thenReturn(Optional.of(pendingMatch()));
        when(noticeRepository.findById(lostNoticeId)).thenReturn(Optional.of(lostNotice(NoticeStatus.PENDING_MATCH)));
        when(noticeRepository.findById(seenNoticeId)).thenReturn(Optional.of(seenNotice(NoticeStatus.PENDING_MATCH)));

        NoticeMatch result = noticeMatchService.confirmMatch(matchId, seenReporterId);

        assertThat(result.getStatus()).isEqualTo(MatchStatus.CONFIRMED);
    }

    @Test
    void confirmMatch_throwsSecurityException_forStranger() {
        when(noticeMatchRepository.findById(matchId)).thenReturn(Optional.of(pendingMatch()));
        when(noticeRepository.findById(lostNoticeId)).thenReturn(Optional.of(lostNotice(NoticeStatus.PENDING_MATCH)));
        when(noticeRepository.findById(seenNoticeId)).thenReturn(Optional.of(seenNotice(NoticeStatus.PENDING_MATCH)));

        assertThatThrownBy(() -> noticeMatchService.confirmMatch(matchId, strangerId))
                .isInstanceOf(SecurityException.class);

        verify(matchDecisionProducer, never()).notifyMatchConfirmed(any());
    }

    @Test
    void confirmMatch_throwsIllegalStateException_whenAlreadyDecided() {
        NoticeMatch decided = pendingMatch();
        decided.setStatus(MatchStatus.CONFIRMED);
        when(noticeMatchRepository.findById(matchId)).thenReturn(Optional.of(decided));

        assertThatThrownBy(() -> noticeMatchService.confirmMatch(matchId, lostOwnerId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void confirmMatch_throwsNoSuchElementException_whenMatchNotFound() {
        when(noticeMatchRepository.findById(matchId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noticeMatchService.confirmMatch(matchId, lostOwnerId))
                .isInstanceOf(NoSuchElementException.class);
    }

    // --- rejectMatch ---

    @Test
    void rejectMatch_setsRejectedAndRestoresNoticesToActive() {
        when(noticeMatchRepository.findById(matchId)).thenReturn(Optional.of(pendingMatch()));
        when(noticeRepository.findById(lostNoticeId)).thenReturn(Optional.of(lostNotice(NoticeStatus.PENDING_MATCH)));
        when(noticeRepository.findById(seenNoticeId)).thenReturn(Optional.of(seenNotice(NoticeStatus.PENDING_MATCH)));

        NoticeMatch result = noticeMatchService.rejectMatch(matchId, seenReporterId);

        assertThat(result.getStatus()).isEqualTo(MatchStatus.REJECTED);
        verify(noticeUpdateProducer).sendNoticeStatusUpdate(lostNoticeId, NoticeStatus.ACTIVE);
        verify(noticeUpdateProducer).sendNoticeStatusUpdate(seenNoticeId, NoticeStatus.ACTIVE);
        verify(matchDecisionProducer).notifyMatchRejected(result);
        verify(matchDecisionProducer, never()).notifyMatchConfirmed(any());
    }

    @Test
    void rejectMatch_throwsSecurityException_forStranger() {
        when(noticeMatchRepository.findById(matchId)).thenReturn(Optional.of(pendingMatch()));
        when(noticeRepository.findById(lostNoticeId)).thenReturn(Optional.of(lostNotice(NoticeStatus.PENDING_MATCH)));
        when(noticeRepository.findById(seenNoticeId)).thenReturn(Optional.of(seenNotice(NoticeStatus.PENDING_MATCH)));

        assertThatThrownBy(() -> noticeMatchService.rejectMatch(matchId, strangerId))
                .isInstanceOf(SecurityException.class);
    }

    // --- getMatchesForNotice ---

    @Test
    void getMatchesForNotice_delegatesToRepository() {
        when(noticeMatchRepository.findByLostNoticeIdOrSeenNoticeId(lostNoticeId, lostNoticeId))
                .thenReturn(List.of(pendingMatch()));

        List<NoticeMatch> result = noticeMatchService.getMatchesForNotice(lostNoticeId);

        assertThat(result).hasSize(1);
        verify(noticeMatchRepository).findByLostNoticeIdOrSeenNoticeId(lostNoticeId, lostNoticeId);
    }
}