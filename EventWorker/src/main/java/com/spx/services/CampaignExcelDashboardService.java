package com.spx.services;

import com.spx.dto.CampaignAggregatedDataDTO;
import com.spx.helper.HelperExcelStylesheet;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Service responsible for generating the Excel dashboard workbook,
// including the summary sheet and the related chart sheets.
@Service
@Slf4j
public class CampaignExcelDashboardService {

    // Constants

    // For saving file on disk
    private static final Path REPORTS_DIRECTORY = Path.of("build", "reports");
    private static final DateTimeFormatter FILE_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final ExcelGeneratorService excelGeneratorService;
    private final ChartGeneratorService chartGeneratorService;

    public CampaignExcelDashboardService(ExcelGeneratorService excelGeneratorService, ChartGeneratorService chartGeneratorService) {

        this.excelGeneratorService = excelGeneratorService;
        this.chartGeneratorService = chartGeneratorService;
    }

    /**
     * Generates the dashboard workbook in memory.
     *
     * @param aggregatedRows the aggregated campaign data used by the dashboard
     * @return the generated Excel dashboard as a byte array
     */
    public byte[] generateDashboardWorkbook(List<CampaignAggregatedDataDTO> aggregatedRows) {

        log.info("Starting dashboard workbook generation. Aggregated row count: {}",
                aggregatedRows == null ? 0 : aggregatedRows.size());

        return excelGeneratorService.generateWorkbook(workbook -> {

            // Create reusable workbook-level styles only once.
            CellStyle headerStyle = HelperExcelStylesheet.createHeaderStyle(workbook);
            CellStyle bodyCellStyle = HelperExcelStylesheet.createCenteredValueStyle(workbook);

            // Always create the summary sheet first.
            createSummarySheet(workbook, aggregatedRows, headerStyle, bodyCellStyle);

            // Stop here if no dashboard data is available.
            if (aggregatedRows == null || aggregatedRows.isEmpty()) {
                log.info("No aggregated campaign data available. Generated summary sheet only.");
                return;
            }

            // Build the chart sheets using the aggregated rows directly.
            chartGeneratorService.createAttendanceOverviewSheet(workbook, aggregatedRows);
            chartGeneratorService.createCompositionSheet(workbook, aggregatedRows);
            chartGeneratorService.createAgeAnalysisSheet(workbook, aggregatedRows);
            chartGeneratorService.createDataCompletenessSheet(workbook, aggregatedRows);

            log.info("Dashboard workbook generated successfully.");
        });
    }

