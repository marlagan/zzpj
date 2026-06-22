import com.zzpj.purrsuit.notificationservice.controller.NotificationController;
import com.zzpj.purrsuit.notificationservice.dto.NotificationDTO;
import com.zzpj.purrsuit.notificationservice.enums.NotificationChannel;
import com.zzpj.purrsuit.notificationservice.enums.NotificationType;
import com.zzpj.purrsuit.notificationservice.service.EmailService;
import com.zzpj.purrsuit.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationController notificationController;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
    }

    @Test
    void getNotifications_ShouldReturnListAndStatus200_WhenHeaderIsPresent() throws Exception {
        NotificationDTO dto = NotificationDTO.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .message("Test message")
                .type(NotificationType.MATCH_FOUND)
                .channel(NotificationChannel.EMAIL)
                .build();

        when(notificationService.getUserNotifications(userId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/notifications")
                        .header("X-User-ID", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Test message"))
                .andExpect(jsonPath("$[0].type").value("MATCH_FOUND"));

        verify(notificationService, times(1)).getUserNotifications(userId);
    }

    @Test
    void getUnreadCount_ShouldReturnCountAndStatus200() throws Exception {
        when(notificationService.countUnread(userId)).thenReturn(3L);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("X-User-ID", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        verify(notificationService, times(1)).countUnread(userId);
    }

    @Test
    void testEmail_ShouldTriggerEmailServiceAndReturnSuccessMessage() throws Exception {
        mockMvc.perform(get("/api/notifications/test-email"))
                .andExpect(status().isOk())
                .andExpect(content().string("Email wysłany!"));

        verify(emailService, times(1)).sendEmail(
                eq("test@test.com"),
                eq("Test Purrsuit"),
                eq("Działa!")
        );
    }
}