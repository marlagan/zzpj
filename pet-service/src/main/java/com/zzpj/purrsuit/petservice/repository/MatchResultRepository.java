package com.zzpj.purrsuit.petservice.repository;

import com.zzpj.purrsuit.petservice.model.MatchResult;
import com.zzpj.purrsuit.petservice.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, UUID> {

    List<MatchResult> findByLostNoticeId(UUID lostNoticeId);
    List<MatchResult> findByLostNoticeIdAndStatus(UUID lostNoticeId, MatchStatus status);
    Optional<MatchResult> findByLostNoticeIdAndSeenNoticeId(UUID lostNoticeId, UUID seenNoticeId);
}
