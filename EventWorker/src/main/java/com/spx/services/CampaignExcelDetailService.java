package com.spx.services;

import com.spx.dto.CampaignReportDTO;
import com.spx.helper.HelperExcelStylesheet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.springframework.stereotype.Service;

import java.util.List;

// Service responsible for generating the Excel report content for each campaign (= detail).
@Service
public class CampaignExcelDetailService {

    // Constants
    private final ExcelSheetGeneratorService excelSheetGeneratorService;

    // Constructor
    public CampaignExcelDetailService(ExcelSheetGeneratorService excelSheetGeneratorService) {
        this.excelSheetGeneratorService = excelSheetGeneratorService;
    }

    /**
     * Generates an Excel workbook in memory starting from the reporting DTOs.
     * Each campaign is exported into its own sheet.
     *
     * @param campaignSections the campaigns to include in the report
     * @return the generated Excel file as a byte array
     */
    public byte[] generateDetailWorkbook(List<CampaignReportDTO> campaignSections) {

        return excelSheetGeneratorService.generateWorkbook(workbook -> {

            // Sheet style
            CellStyle titleStyle = HelperExcelStylesheet.createTitleStyle(workbook);
            CellStyle headerStyle = HelperExcelStylesheet.createHeaderStyle(workbook);
            CellStyle centeredValueStyle = HelperExcelStylesheet.createCenteredValueStyle(workbook);

            // When no campaigns are available
            if (campaignSections == null || campaignSections.isEmpty()) {
                Sheet emptySheet = workbook.createSheet("Campaign Report");
                Row messageRow = emptySheet.createRow(0);
                HelperExcelStylesheet.createCell(messageRow, 0, "No campaigns available.", centeredValueStyle);

            } else {

                // Create one worksheet for each campaign
                for (CampaignReportDTO campaignSection : campaignSections) {
                    createCampaignSheet(workbook, campaignSection, titleStyle, headerStyle, centeredValueStyle);
                }
            }
        });
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

        // Create sheet
        String safeSheetName = WorkbookUtil.createSafeSheetName(campaignSection.campaignDisplayName());
        Sheet sheet = workbook.createSheet(safeSheetName);

        // Sheet layout
        HelperExcelStylesheet.applyDefaultSheetLayout(sheet);

        int rowIndex = 0;

        // Main header rows
        Row titleRow = sheet.createRow(rowIndex++);
        titleRow.setHeightInPoints(24);
        HelperExcelStylesheet.createCell(titleRow, 0, "Campaign", titleStyle);
        HelperExcelStylesheet.createCell(titleRow, 1, campaignSection.campaignDisplayName(), titleStyle);

        Row countRow = sheet.createRow(rowIndex++);
        countRow.setHeightInPoints(22);
        HelperExcelStylesheet.createCell(countRow, 0, "Attendee count", titleStyle);
        HelperExcelStylesheet.createCell(countRow, 1, String.valueOf(campaignSection.attendeeCount()), centeredValueStyle);

        rowIndex++;

        // Sub-header rows
        Row headerRow = sheet.createRow(rowIndex++);
        headerRow.setHeightInPoints(22);
        HelperExcelStylesheet.createCell(headerRow, 0, "First Name", headerStyle);
        HelperExcelStylesheet.createCell(headerRow, 1, "Last Name", headerStyle);
        HelperExcelStylesheet.createCell(headerRow, 2, "CN", headerStyle);
        HelperExcelStylesheet.createCell(headerRow, 3, "Birth Date", headerStyle);
        HelperExcelStylesheet.createCell(headerRow, 4, "Companion", headerStyle);

        // Values
        for (CampaignReportDTO.AttendeeReportRow attendeeRow : campaignSection.attendeeRows()) {
            Row row = sheet.createRow(rowIndex++);

            HelperExcelStylesheet.createCell(row, 0, HelperExcelStylesheet.defaultString(attendeeRow.firstName()), centeredValueStyle);
            HelperExcelStylesheet.createCell(row, 1, HelperExcelStylesheet.defaultString(attendeeRow.lastName()), centeredValueStyle);
            HelperExcelStylesheet.createCell(row, 2, HelperExcelStylesheet.defaultString(attendeeRow.cn()), centeredValueStyle);
            HelperExcelStylesheet.createCell(row,3, attendeeRow.birthDate() == null ? "" : attendeeRow.birthDate().toString(), centeredValueStyle);
            HelperExcelStylesheet.createCell(row, 4, Boolean.TRUE.equals(attendeeRow.companion()) ? "Yes" : "No", centeredValueStyle);
        }

        // Column size
        HelperExcelStylesheet.applyColumnSizing(sheet, new int[]{
                HelperExcelStylesheet.excelWidth(18),
                HelperExcelStylesheet.excelWidth(18),
                HelperExcelStylesheet.excelWidth(20),
                HelperExcelStylesheet.excelWidth(14),
                HelperExcelStylesheet.excelWidth(12)
        });
    }
}