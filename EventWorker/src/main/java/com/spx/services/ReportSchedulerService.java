package com.spx.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Triggers periodic report generation and email delivery
@Component
@Slf4j
public class ReportSchedulerService {

    // Constants
    private final ReportDispatchService reportDispatchService;

    // Constructor
    public ReportSchedulerService(ReportDispatchService reportDispatchService) {
        this.reportDispatchService = reportDispatchService;
    }

    @Scheduled(cron = "${app.report.schedule}")
    public void runScheduledReportDispatch() {

        log.info("Scheduled report job started.");
        reportDispatchService.dispatchReports();
        log.info("Scheduled report job completed.");

    }
}