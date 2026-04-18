package com.spx.services;

import com.spx.dto.CampaignAggregatedDataDTO;
import com.spx.dto.CampaignReportDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

// Coordinates report generation and email delivery.
@Service
@Slf4j
public class ReportDispatchService {

    // Constants
    private final CampaignRetrievalService campaignRetrievalService;
    private final CampaignAggregatedDataService campaignAggregatedDataService;
    private final CampaignExcelDetailService campaignExcelDetailService;
    private final CampaignExcelDashboardService campaignExcelDashboardService;
    private final ReportEmailService reportEmailService;

    // Constructor
    public ReportDispatchService(CampaignRetrievalService campaignRetrievalService, CampaignAggregatedDataService campaignAggregatedDataService,
                                 CampaignExcelDetailService campaignExcelDetailService, CampaignExcelDashboardService campaignExcelDashboardService, ReportEmailService reportEmailService) {

        this.campaignRetrievalService = campaignRetrievalService;
        this.campaignAggregatedDataService = campaignAggregatedDataService;
        this.campaignExcelDetailService = campaignExcelDetailService;
        this.campaignExcelDashboardService = campaignExcelDashboardService;
        this.reportEmailService = reportEmailService;
    }

    // Generates the Excel reports and sends them by email.
    public void dispatchReports() {

        log.info("Report dispatch started.");

        // Step 1: Load the report-ready campaign sections from the database
        List<CampaignReportDTO> campaignSections = campaignRetrievalService.getAllCampaignsForReport();

        // Step 2: Build aggregated dashboard rows starting from the campaign sections
        List<CampaignAggregatedDataDTO> aggregateRows = campaignAggregatedDataService.getAllCampaignsAggregatedData(campaignSections);

        // Step 3: Generate both Excel reports in memory as byte arrays
        byte[] detailReportContent = campaignExcelDetailService.generateCampaignDetailWorkbook(campaignSections);

        byte[] dashboardReportContent = campaignExcelDashboardService.generateDashboardWorkbook(aggregateRows);

        // Step 4: Send the generated reports as email attachments
        reportEmailService.sendReportEmailWithAttachments(detailReportContent, dashboardReportContent);

        log.info("Report dispatch completed successfully.");
    }
}