    /**
     * Generates the dashboard workbook and saves it under build/reports.
     *
     * @param aggregatedRows the aggregated campaign data used by the dashboard
     * @return the saved file path
     */
    public Path saveDashboardWorkbookToDisk(List<CampaignAggregatedDataDTO> aggregatedRows) {
        byte[] workbookBytes = generateDashboardWorkbook(aggregatedRows);

        try {
            Files.createDirectories(REPORTS_DIRECTORY);

            Path outputPath = REPORTS_DIRECTORY.resolve("campaign-dashboard-" + LocalDateTime.now().format(FILE_TIMESTAMP_FORMAT) + ".xlsx");

            Files.write(outputPath, workbookBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            log.info("Dashboard workbook saved to disk: {}", outputPath.toAbsolutePath());
            return outputPath;

        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save dashboard workbook to disk.", exception);
        }
    }

    /**
     * Creates the summary worksheet containing one aggregated row for each campaign.
     *
     * @param workbook the target workbook
     * @param aggregatedRows the aggregated campaign data used for the summary
     * @param headerStyle the style used for table headers
     * @param bodyCellStyle the style used for normal data cells
     * @return the generated summary sheet
     */
    private XSSFSheet createSummarySheet(XSSFWorkbook workbook, List<CampaignAggregatedDataDTO> aggregatedRows,
                                          CellStyle headerStyle, CellStyle bodyCellStyle) {

        XSSFSheet sheet = workbook.createSheet("Summary");
        HelperExcelStylesheet.applyDefaultSheetLayout(sheet);

        int rowIndex = 0;

        if (aggregatedRows == null || aggregatedRows.isEmpty()) {
            Row emptyRow = sheet.createRow(rowIndex);
            HelperExcelStylesheet.createCell(emptyRow, 0, "No campaign summary data available.", bodyCellStyle);

            HelperExcelStylesheet.applyColumnSizing(sheet, HelperExcelStylesheet.SUMMARY_SHEET_WIDTHS);
            return sheet;
        }

        Row headerRow = sheet.createRow(rowIndex++);
        headerRow.setHeightInPoints(22);

        HelperExcelStylesheet.createCell(headerRow, 0, "Campaign", headerStyle);
        HelperExcelStylesheet.createCell(headerRow, 1, "Attendees", headerStyle);
        HelperExcelStylesheet.createCell(headerRow, 2, "Main Attendees", headerStyle);
        HelperExcelStylesheet.createCell(headerRow, 3, "Companions", headerStyle);
        HelperExcelStylesheet.createCell(headerRow, 4, "Main Attendee Rate %", headerStyle);
        HelperExcelStylesheet.createCell(headerRow, 5, "Companion Rate %", headerStyle);
        HelperExcelStylesheet.createCell(headerRow, 6, "Average Age", headerStyle);
        HelperExcelStylesheet.createCell(headerRow, 7, "Young (<=29)", headerStyle);
        HelperExcelStylesheet.createCell(headerRow, 8, "Adult (30-49)", headerStyle);
        HelperExcelStylesheet.createCell(headerRow, 9, "Senior (50+)", headerStyle);
        HelperExcelStylesheet.createCell(headerRow, 10, "Missing Birth Date", headerStyle);
        HelperExcelStylesheet.createCell(headerRow, 11, "Missing CN", headerStyle);
        HelperExcelStylesheet.createCell(headerRow, 12, "Has Sub-Campaign", headerStyle);
        HelperExcelStylesheet.createCell(headerRow, 13, "Data Completeness %", headerStyle);

        for (CampaignAggregatedDataDTO aggregatedRow : aggregatedRows) {
            Row row = sheet.createRow(rowIndex++);

            HelperExcelStylesheet.createCell(row, 0,  HelperExcelStylesheet.defaultString(aggregatedRow.campaignDisplayName()), bodyCellStyle);
            HelperExcelStylesheet.createCell(row, 1, aggregatedRow.attendeeCount(), bodyCellStyle);
            HelperExcelStylesheet.createCell(row, 2, aggregatedRow.mainAttendeeCount(), bodyCellStyle);
            HelperExcelStylesheet.createCell(row, 3, aggregatedRow.companionCount(), bodyCellStyle);
            HelperExcelStylesheet.createCell(row, 4, aggregatedRow.mainAttendeeRate(), bodyCellStyle);
            HelperExcelStylesheet.createCell(row, 5, aggregatedRow.companionRate(), bodyCellStyle);
            HelperExcelStylesheet.createCell(row, 6, aggregatedRow.averageAge(), bodyCellStyle);
            HelperExcelStylesheet.createCell(row, 7, aggregatedRow.youngAttendeeCount(), bodyCellStyle);
            HelperExcelStylesheet.createCell(row, 8, aggregatedRow.adultAttendeeCount(), bodyCellStyle);
            HelperExcelStylesheet.createCell(row, 9, aggregatedRow.seniorAttendeeCount(), bodyCellStyle);
            HelperExcelStylesheet.createCell(row, 10, aggregatedRow.missingBirthDateCount(), bodyCellStyle);
            HelperExcelStylesheet.createCell(row, 11, aggregatedRow.missingCnCount(), bodyCellStyle);
            HelperExcelStylesheet.createCell(row, 12, aggregatedRow.hasSubCampaign() ? "Yes" : "No", bodyCellStyle);
            HelperExcelStylesheet.createCell(row, 13, aggregatedRow.dataCompletenessRate(), bodyCellStyle);
        }

        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 13));
        HelperExcelStylesheet.applyColumnSizing(sheet, HelperExcelStylesheet.SUMMARY_SHEET_WIDTHS);

        return sheet;
    }
}