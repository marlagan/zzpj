package com.zzpj.purrsuit.noticeservice.kafka;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event publikowany przez notice-service po tym, jak użytkownik potwierdzi
 * lub odrzuci dopasowanie zaproponowane przez pet-service.
 *
 * Tematy: "match-confirmed-topic" / "match-rejected-topic" (patrz MatchDecisionProducer).
 *
 * Planowani konsumenci (poza zakresem tego zgłoszenia, własne moduły notice-service):
 *  - pet-service  — aktualizuje status swojego MatchResult i usuwa/zostawia
 *                   zwierzę w puli aktywnych kandydatów do dopasowań.
 *  - map-service  — usuwa lub dezaktywuje punkt lokalizacji powiązany
 *                   z zakończonym zgłoszeniem, aby nie pojawiał się
 *                   w kolejnych zapytaniach ST_DWithin.
 */
public record MatchDecisionEvent(
        UUID matchId,
        UUID lostNoticeId,
        UUID seenNoticeId,
        String decision, // "CONFIRMED" lub "REJECTED"
        LocalDateTime decidedAt
) {}
