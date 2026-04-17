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

import java.util.List;

// Service responsible for generating the Excel dashboard workbook (summary sheet + charts)
@Service
@Slf4j
public class CampaignExcelDashboardService {

    // Constants
    private final ExcelGeneratorService excelGeneratorService;
    private final DashboardChartBuilderService dashboardChartBuilderService;

    // Constructor
    public CampaignExcelDashboardService(ExcelGeneratorService excelGeneratorService, DashboardChartBuilderService dashboardChartBuilderService) {
        this.excelGeneratorService = excelGeneratorService;
        this.dashboardChartBuilderService = dashboardChartBuilderService;
    }

    /**
     * Generates the dashboard workbook in memory.
     *
     * @param aggregatedRows the aggregated campaign data used by the dashboard
     * @return the generated Excel dashboard as a byte array
     */
    public byte[] generateDashboardWorkbook(List<CampaignAggregatedDataDTO> aggregatedRows) {

        log.info("Starting dashboard workbook generation. Aggregated row count: {}", aggregatedRows == null ? 0 : aggregatedRows.size());

        return excelGeneratorService.generateWorkbook(workbook -> {

            // Create reusable workbook styles
            CellStyle headerStyle = HelperExcelStylesheet.createHeaderStyle(workbook);
            CellStyle bodyCellStyle = HelperExcelStylesheet.createCenteredValueStyle(workbook);

            // Always create the summary sheet first because chart sheets depend on it
            XSSFSheet summarySheet = createSummarySheet(workbook, aggregatedRows, headerStyle, bodyCellStyle);

            // If there is no aggregated data, stop after creating the empty summary sheet
            if (aggregatedRows == null || aggregatedRows.isEmpty()) {
                log.info("No aggregated campaign data available. Generated dashboard workbook with summary sheet only.");
                return;
            }

            // Create dashboard chart sheets based on the summary data.
            dashboardChartBuilderService.createAttendanceOverviewSheet(workbook, summarySheet, aggregatedRows);
            dashboardChartBuilderService.createCompositionSheet(workbook, summarySheet, aggregatedRows);
            dashboardChartBuilderService.createAgeAnalysisSheet(workbook, summarySheet, aggregatedRows);
            dashboardChartBuilderService.createDataQualitySheet(workbook, summarySheet, aggregatedRows);

            log.info("Dashboard workbook generated successfully.");
        });
    }

    /**
     * Creates the summary worksheet containing one aggregated row for each campaign.
     *
     * @param workbook       the target workbook
     * @param aggregatedRows the aggregated campaign data used for the summary
     * @param headerStyle    the style used for table headers
     * @param bodyCellStyle  the style used for normal data cells
     * @return the generated summary sheet
     */
    private XSSFSheet createSummarySheet(XSSFWorkbook workbook, List<CampaignAggregatedDataDTO> aggregatedRows,
                                         CellStyle headerStyle, CellStyle bodyCellStyle) {

        // Create the summary sheet and apply the standard report layout.
        XSSFSheet sheet = workbook.createSheet("Summary");
        HelperExcelStylesheet.applyDefaultSheetLayout(sheet);

        int rowIndex = 0;

        // If no aggregated data is available, write a fallback message and return the sheet.
        if (aggregatedRows == null || aggregatedRows.isEmpty()) {
            Row emptyRow = sheet.createRow(rowIndex);
            HelperExcelStylesheet.createCell(emptyRow, 0, "No campaign summary data available.", bodyCellStyle);

            HelperExcelStylesheet.applyColumnSizing(sheet, HelperExcelStylesheet.SUMMARY_SHEET_WIDTHS);
            return sheet;
        }

        // Write the summary table header.
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

        // Write one summary row for each campaign.
        for (CampaignAggregatedDataDTO aggregatedRow : aggregatedRows) {
            Row row = sheet.createRow(rowIndex++);

            HelperExcelStylesheet.createCell(row, 0, HelperExcelStylesheet.defaultString(aggregatedRow.campaignDisplayName()), bodyCellStyle);
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

        // Keep the header visible while scrolling and enable filtering on the summary table.
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 13));

        // Apply fixed column widths for consistent readability.
        HelperExcelStylesheet.applyColumnSizing(sheet, HelperExcelStylesheet.SUMMARY_SHEET_WIDTHS);

        return sheet;
    }
}