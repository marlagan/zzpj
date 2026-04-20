package com.zzpj.purrsuit.perservice.repository;

import com.zzpj.purrsuit.perservice.model.MatchResult;
import com.zzpj.purrsuit.perservice.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MatchResultRepository extends JpaRepository<MatchResult, UUID> {

    List<MatchResult> findByLostNotice(UUID lostNoticeId);
    List<MatchResult> findByLostNoticeIdAndStatus(UUID lostNoticeId, MatchStatus status);
}
