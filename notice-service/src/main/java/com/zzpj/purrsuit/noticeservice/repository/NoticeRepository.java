package com.zzpj.purrsuit.noticeservice.repository;

import com.zzpj.purrsuit.common.events.NoticeStatus;
import com.zzpj.purrsuit.noticeservice.domain.NoticeType;
import com.zzpj.purrsuit.noticeservice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, UUID> {

    List<Notice> findByTypeAndStatus(NoticeType type, NoticeStatus status);

    List<Notice> findByReportedByUserIdOrderByCreatedAtDesc(UUID reportedByUserId);
}
