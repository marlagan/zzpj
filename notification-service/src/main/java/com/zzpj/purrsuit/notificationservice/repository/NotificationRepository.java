package com.zzpj.purrsuit.notificationservice.repository;

import com.zzpj.purrsuit.notificationservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    //List<Notification> findByUserId(UUID userId);
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
    //List<Notification> findByUSerIdOrderByCreatedAtAfter(UUID userId, LocalDateTime date);

    long countByUserId(UUID userId);
}
//Todo: modifying? status?
