package com.spx.services;

import com.spx.dto.CampaignAggregatedDataDTO;
import com.spx.helper.HelperExcelStylesheet;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.stereotype.Service;


import java.util.List;

// Service responsible for generating the Excel dashboard workbook (= summary sheet + chart sheets).
@Service
public class CampaignExcelDashboardReportService {

    // Constants
    private final ExcelSheetGeneratorService excelSheetGeneratorService;
    private final DashboardChartBuilderService dashboardChartBuilderService;

    // Constructor
    public CampaignExcelDashboardReportService(ExcelSheetGeneratorService excelSheetGeneratorService, DashboardChartBuilderService dashboardChartBuilderService) {
        this.excelSheetGeneratorService = excelSheetGeneratorService;
        this.dashboardChartBuilderService = dashboardChartBuilderService;
    }

    /**
     * Generates the dashboard workbook in memory.
     *
     * @param aggregateRows the aggregated campaign data used by the dashboard
     * @return the generated Excel dashboard as a byte array
     */
    public byte[] generateDashboardWorkbook(List<CampaignAggregatedDataDTO> aggregateRows) {

        return excelSheetGeneratorService.generateWorkbook(workbook -> {

            // Cells style
            CellStyle headerStyle = HelperExcelStylesheet.createHeaderStyle(workbook);
            CellStyle centeredValueStyle = HelperExcelStylesheet.createCenteredValueStyle(workbook);

            // Create summary sheet
            XSSFSheet summarySheet = createSummarySheet(workbook, aggregateRows, headerStyle, centeredValueStyle);

            // Create the chart sheets
            dashboardChartBuilderService.createAttendanceOverviewSheet(workbook, summarySheet, aggregateRows);
            dashboardChartBuilderService.createCompositionSheet(workbook, summarySheet, aggregateRows);
            dashboardChartBuilderService.createAgeAnalysisSheet(workbook, summarySheet, aggregateRows);
            dashboardChartBuilderService.createDataQualitySheet(workbook, summarySheet, aggregateRows);
        });
    }

    /**
     * Creates the summary worksheet containing one aggregated row for each campaign.
     *
     * @param workbook the target workbook
     * @param aggregateRows the aggregated campaign data used for the summary
     * @param headerStyle the style used for table headers
     * @param centeredValueStyle the style used for centered values
     * @return the generated summary sheet
     */
    private XSSFSheet createSummarySheet(org.apache.poi.xssf.usermodel.XSSFWorkbook workbook,
                                                 List<CampaignAggregatedDataDTO> aggregateRows, CellStyle headerStyle,
                                                 CellStyle centeredValueStyle) {

        // Create Summary sheet with its layout
        XSSFSheet sheet = workbook.createSheet("Summary");
        HelperExcelStylesheet.applyDefaultSheetLayout(sheet);

        int rowIndex = 0;

        if (aggregateRows == null || aggregateRows.isEmpty()) {
            Row emptyRow = sheet.createRow(rowIndex);
            HelperExcelStylesheet.createCell(emptyRow, 0, "No campaign summary data available.", centeredValueStyle);

            HelperExcelStylesheet.applyColumnSizing(sheet, HelperExcelStylesheet.SUMMARY_SHEET_WIDTHS);
            return sheet;

        } else {
            Row headerRow = sheet.createRow(rowIndex++);
            headerRow.setHeightInPoints(22);

            // Headers
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
        }

        // Values
        for (CampaignAggregatedDataDTO aggregateRow : aggregateRows) {
            Row row = sheet.createRow(rowIndex++);

            HelperExcelStylesheet.createCell(row, 0, aggregateRow.campaignDisplayName(), centeredValueStyle);
            HelperExcelStylesheet.createCell(row, 1, aggregateRow.attendeeCount(), centeredValueStyle);
            HelperExcelStylesheet.createCell(row, 2, aggregateRow.mainAttendeeCount(), centeredValueStyle);
            HelperExcelStylesheet.createCell(row, 3, aggregateRow.companionCount(), centeredValueStyle);
            HelperExcelStylesheet.createCell(row, 4, aggregateRow.mainAttendeeRate(), centeredValueStyle);
            HelperExcelStylesheet.createCell(row, 5, aggregateRow.companionRate(), centeredValueStyle);
            HelperExcelStylesheet.createCell(row, 6, aggregateRow.averageAge(), centeredValueStyle);
            HelperExcelStylesheet.createCell(row, 7, aggregateRow.youngAttendeeCount(), centeredValueStyle);
            HelperExcelStylesheet.createCell(row, 8, aggregateRow.adultAttendeeCount(), centeredValueStyle);
            HelperExcelStylesheet.createCell(row, 9, aggregateRow.seniorAttendeeCount(), centeredValueStyle);
            HelperExcelStylesheet.createCell(row, 10, aggregateRow.missingBirthDateCount(), centeredValueStyle);
            HelperExcelStylesheet.createCell(row, 11, aggregateRow.missingCnCount(), centeredValueStyle);
            HelperExcelStylesheet.createCell(row, 12, aggregateRow.hasSubCampaign() ? "Yes" : "No", centeredValueStyle);
            HelperExcelStylesheet.createCell(row, 13, aggregateRow.dataCompletenessRate(), centeredValueStyle);
        }

        // Column size
        HelperExcelStylesheet.applyColumnSizing(sheet, HelperExcelStylesheet.SUMMARY_SHEET_WIDTHS);
        return sheet;
    }
}