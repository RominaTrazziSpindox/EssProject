package com.spx.services;

import com.spx.dto.CampaignAggregatedDataDTO;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Service responsible for generating the Excel dashboard workbook.
 * This workbook is separate from the detailed campaign report and contains
 * summary data plus dedicated sheets for charts.
 */
@Service
public class CampaignExcelDashboardReportService {

    /**
     * Generates the dashboard workbook in memory.
     *
     * @param aggregateRows the aggregated campaign data used by the dashboard
     * @return the generated Excel dashboard as a byte array
     */
    public byte[] generateDashboardReport(List<CampaignAggregatedDataDTO> aggregateRows) {
        try (Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle centeredValueStyle = createCenteredValueStyle(workbook);

            createSummarySheet(workbook, aggregateRows, headerStyle, centeredValueStyle);
            createPlaceholderSheet(workbook, "Attendance Overview");
            createPlaceholderSheet(workbook, "Composition");
            createPlaceholderSheet(workbook, "Age Analysis");
            createPlaceholderSheet(workbook, "Data Quality");

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException exception) {
            throw new IllegalStateException("Unable to generate dashboard Excel report.", exception);
        }
    }

    /**
     * Creates the summary worksheet containing one aggregated row for each campaign.
     *
     * @param workbook the target workbook
     * @param aggregateRows the aggregated campaign data used for the summary
     * @param headerStyle the style used for table headers
     * @param centeredValueStyle the style used for centered values
     */
    private void createSummarySheet(Workbook workbook,
                                    List<CampaignAggregatedDataDTO> aggregateRows,
                                    CellStyle headerStyle,
                                    CellStyle centeredValueStyle) {

        Sheet sheet = workbook.createSheet("Summary");
        sheet.setDefaultRowHeightInPoints(20);
        sheet.setDefaultColumnWidth(18);

        int rowIndex = 0;

        Row headerRow = sheet.createRow(rowIndex++);
        headerRow.setHeightInPoints(22);

        createCell(headerRow, 0, "Campaign", headerStyle);
        createCell(headerRow, 1, "Total Attendees", headerStyle);
        createCell(headerRow, 2, "Main Attendees", headerStyle);
        createCell(headerRow, 3, "Companions", headerStyle);
        createCell(headerRow, 4, "Main Attendee Rate %", headerStyle);
        createCell(headerRow, 5, "Companion Rate %", headerStyle);
        createCell(headerRow, 6, "Average Age", headerStyle);
        createCell(headerRow, 7, "Young (<=29)", headerStyle);
        createCell(headerRow, 8, "Adult (30-49)", headerStyle);
        createCell(headerRow, 9, "Senior (50+)", headerStyle);
        createCell(headerRow, 10, "Missing Birth Date", headerStyle);
        createCell(headerRow, 11, "Missing CN", headerStyle);
        createCell(headerRow, 12, "Has Sub-Campaign", headerStyle);
        createCell(headerRow, 13, "Data Completeness %", headerStyle);

        if (aggregateRows == null || aggregateRows.isEmpty()) {
            Row emptyRow = sheet.createRow(rowIndex);
            createCell(emptyRow, 0, "No campaign summary data available.", centeredValueStyle);
            applySummaryColumnSizing(sheet);
            return;
        }

        for (CampaignAggregatedDataDTO aggregateRow : aggregateRows) {
            Row row = sheet.createRow(rowIndex++);

            createCell(row, 0, aggregateRow.campaignDisplayName(), centeredValueStyle);
            createCell(row, 1, String.valueOf(aggregateRow.attendeeCount()), centeredValueStyle);
            createCell(row, 2, String.valueOf(aggregateRow.mainAttendeeCount()), centeredValueStyle);
            createCell(row, 3, String.valueOf(aggregateRow.companionCount()), centeredValueStyle);
            createCell(row, 4, String.valueOf(aggregateRow.mainAttendeeRate()), centeredValueStyle);
            createCell(row, 5, String.valueOf(aggregateRow.companionRate()), centeredValueStyle);
            createCell(row, 6, String.valueOf(aggregateRow.averageAge()), centeredValueStyle);
            createCell(row, 7, String.valueOf(aggregateRow.youngAttendeeCount()), centeredValueStyle);
            createCell(row, 8, String.valueOf(aggregateRow.adultAttendeeCount()), centeredValueStyle);
            createCell(row, 9, String.valueOf(aggregateRow.seniorAttendeeCount()), centeredValueStyle);
            createCell(row, 10, String.valueOf(aggregateRow.missingBirthDateCount()), centeredValueStyle);
            createCell(row, 11, String.valueOf(aggregateRow.missingCnCount()), centeredValueStyle);
            createCell(row, 12, aggregateRow.hasSubCampaign() ? "Yes" : "No", centeredValueStyle);
            createCell(row, 13, String.valueOf(aggregateRow.dataCompletenessRate()), centeredValueStyle);
        }

        applySummaryColumnSizing(sheet);
    }

    /**
     * Creates an empty placeholder sheet for future chart placement.
     *
     * @param workbook the target workbook
     * @param sheetName the name of the sheet to create
     */
    private void createPlaceholderSheet(Workbook workbook, String sheetName) {
        Sheet sheet = workbook.createSheet(sheetName);
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("Chart placeholder");
    }

    /**
     * Creates the style used for table headers.
     *
     * @param workbook the workbook that owns the style
     * @return the configured header style
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    /**
     * Creates the style used for centered cell values.
     *
     * @param workbook the workbook that owns the style
     * @return the configured centered value style
     */
    private CellStyle createCenteredValueStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    /**
     * Creates one cell, assigns its value and optionally applies a style.
     *
     * @param row the parent row
     * @param columnIndex the target column index
     * @param value the cell text value
     * @param style the optional style to apply
     */
    private void createCell(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);

        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    /**
     * Applies auto-sizing and minimum widths to the summary sheet columns.
     *
     * @param sheet the target sheet
     */
    private void applySummaryColumnSizing(Sheet sheet) {
        int[] minimumWidths = {
                22 * 256,
                12 * 256,
                16 * 256,
                12 * 256,
                18 * 256,
                16 * 256,
                12 * 256,
                12 * 256,
                12 * 256,
                12 * 256,
                18 * 256,
                12 * 256,
                16 * 256,
                18 * 256
        };

        for (int columnIndex = 0; columnIndex < minimumWidths.length; columnIndex++) {
            sheet.autoSizeColumn(columnIndex);
            int currentWidth = sheet.getColumnWidth(columnIndex);
            sheet.setColumnWidth(columnIndex, Math.max(currentWidth + (2 * 256), minimumWidths[columnIndex]));
        }
    }
}