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

    private final NotificationRepository notificationRepository;

    public List<NotificationDTO> getUserNotifications(UUID userId) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public long countUnread(UUID userId) {
        return notificationRepository.countByUserId(userId);
    }

    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    private NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .message(notification.getMessage())
                .type(notification.getType())
                .channel(notification.getChannel())
                .createdAt(notification.getCreatedAt())
                .build();
    }

}