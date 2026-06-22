import com.zzpj.purrsuit.notificationservice.dto.NotificationDTO;
import com.zzpj.purrsuit.notificationservice.entity.Notification;
import com.zzpj.purrsuit.notificationservice.enums.NotificationChannel;
import com.zzpj.purrsuit.notificationservice.enums.NotificationStatus;
import com.zzpj.purrsuit.notificationservice.enums.NotificationType;
import com.zzpj.purrsuit.notificationservice.repository.NotificationRepository;
import com.zzpj.purrsuit.notificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    private final UUID userId = UUID.randomUUID();

    @Test
    void getUserNotifications_ShouldReturnMappedDtoList() {
        Notification notification = Notification.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .message("Twój pies się znalazł!")
                .type(NotificationType.MATCH_FOUND)
                .channel(NotificationChannel.EMAIL)
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(notification));

        List<NotificationDTO> result = notificationService.getUserNotifications(userId);

        assertNotNull(result);
        assertEquals(1, result.size());

        NotificationDTO dto = result.get(0);
        assertEquals(notification.getId(), dto.getId());
        assertEquals(notification.getUserId(), dto.getUserId());
        assertEquals(notification.getMessage(), dto.getMessage());
        assertEquals(notification.getType(), dto.getType());
        assertEquals(notification.getChannel(), dto.getChannel());
        assertEquals(notification.getCreatedAt(), dto.getCreatedAt());
    }

    @Test
    void getUserNotifications_ShouldReturnEmptyList_WhenNoNotificationsFound() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(Collections.emptyList());

        List<NotificationDTO> result = notificationService.getUserNotifications(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void countUnread_ShouldReturnCountFromRepository() {
        when(notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.PENDING)).thenReturn(5L);

        long unreadCount = notificationService.countUnread(userId);

        assertEquals(5L, unreadCount);
        verify(notificationRepository, times(1)).countByUserIdAndStatus(userId, NotificationStatus.PENDING);
    }

    @Test
    void save_ShouldPersistNotificationAndReturnIt() {
        UUID testUserId = UUID.randomUUID();

        Notification notification = Notification.builder()
                .userId(testUserId)
                .message("Test")
                .build();

        when(notificationRepository.save(notification)).thenReturn(notification);

        Notification saved = notificationService.save(notification);

        assertNotNull(saved);
        assertEquals("Test", saved.getMessage());
        verify(notificationRepository, times(1)).save(notification);
        verify(messagingTemplate, times(1)).convertAndSendToUser(
                eq(testUserId.toString()),
                eq("/queue/notifications/" + testUserId),
                any(NotificationDTO.class)
        );
    }
}