package com.spx.reporting;

import com.spx.services.ReportEmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Runs an email test when the application starts
@Component
@Slf4j
public class EmailTest implements ApplicationRunner {

    // Constants
    private final ReportEmailService reportEmailService;

    // Constructor
    public EmailTest(ReportEmailService reportEmailService) {
        this.reportEmailService = reportEmailService;
    }

    @Override
    public void run(ApplicationArguments args) {

        log.info("Starting email test.");

        try {

            // Send the email to MailHog server
            reportEmailService.sendTestEmail();
            log.info("Email sent successfully");

        } catch (RuntimeException exception) {

            log.error("Failed to send the email to the SMTP server.", exception);
            throw new IllegalStateException("Unable to send the email.", exception);
        }
    }
}

