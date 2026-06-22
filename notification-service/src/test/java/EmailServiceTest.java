import com.zzpj.purrsuit.notificationservice.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendEmail_ShouldSendSimpleMessageSuccessfully() {
        String to = "user@example.com";
        String subject = "Test Subject";
        String body = "Hello World";

        emailService.sendEmail(to, subject, body);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertArrayEquals(new String[]{to}, sentMessage.getTo());
        assertEquals(subject, sentMessage.getSubject());
        assertEquals(body, sentMessage.getText());
        assertEquals("noreply@purrsuit.com", sentMessage.getFrom());
    }

    @Test
    void sendEmail_ShouldLogAndNotThrow_WhenExceptionOccurs() {
        doThrow(new RuntimeException("Mail server down")).when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> emailService.sendEmail("fail@test.com", "Sub", "Body"));
    }

    @Test
    void sendTemplatedEmail_ShouldProcessTemplateAndSendMimeMessage() {
        String to = "user@example.com";
        String subject = "Welcome!";
        String templateName = "welcome-template";
        Map<String, Object> variables = Map.of("name", "Jan");
        String expectedHtml = "<h1>Hello Jan</h1>";

        MimeMessage mockMimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(templateEngine.process(eq(templateName), any(Context.class))).thenReturn(expectedHtml);

        emailService.sendTemplatedEmail(to, subject, templateName, variables);

        verify(templateEngine, times(1)).process(eq(templateName), any(Context.class));
        verify(mailSender, times(1)).send(mockMimeMessage);
    }

    @Test
    void sendTemplatedEmail_ShouldHandleExceptionGracefully() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mime creation error"));

        assertDoesNotThrow(() ->
                emailService.sendTemplatedEmail("fail@test.com", "Sub", "temp", Map.of())
        );
    }
}