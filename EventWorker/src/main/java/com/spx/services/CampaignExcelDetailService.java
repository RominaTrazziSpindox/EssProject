package com.spx.services;

import com.spx.dto.CampaignReportDTO;
import com.spx.helper.HelperExcelStylesheet;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Service responsible for generating the Excel detail report, with one worksheet for each campaign
@Service
@Slf4j
public class CampaignExcelDetailService {

    // Constants
    private final ExcelGeneratorService excelGeneratorService;

    // Constructor
    public CampaignExcelDetailService(ExcelGeneratorService excelGeneratorService) {
        this.excelGeneratorService = excelGeneratorService;
    }

    /**
     * Generates the campaign detail workbook in memory.
     * Each campaign is exported into a dedicated worksheet.
     *
     * @param campaignReports the campaign report sections to export
     * @return the generated Excel file as a byte array
     */
    public byte[] generateCampaignDetailWorkbook(List<CampaignReportDTO> campaignReports) {

        log.info("Starting campaign detail workbook generation. Campaign count: {}", campaignReports == null ? 0 : campaignReports.size());

        // Delegate workbook creation to the shared Excel generator service
        return excelGeneratorService.generateWorkbook(workbook -> {

            // Create reusable workbook styles
            CellStyle titleStyle = HelperExcelStylesheet.createTitleStyle(workbook);
            CellStyle headerStyle = HelperExcelStylesheet.createHeaderStyle(workbook);
            CellStyle bodyCellStyle = HelperExcelStylesheet.createCenteredValueStyle(workbook);

            // If no campaign data is available, generate a simple fallback empty sheet
            if (campaignReports == null || campaignReports.isEmpty()) {
                createEmptySheet(workbook, bodyCellStyle);

                log.info("No campaign data available. Generated empty detail workbook.");
                return;
            }

            // Track used sheet names to avoid duplicates in the same workbook
            Set<String> usedSheetNames = new HashSet<>();

            // Create one worksheet for each campaign.
            for (CampaignReportDTO campaignReport : campaignReports) {
                createCampaignSheet(workbook, campaignReport, titleStyle, headerStyle, bodyCellStyle, usedSheetNames);
            }

            log.info("Campaign detail workbook generated successfully.");
        });
    }

    /**
     * Creates a fallback sheet when no campaigns are available.
     *
     * @param workbook the target workbook
     * @param bodyCellStyle the style used for the message cell
     */
    private void createEmptySheet(Workbook workbook, CellStyle bodyCellStyle) {
        Sheet emptySheet = workbook.createSheet("Campaign Report");
        Row messageRow = emptySheet.createRow(0);

        HelperExcelStylesheet.createCell(messageRow, 0, "No campaigns available.", bodyCellStyle);
        HelperExcelStylesheet.applyColumnSizing(emptySheet, new int[]{
                HelperExcelStylesheet.excelWidth(30)
        });
    }

