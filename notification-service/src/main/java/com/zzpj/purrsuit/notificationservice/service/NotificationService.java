package com.zzpj.purrsuit.notificationservice.service;

import com.zzpj.purrsuit.notificationservice.dto.NotificationDTO;
import com.zzpj.purrsuit.notificationservice.entity.Notification;
import com.zzpj.purrsuit.notificationservice.enums.NotificationStatus;
import com.zzpj.purrsuit.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public List<NotificationDTO> getUserNotificationsByStatus(UUID userId, NotificationStatus status) {
        return notificationRepository
                .findByUserIdAndStatus(userId, status)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public long countUnread(UUID userId) {
        return notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.PENDING);
    }

    public Notification save(Notification notification) {
        Notification saved = notificationRepository.save(notification);

        // wyślij powiadomienie przez websocket (to user x)
        NotificationDTO dto = toDTO(saved);
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + saved.getUserId(),
                dto
        );

        return saved;
    }

    private NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .channel(notification.getChannel())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public void acceptNotification(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setStatus(NotificationStatus.READ);
        notificationRepository.save(notification);
    }

    public List<NotificationDTO> getUserNotifications(UUID userId) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}