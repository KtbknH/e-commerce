package com.yoteh.api.service.impl;

import com.yoteh.api.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${yoteh.mail.from:noreply@yoteh.com}")
    private String fromAddress;

    @Value("${yoteh.mail.from-name:Yoteh}")
    private String fromName;

    // ─────────────────────────────────────────────────────────────
    //  EMAIL SIMPLE (texte brut)
    // ─────────────────────────────────────────────────────────────

    @Override
    @Async("emailTaskExecutor")
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromName + " <" + fromAddress + ">");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Email simple envoyé à {} — sujet : {}", to, subject);
        } catch (Exception e) {
            log.error("Échec envoi email simple à {} : {}", to, e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  EMAIL HTML (template Thymeleaf)
    // ─────────────────────────────────────────────────────────────

    @Override
    @Async("emailTaskExecutor")
    public void sendHtmlEmail(
            String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            // Construire le contexte Thymeleaf
            Context context = new Context();
            if (variables != null) {
                variables.forEach(context::setVariable);
            }

            // Rendre le template HTML
            String htmlContent = templateEngine.process("email/" + templateName, context);

            // Construire le MimeMessage
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info(
                    "Email HTML envoyé à {} — template : {} — sujet : {}",
                    to,
                    templateName,
                    subject);
        } catch (MessagingException e) {
            log.error(
                    "Échec envoi email HTML à {} (template {}) : {}",
                    to,
                    templateName,
                    e.getMessage(),
                    e);
        } catch (Exception e) {
            log.error("Erreur inattendue envoi email à {} : {}", to, e.getMessage(), e);
        }
    }
}
