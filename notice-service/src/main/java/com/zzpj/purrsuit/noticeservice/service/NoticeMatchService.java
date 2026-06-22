package com.zzpj.purrsuit.noticeservice.service;

import com.zzpj.purrsuit.common.events.MatchResultEvent;
import com.zzpj.purrsuit.noticeservice.domain.MatchStatus;
import com.zzpj.purrsuit.common.events.NoticeStatus;
import com.zzpj.purrsuit.noticeservice.entity.Notice;
import com.zzpj.purrsuit.noticeservice.entity.NoticeMatch;
import com.zzpj.purrsuit.noticeservice.kafka.MatchDecisionProducer;
import com.zzpj.purrsuit.noticeservice.repository.NoticeMatchRepository;
import com.zzpj.purrsuit.noticeservice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zzpj.purrsuit.noticeservice.kafka.NoticeUpdateProducer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeMatchService {

    private final NoticeMatchRepository noticeMatchRepository;
    private final NoticeRepository noticeRepository;
    private final MatchDecisionProducer matchDecisionProducer;
    private final NoticeUpdateProducer noticeUpdateProducer;

    /**
     * Wywoływane przez MatchResultKafkaListener po odebraniu MatchResultEvent
     * z pet-service. Tworzy nową encję NoticeMatch (jeśli jeszcze nie istnieje)
     * i przeprowadza oba powiązane zgłoszenia w status PENDING_MATCH.
     */
    @Transactional
    public void handleMatchFound(MatchResultEvent event) {
        if (noticeMatchRepository
                .findByLostNoticeIdAndSeenNoticeId(event.lostNoticeId(), event.seenNoticeId())
                .isPresent()) {
            log.info("Dopasowanie {} <-> {} już istnieje w notice-service. Pomijam.",
                    event.lostNoticeId(), event.seenNoticeId());
            return;
        }

        NoticeMatch match = NoticeMatch.builder()
                .lostNoticeId(event.lostNoticeId())
                .seenNoticeId(event.seenNoticeId())
                .lostOwnerId(event.userId())
                .similarityScore(event.similarityScore())
                .status(MatchStatus.PENDING)
                .build();
        noticeMatchRepository.save(match);
        log.info("Utworzono NoticeMatch id={} dla lost={} seen={}",
                match.getId(), match.getLostNoticeId(), match.getSeenNoticeId());

        markAsPendingMatch(event.lostNoticeId());
        markAsPendingMatch(event.seenNoticeId());
    }

    /**
     * Potwierdza odnalezienie zwierzęcia: oba zgłoszenia przechodzą w RESOLVED,
     * a map-service oraz pet-service są o tym informowane przez Kafkę.
     */
    @Transactional
    public NoticeMatch confirmMatch(UUID matchId, UUID requestingUserId) {
        NoticeMatch match = getPendingMatchOrThrow(matchId);
        assertRequesterIsParty(match, requestingUserId);

        match.setStatus(MatchStatus.CONFIRMED);
        match.setDecidedAt(LocalDateTime.now());
        noticeMatchRepository.save(match);

        setNoticeStatus(match.getLostNoticeId(), NoticeStatus.RESOLVED);
        setNoticeStatus(match.getSeenNoticeId(), NoticeStatus.RESOLVED);

        noticeUpdateProducer.sendNoticeStatusUpdate(match.getLostNoticeId(), NoticeStatus.RESOLVED);
        noticeUpdateProducer.sendNoticeStatusUpdate(match.getSeenNoticeId(), NoticeStatus.RESOLVED);

        matchDecisionProducer.notifyMatchConfirmed(match);
        log.info("Dopasowanie {} potwierdzone przez użytkownika {}", matchId, requestingUserId);
        return match;
    }

    /**
     * Odrzuca dopasowanie: oba zgłoszenia wracają do ACTIVE (wyszukiwanie trwa dalej),
     * a map-service oraz pet-service są o tym informowane przez Kafkę.
     */
    @Transactional
    public NoticeMatch rejectMatch(UUID matchId, UUID requestingUserId) {
        NoticeMatch match = getPendingMatchOrThrow(matchId);
        assertRequesterIsParty(match, requestingUserId);

        match.setStatus(MatchStatus.REJECTED);
        match.setDecidedAt(LocalDateTime.now());
        noticeMatchRepository.save(match);

        setNoticeStatus(match.getLostNoticeId(), NoticeStatus.ACTIVE);
        setNoticeStatus(match.getSeenNoticeId(), NoticeStatus.ACTIVE);

        noticeUpdateProducer.sendNoticeStatusUpdate(match.getLostNoticeId(), NoticeStatus.ACTIVE);
        noticeUpdateProducer.sendNoticeStatusUpdate(match.getSeenNoticeId(), NoticeStatus.ACTIVE);

        matchDecisionProducer.notifyMatchRejected(match);
        log.info("Dopasowanie {} odrzucone przez użytkownika {}", matchId, requestingUserId);
        return match;
    }

    @Transactional(readOnly = true)
    public List<NoticeMatch> getMatchesForNotice(UUID noticeId) {
        return noticeMatchRepository.findByLostNoticeIdOrSeenNoticeId(noticeId, noticeId);
    }

    private void markAsPendingMatch(UUID noticeId) {
        noticeRepository.findById(noticeId).ifPresentOrElse(notice -> {
            if (notice.getStatus() == NoticeStatus.ACTIVE) {
                notice.setStatus(NoticeStatus.PENDING_MATCH);
                noticeRepository.save(notice);
            }
        }, () -> log.warn("Zgłoszenie {} nie istnieje w notice-service — pomijam zmianę statusu", noticeId));
    }

    private void setNoticeStatus(UUID noticeId, NoticeStatus status) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoSuchElementException("Notice not found: " + noticeId));
        notice.setStatus(status);
        noticeRepository.save(notice);
    }

    private NoticeMatch getPendingMatchOrThrow(UUID matchId) {
        NoticeMatch match = noticeMatchRepository.findById(matchId)
                .orElseThrow(() -> new NoSuchElementException("Match not found: " + matchId));
        if (match.getStatus() != MatchStatus.PENDING) {
            throw new IllegalStateException("Dopasowanie zostało już rozstrzygnięte: " + matchId);
        }
        return match;
    }

    /**
     * Decyzję o dopasowaniu może podjąć właściciel zgłoszenia LOST
     * lub osoba, która zgłosiła znalezienie zwierzęcia (FOUND) —
     * sprawdzane na podstawie rzeczywistych reportedByUserId obu zgłoszeń,
     * a nie tylko denormalizowanego pola NoticeMatch#lostOwnerId.
     */
    private void assertRequesterIsParty(NoticeMatch match, UUID requestingUserId) {
        boolean isLostOwner = noticeRepository.findById(match.getLostNoticeId())
                .map(n -> n.getReportedByUserId().equals(requestingUserId))
                .orElse(false);
        boolean isSeenReporter = noticeRepository.findById(match.getSeenNoticeId())
                .map(n -> n.getReportedByUserId().equals(requestingUserId))
                .orElse(false);

        if (!isLostOwner && !isSeenReporter) {
            throw new SecurityException(
                    "Tylko strona zgłoszenia może potwierdzić lub odrzucić dopasowanie: " + match.getId());
        }
    }
}
