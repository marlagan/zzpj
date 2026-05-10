package com.zzpj.purrsuit.noticeservice.repository;

import com.zzpj.purrsuit.noticeservice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findByPetId(Long petId);
}