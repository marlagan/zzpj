package com.zzpj.purrsuit.notificationservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final TemplateEngine templateEngine;

    public void sendEmail(String to, String subject, String body) {
        log.info("\n" +
                "================== SYMULACJA WYSYŁKI EMAIL ==================\n" +
                "Do: {}\n" +
                "Temat: {}\n" +
                "Treść:\n{}\n" +
                "=============================================================",
                to, subject, body);
    }

    public void sendTemplatedEmail(String to, String subject,
                                   String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process(templateName, context);

            log.info("\n" +
                    "============== SYMULACJA WYSYŁKI EMAIL (SZABLON) ==============\n" +
                    "Do: {}\n" +
                    "Temat: {}\n" +
                    "Szablon: {}\n" +
                    "Treść HTML:\n{}\n" +
                    "===============================================================",
                    to, subject, templateName, htmlContent);
            log.info("Templated email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send templated email to: {}", to, e);
        }
    }
}