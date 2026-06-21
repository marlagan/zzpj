package com.zzpj.purrsuit.petservice.repository;

import com.zzpj.purrsuit.petservice.entity.MatchResult;
import com.zzpj.purrsuit.petservice.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interfejs dostępu do bazy danych dla wyników dopasowań.
 */
@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, UUID> {

    /**
     * Pobiera wszystkie dopasowania dla konkretnego ogłoszenia o zgubie.
     */
    List<MatchResult> findByLostNoticeId(UUID lostNoticeId);

    /**
     * Pobiera dopasowania dla ogłoszenia o zgubie z określonym statusem weryfikacji.
     */
    List<MatchResult> findByLostNoticeIdAndStatus(UUID lostNoticeId, MatchStatus status);

    /**
     * Wyszukuje konkretne dopasowanie między zgubionym a odnalezionym zwierzęciem.
     */
    Optional<MatchResult> findByLostNoticeIdAndSeenNoticeId(UUID lostNoticeId, UUID seenNoticeId);
}
