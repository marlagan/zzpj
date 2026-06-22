package com.zzpj.purrsuit.noticeservice.repository;

import com.zzpj.purrsuit.noticeservice.entity.NoticeMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoticeMatchRepository extends JpaRepository<NoticeMatch, UUID> {

    Optional<NoticeMatch> findByLostNoticeIdAndSeenNoticeId(UUID lostNoticeId, UUID seenNoticeId);

    List<NoticeMatch> findByLostNoticeIdOrSeenNoticeId(UUID lostNoticeId, UUID seenNoticeId);
}
