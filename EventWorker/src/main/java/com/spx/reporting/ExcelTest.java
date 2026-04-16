package com.spx.reporting;

import com.spx.dto.CampaignAggregatedDataDTO;
import com.spx.dto.CampaignReportDTO;
import com.spx.services.CampaignAggregatedDataService;
import com.spx.services.CampaignExcelDetailService;
import com.spx.services.CampaignExcelDashboardReportService;
import com.spx.services.CampaignReportQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Generates a local Excel report at application startup for manual verification
@Component
@Slf4j
@ConditionalOnBooleanProperty(prefix = "app.report.excel.smoke-test", name = "enabled", havingValue = true, matchIfMissing = false)
public class ExcelTest implements ApplicationRunner {

    // Constants
    private final CampaignReportQueryService campaignReportQueryService;
    private final CampaignExcelDetailService campaignExcelDetailService;
    private final CampaignAggregatedDataService campaignAggregateDataService;
    private final CampaignExcelDashboardReportService campaignExcelDashboardReportService;

    // Constructor
    public ExcelTest(CampaignReportQueryService campaignReportQueryService,
                     CampaignExcelDetailService campaignExcelDetailService,
                     CampaignAggregatedDataService campaignAggregateDataService,
                     CampaignExcelDashboardReportService campaignExcelDashboardReportService) {

        this.campaignReportQueryService = campaignReportQueryService;
        this.campaignExcelDetailService = campaignExcelDetailService;
        this.campaignAggregateDataService = campaignAggregateDataService;
        this.campaignExcelDashboardReportService = campaignExcelDashboardReportService;
    }

    @Override
    public void run(ApplicationArguments args) {

        log.info("Starting Excel report test.");

        /* Retrieves the full list of campaigns to be included in the report from the specific service.
        Each CampaignReportDTO represents the detailed data of a single campaign. */
        List<CampaignReportDTO> campaignSections = campaignReportQueryService.getAllCampaignsForReport();

        /* Builds the aggregated dataset starting from the campaign detail rows from the specific service.
         This list is typically used for summary views, metrics, and dashboard charts. */
        List<CampaignAggregatedDataDTO> aggregateRows = campaignAggregateDataService.buildAggregateDataList(campaignSections);

        // Creates the detailed Excel report file, including one sheet for each campaign
        byte[] detailExcelReport = campaignExcelDetailService.generateReport(campaignSections);

        // Creates the dashboard Excel report file, containing aggregated data only
        byte[] dashboardExcelReport = campaignExcelDashboardReportService.generateDashboardReport(aggregateRows);

        // Path and Filename of the Excel files (\build\reports)
        Path outputDirectory = Path.of("build", "reports");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path detailOutputFile = outputDirectory.resolve("campaign-detail-report-" + timestamp + ".xlsx");
        Path dashboardOutputFile = outputDirectory.resolve("campaign-summary-dashboard-" + timestamp + ".xlsx");

        try {

            // Generate the Excel files
            Files.createDirectories(outputDirectory);
            Files.write(detailOutputFile, detailExcelReport);
            Files.write(dashboardOutputFile, dashboardExcelReport);

            log.info("Detail Excel report generated successfully at: {}", detailOutputFile.toAbsolutePath());
            log.info("Dashboard Excel report generated successfully at: {}", dashboardOutputFile.toAbsolutePath());

        } catch (IOException exception) {

            log.error("Failed to write Excel test reports to disk.", exception);
            throw new IllegalStateException("Unable to save Excel test reports.", exception);
        }

    }
}