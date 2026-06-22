package com.zzpj.purrsuit.notificationservice.controller;

import com.zzpj.purrsuit.notificationservice.dto.NotificationDTO;
import com.zzpj.purrsuit.notificationservice.enums.NotificationStatus;
import com.zzpj.purrsuit.notificationservice.service.EmailService;
import com.zzpj.purrsuit.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final EmailService emailService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false) String status) {

        if (status != null) {
            NotificationStatus parsedStatus = NotificationStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(
                    notificationService.getUserNotificationsByStatus(userId, parsedStatus)
            );
        }
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(notificationService.countUnread(userId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> acceptNotification(@PathVariable UUID id) {
        notificationService.acceptNotification(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/test-email", produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> testEmail() {
        emailService.sendEmail(
                "test@test.com",
                "Test Purrsuit",
                "Działa!"
        );
        return ResponseEntity.ok("Email wysłany!");
    }
}