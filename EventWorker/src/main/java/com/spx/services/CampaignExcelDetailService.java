package com.spx.services;

import com.spx.dto.CampaignReportDTO;
import com.spx.helper.HelperExcelStylesheet;
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
    public byte[] generateReport(List<CampaignReportDTO> campaignSections) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            CellStyle titleStyle = HelperExcelStylesheet.createTitleStyle(workbook);
            CellStyle headerStyle = HelperExcelStylesheet.createHeaderStyle(workbook);
            CellStyle centeredValueStyle = HelperExcelStylesheet.createCenteredValueStyle(workbook);

            if (campaignSections == null || campaignSections.isEmpty()) {
                Sheet emptySheet = workbook.createSheet("Campaign Report");
                Row messageRow = emptySheet.createRow(0);
                HelperExcelStylesheet.createCell(messageRow, 0, "No campaigns available.", centeredValueStyle);

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
    private void createCampaignSheet(Workbook workbook,CampaignReportDTO campaignSection,
                                     CellStyle titleStyle, CellStyle headerStyle, CellStyle centeredValueStyle) {

        // Create a new sheet
        String safeSheetName = WorkbookUtil.createSafeSheetName(campaignSection.campaignDisplayName());
        Sheet sheet = workbook.createSheet(safeSheetName);

        // Size of default row and column
        sheet.setDefaultRowHeightInPoints(20);
        sheet.setDefaultColumnWidth(18);

        int rowIndex = 0;

        // Title of the sheet (Campaign + Subcampaign if exists)
        Row titleRow = sheet.createRow(rowIndex++);
        titleRow.setHeightInPoints(24);
        HelperExcelStylesheet.createCell(titleRow, 0, "Campaign", titleStyle);
        HelperExcelStylesheet.createCell(titleRow, 1, campaignSection.campaignDisplayName(), titleStyle);

        // Total attendee
        Row countRow = sheet.createRow(rowIndex++);
        countRow.setHeightInPoints(22);
        HelperExcelStylesheet.createCell(countRow, 0, "Attendee count", titleStyle);
        HelperExcelStylesheet.createCell(countRow, 1, String.valueOf(campaignSection.attendeeCount()), centeredValueStyle);

        rowIndex++;

        // Sub header section
        Row headerRow = sheet.createRow(rowIndex++);
        headerRow.setHeightInPoints(22);
        HelperExcelStylesheet.createCell(headerRow, 0, "First Name", headerStyle);
        HelperExcelStylesheet.createCell(headerRow, 1, "Last Name", headerStyle);
        HelperExcelStylesheet.createCell(headerRow, 2, "CN", headerStyle);
        HelperExcelStylesheet.createCell(headerRow, 3, "Birth Date", headerStyle);
        HelperExcelStylesheet.createCell(headerRow, 4, "Companion", headerStyle);

        // Value section
        for (CampaignReportDTO.AttendeeReportRow attendeeRow : campaignSection.attendeeRows()) {

            Row row = sheet.createRow(rowIndex++);

            HelperExcelStylesheet.createCell(row, 0, HelperExcelStylesheet.defaultString(attendeeRow.firstName()), centeredValueStyle);
            HelperExcelStylesheet.createCell(row, 1, HelperExcelStylesheet.defaultString(attendeeRow.lastName()), centeredValueStyle);
            HelperExcelStylesheet.createCell(row, 2, HelperExcelStylesheet.defaultString(attendeeRow.cn()), centeredValueStyle);
            HelperExcelStylesheet.createCell(row, 3, attendeeRow.birthDate() == null ? "" : attendeeRow.birthDate().toString(), centeredValueStyle);
            HelperExcelStylesheet.createCell( row, 4, Boolean.TRUE.equals(attendeeRow.companion()) ? "Yes" : "No", centeredValueStyle);

            // Size of each column
            HelperExcelStylesheet.applyColumnSizing(sheet, HelperExcelStylesheet.DETAIL_SHEET_WIDTHS);
        }
    }
}