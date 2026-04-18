package com.spx.services;

import com.spx.config.ReportEmailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

    // Sends the generated Excel reports as email attachments.
    public void sendReportEmailWithAttachments(byte[] detailReportContent, byte[] dashboardReportContent) {

        try {

            // Create a new email message + helper object
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            // Set emails, subject and text of the email
            helper.setFrom(reportEmailProperties.getFrom());
            helper.setTo(reportEmailProperties.getTo());
            helper.setSubject(reportEmailProperties.getSubject());
            helper.setText("Hi, this is a new email. Please find attached the generated Excel reports.");

            log.info("Preparing report email for {}", reportEmailProperties.getTo());

            // Attach the detail workbook if content is available
            if (detailReportContent != null && detailReportContent.length > 0) {
                helper.addAttachment("campaign-detail-report.xlsx", new ByteArrayResource(detailReportContent));

                log.info("Attached detail report to email.");

            } else {
                log.warn("Detail report attachment skipped because content is empty.");
            }

            // Attach the dashboard workbook if content is available
            if (dashboardReportContent != null && dashboardReportContent.length > 0) {
                helper.addAttachment("campaign-dashboard-report.xlsx", new ByteArrayResource(dashboardReportContent)
                );

                log.info("Attached dashboard report to email.");

            } else {

                log.warn("Dashboard report attachment skipped because content is empty.");
            }

            // Send the email
            mailSender.send(mimeMessage);
            log.info("Report email sent successfully to {}.", reportEmailProperties.getTo());

        } catch (MessagingException ex) {

            log.error("Failed to send report email with attachments.", ex);
            throw new IllegalStateException("Failed to send report email with attachments.", ex);
        }
    }
}