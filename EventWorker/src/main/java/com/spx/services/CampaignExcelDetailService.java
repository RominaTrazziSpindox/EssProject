package com.spx.services;

import com.spx.dto.CampaignReportDTO;
import com.spx.dto.CampaignAggregatedDataDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

// Service responsible for generating the Excel report content.

@Service
public class CampaignExcelDetailService {

    // --- MAIN FUNCTIONS

    /**
     * Generates an Excel workbook in memory starting from the reporting DTOs.
     * Each campaign is exported into its own sheet.
     *
     * @param campaignSections the campaigns to include in the report
     * @return the generated Excel file as a byte array
     */
    public byte[] generateReport(List<CampaignReportDTO> campaignSections, List<CampaignAggregatedDataDTO> aggregateRows) {

        try (Workbook workbook = new XSSFWorkbook();

             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                CellStyle titleStyle = createTitleStyle(workbook);
                CellStyle headerStyle = createHeaderStyle(workbook);
                CellStyle centeredValueStyle = createCenteredValueStyle(workbook);

                createSummarySheet(workbook, aggregateRows, headerStyle, centeredValueStyle);

            if (campaignSections == null || campaignSections.isEmpty()) {
                Sheet emptySheet = workbook.createSheet("Campaign Report");
                Row messageRow = emptySheet.createRow(0);
                createCell(messageRow, 0, "No campaigns available.", centeredValueStyle);

            } else {

                for (CampaignReportDTO campaignSection : campaignSections) {
                    createCampaignSheet(workbook, campaignSection, titleStyle, headerStyle, centeredValueStyle);
                }
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException exception) {
            throw new IllegalStateException("Unable to generate Excel report.", exception);
        }
    }

    /**
     * Creates one worksheet for the selected campaign.
     *
     * @param workbook the target workbook
     * @param campaignSection the campaign data to export
     * @param titleStyle the style used for title rows
     * @param headerStyle the style used for table headers
     * @param centeredValueStyle the style used for centered values
     */
    private void createCampaignSheet(Workbook workbook,CampaignReportDTO campaignSection, CellStyle titleStyle, CellStyle headerStyle, CellStyle centeredValueStyle) {

        String safeSheetName = WorkbookUtil.createSafeSheetName(campaignSection.campaignDisplayName());
        Sheet sheet = workbook.createSheet(safeSheetName);

        sheet.setDefaultRowHeightInPoints(20);
        sheet.setDefaultColumnWidth(18);

        int rowIndex = 0;

        Row titleRow = sheet.createRow(rowIndex++);
        titleRow.setHeightInPoints(24);
        createCell(titleRow, 0, "Campaign", titleStyle);
        createCell(titleRow, 1, campaignSection.campaignDisplayName(), titleStyle);

        Row countRow = sheet.createRow(rowIndex++);
        countRow.setHeightInPoints(22);
        createCell(countRow, 0, "Attendee count", titleStyle);
        createCell(countRow, 1, String.valueOf(campaignSection.attendeeCount()), centeredValueStyle);

        rowIndex++;

        Row headerRow = sheet.createRow(rowIndex++);
        headerRow.setHeightInPoints(22);
        createCell(headerRow, 0, "First Name", headerStyle);
        createCell(headerRow, 1, "Last Name", headerStyle);
        createCell(headerRow, 2, "CN", headerStyle);
        createCell(headerRow, 3, "Birth Date", headerStyle);
        createCell(headerRow, 4, "Companion", headerStyle);

        for (CampaignReportDTO.AttendeeReportRow attendeeRow : campaignSection.attendeeRows()) {
            Row row = sheet.createRow(rowIndex++);

            createCell(row, 0, defaultString(attendeeRow.firstName()), centeredValueStyle);
            createCell(row, 1, defaultString(attendeeRow.lastName()), centeredValueStyle);
            createCell(row, 2, defaultString(attendeeRow.cn()), centeredValueStyle);
            createCell(row, 3, attendeeRow.birthDate() == null ? "" : attendeeRow.birthDate().toString(), centeredValueStyle);
            createCell(row, 4, Boolean.TRUE.equals(attendeeRow.companion()) ? "Yes" : "No", centeredValueStyle);
        }

        applyColumnSizing(sheet);
    }

    /**
     * Creates the summary worksheet containing one aggregated row for each campaign.
     *
     * @param workbook the target workbook
     * @param aggregateRows the aggregated campaign data used for the summary
     * @param headerStyle the style used for table headers
     * @param centeredValueStyle the style used for centered values
     */
    private void createSummarySheet(Workbook workbook, List<CampaignAggregatedDataDTO> aggregateRows, CellStyle headerStyle, CellStyle centeredValueStyle) {

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


    // --- STYLE FUNCTIONS FOR THE WORKBOOK AND THE SHEETS

    /**
     * Creates the style used for report titles and summary labels.
     *
     * @param workbook the workbook that owns the style
     * @return the configured title style
     */
    private CellStyle createTitleStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
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
     * Applies auto-sizing and minimum widths to the report columns.
     *
     * @param sheet the target sheet
     */
    private void applyColumnSizing(Sheet sheet) {
        int[] minimumWidths = {
                18 * 256,
                18 * 256,
                20 * 256,
                14 * 256,
                12 * 256
        };

        for (int columnIndex = 0; columnIndex < minimumWidths.length; columnIndex++) {
            sheet.autoSizeColumn(columnIndex);
            int currentWidth = sheet.getColumnWidth(columnIndex);
            sheet.setColumnWidth(columnIndex, Math.max(currentWidth + (2 * 256), minimumWidths[columnIndex]));
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

    /**
     * Returns an empty string when the provided value is null.
     *
     * @param value the source string
     * @return a non-null string value
     */
    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
