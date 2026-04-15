package com.spx.services;

import com.spx.config.ReportEmailProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// Responsible for sending report-related emails
@Service
@Slf4j
public class ReportEmailService {

    // Constants
    private final JavaMailSender mailSender;
    private final ReportEmailProperties reportEmailProperties;

    // Constructor
    public ReportEmailService(JavaMailSender mailSender, ReportEmailProperties reportEmailProperties) {
        this.mailSender = mailSender;
        this.reportEmailProperties = reportEmailProperties;
    }

    // Sends a plain text test email to verify SMTP configuration
    public void sendTestEmail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(reportEmailProperties.getFrom());
        message.setTo(reportEmailProperties.getTo());
        message.setSubject(reportEmailProperties.getSubject());
        message.setText("""
                Hello,

                this is a test email sent by the Event Worker application.

                If you can read this message in MailHog, the SMTP configuration is working correctly.

                Regards,
                Event Worker
                """);

        try {
            mailSender.send(message);
            log.info("Test email sent successfully to {}", reportEmailProperties.getTo());
        } catch (Exception exception) {
            log.error("Failed to send test email to {}", reportEmailProperties.getTo(), exception);
            throw new IllegalStateException("Unable to send test email.", exception);
        }
    }
}

