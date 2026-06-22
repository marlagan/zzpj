import com.zzpj.purrsuit.notificationservice.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendEmail_ShouldSendSimpleMessageSuccessfully() {
        assertDoesNotThrow(() ->
            emailService.sendEmail("user@example.com", "Test Subject", "Hello World")
        );
    }

    @Test
    void sendTemplatedEmail_ShouldProcessTemplateAndLog() {
        String to = "user@example.com";
        String subject = "Welcome!";
        String templateName = "welcome-template";
        Map<String, Object> variables = Map.of("name", "Jan");
        String expectedHtml = "<h1>Hello Jan</h1>";

        when(templateEngine.process(eq(templateName), any(Context.class))).thenReturn(expectedHtml);

        assertDoesNotThrow(() ->
            emailService.sendTemplatedEmail(to, subject, templateName, variables)
        );

        verify(templateEngine, times(1)).process(eq(templateName), any(Context.class));
    }

    @Test
    void sendTemplatedEmail_ShouldHandleExceptionGracefully() {
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenThrow(new RuntimeException("Template error"));

        assertDoesNotThrow(() ->
                emailService.sendTemplatedEmail("fail@test.com", "Sub", "temp", Map.of())
        );
    }
}