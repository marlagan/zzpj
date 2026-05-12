package com.zzpj.purrsuit.notificationservice.dto;

import com.zzpj.purrsuit.notificationservice.enums.NotificationChannel;
import com.zzpj.purrsuit.notificationservice.enums.NotificationType;
import jdk.jshell.Snippet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private UUID id;
    private UUID userId;
    private String title;
    private String message;
    private NotificationType type;
    private NotificationChannel channel;
    private LocalDateTime createdAt;
}
