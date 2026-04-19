package com.zzpj.purrsuit.notificationservice.service;

import com.zzpj.purrsuit.notificationservice.dto.NotificationDTO;
import com.zzpj.purrsuit.notificationservice.entity.Notification;
import com.zzpj.purrsuit.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private NotificationRepository notificationRepository;

    /*public List<NotificationDTO> getUserNotifications(UUID userId) {
        return notificationRepository
                .findByUSerIdOrderByCreatedAtDesc(userId)
                .stream()
                //.map(this::toDTO)
                //.collect(Collectors.toList());
    }*/

    public long countUnread(UUID userId) {
        return notificationRepository.countByUserId(userId);
    }

    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

// todo toDTO
}