    /**
     * Creates one worksheet for the given campaign and writes
     * both campaign metadata and attendee rows.
     *
     * @param workbook the target workbook
     * @param campaignReport the campaign data to export
     * @param titleStyle the style used for title rows
     * @param headerStyle the style used for table headers
     * @param bodyCellStyle the style used for normal data cells
     * @param usedSheetNames the set of sheet names already used in the workbook
     */
    private void createCampaignSingleSheet(Workbook workbook, CampaignReportDTO campaignReport, CellStyle titleStyle,
                                           CellStyle headerStyle, CellStyle bodyCellStyle, Set<String> usedSheetNames) {

        // Build a sheet name that is both Excel-safe and unique inside the workbook
        String sheetName = buildUniqueSheetName(campaignReport.campaignDisplayName(), usedSheetNames);
        Sheet sheet = workbook.createSheet(sheetName);

        // Apply the standard sheet layout shared across the report
        HelperExcelStylesheet.applyDefaultSheetLayout(sheet);

        int rowIndex = 0;

        // Write the main campaign title row
        Row titleRow = sheet.createRow(rowIndex++);
        titleRow.setHeightInPoints(24);
        HelperExcelStylesheet.createCell(titleRow, 0, "Campaign", titleStyle);
        HelperExcelStylesheet.createCell(titleRow,  1, HelperExcelStylesheet.defaultString(campaignReport.campaignDisplayName()), titleStyle);

        // Write the total attendee count for the current campaign
        Row attendeeCountRow = sheet.createRow(rowIndex++);
        attendeeCountRow.setHeightInPoints(22);
        HelperExcelStylesheet.createCell(attendeeCountRow, 0, "Attendee count", titleStyle);
        HelperExcelStylesheet.createCell(attendeeCountRow,1, String.valueOf(campaignReport.attendeeCount()), bodyCellStyle);

        // Leave one empty row between campaign metadata and attendee table
        rowIndex++;

        // Write the attendee table header
        Row tableHeaderRow = sheet.createRow(rowIndex++);
        tableHeaderRow.setHeightInPoints(22);
        HelperExcelStylesheet.createCell(tableHeaderRow, 0, "First Name", headerStyle);
        HelperExcelStylesheet.createCell(tableHeaderRow, 1, "Last Name", headerStyle);
        HelperExcelStylesheet.createCell(tableHeaderRow, 2, "CN", headerStyle);
        HelperExcelStylesheet.createCell(tableHeaderRow, 3, "Birth Date", headerStyle);
        HelperExcelStylesheet.createCell(tableHeaderRow, 4, "Companion", headerStyle);

        // Protect against null attendee lists to avoid NullPointerException
        List<CampaignReportDTO.AttendeeReportRow> attendeeRows = campaignReport.attendeeRows() == null ? List.of() : campaignReport.attendeeRows();

        // Write one row per attendee.
        for (CampaignReportDTO.AttendeeReportRow attendeeRow : attendeeRows) {

            Row row = sheet.createRow(rowIndex++);

            HelperExcelStylesheet.createCell(row, 0, HelperExcelStylesheet.defaultString(attendeeRow.firstName()), bodyCellStyle);
            HelperExcelStylesheet.createCell(row, 1, HelperExcelStylesheet.defaultString(attendeeRow.lastName()), bodyCellStyle);
            HelperExcelStylesheet.createCell(row, 2, HelperExcelStylesheet.defaultString(attendeeRow.cn()), bodyCellStyle);
            HelperExcelStylesheet.createCell(row, 3, attendeeRow.birthDate() == null ? "" : attendeeRow.birthDate().toString(), bodyCellStyle);
            HelperExcelStylesheet.createCell(row, 4, Boolean.TRUE.equals(attendeeRow.companion()) ? "Yes" : "No", bodyCellStyle);
        }

        // Freeze the upper section so headers remain visible while scrolling.
        sheet.createFreezePane(0, 4);

        // Apply a filter to the attendee table header row.
        sheet.setAutoFilter(new CellRangeAddress(3, 3, 0, 4));

        // Set fixed column widths for readability.
        HelperExcelStylesheet.applyColumnSizing(sheet, new int[]{
                HelperExcelStylesheet.excelWidth(18),
                HelperExcelStylesheet.excelWidth(18),
                HelperExcelStylesheet.excelWidth(20),
                HelperExcelStylesheet.excelWidth(14),
                HelperExcelStylesheet.excelWidth(12)
        });
    }

    /**
     * Builds a unique and Excel-safe sheet name.
     * This method avoids duplicate names and handles Excel's 31-character limit.
     *
     * @param rawSheetName the original campaign display name
     * @param usedSheetNames the names already used in the workbook
     * @return a unique safe sheet name
     */
    private String buildUniqueSheetName(String rawSheetName, Set<String> usedSheetNames) {

        // Fallback name if campaign display name is null or blank.
        String baseName = (rawSheetName == null || rawSheetName.isBlank()) ? "Campaign" : rawSheetName;

        // First sanitize the base name.
        String safeBaseName = WorkbookUtil.createSafeSheetName(baseName);

        // If not already used, return it immediately.
        if (!usedSheetNames.contains(safeBaseName)) {
            usedSheetNames.add(safeBaseName);
            return safeBaseName;
        }

        // Otherwise append a numeric suffix, keeping the final name within Excel's 31-char limit.
        int counter = 1;
        while (true) {

            String suffix = "_" + counter;
            int maxBaseLength = 31 - suffix.length();

            String truncatedBase = safeBaseName.length() > maxBaseLength ? safeBaseName.substring(0, maxBaseLength) : safeBaseName;

            String candidate = WorkbookUtil.createSafeSheetName(truncatedBase + suffix);

            if (!usedSheetNames.contains(candidate)) {
                usedSheetNames.add(candidate);
                return candidate;
            }

            counter++;
        }
    }
}