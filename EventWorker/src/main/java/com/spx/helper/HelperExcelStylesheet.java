package com.spx.helper;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// Utility class containing shared Excel styling and sheet formatting helpers.
public final class HelperExcelStylesheet {

    // Standard minimum widths used by the dashboard summary sheet.
    public static final int[] SUMMARY_SHEET_WIDTHS = {
            excelWidth(22),
            excelWidth(12),
            excelWidth(16),
            excelWidth(12),
            excelWidth(18),
            excelWidth(16),
            excelWidth(12),
            excelWidth(12),
            excelWidth(12),
            excelWidth(12),
            excelWidth(18),
            excelWidth(12),
            excelWidth(16),
            excelWidth(18)
    };

    public static final int[] DETAIL_SHEET_WIDTHS = {
            excelWidth( 18),
            excelWidth(20),
            excelWidth(18),
            excelWidth(20),
            excelWidth(14),
            excelWidth(12)
    };

    private HelperExcelStylesheet() {

    }

    // --- STYLE FUNCTIONS FOR THE WORKBOOK AND THE SHEETS

    /**
     * Creates the style used for report titles and summary labels.
     *
     * @param workbook the workbook that owns the style
     * @return the configured title style
     */
    public static CellStyle createTitleStyle(Workbook workbook) {
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
    public static CellStyle createHeaderStyle(Workbook workbook) {
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
    public static CellStyle createCenteredValueStyle(Workbook workbook) {
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
    public static void createCell(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);

        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    /**
     * Applies the default layout used by standard report sheets.
     *
     * @param sheet the target sheet
     */
    public static void applyDefaultSheetLayout(Sheet sheet) {
        sheet.setDefaultRowHeightInPoints(20);
        sheet.setDefaultColumnWidth(18);
    }

    /**
     * Converts a column width expressed in visible character units
     * to the internal Excel width unit used by Apache POI.
     *
     * @param characters the desired approximate width in characters
     * @return the width expressed in Excel internal units
     */
    public static int excelWidth(int characters) {
        return characters * 256;
    }

    /**
     * Applies auto-sizing and minimum widths to the selected columns.
     *
     * @param sheet the target sheet
     * @param minimumWidths the minimum widths expressed in Excel width units
     */
    public static void applyColumnSizing(Sheet sheet, int[] minimumWidths) {
        for (int columnIndex = 0; columnIndex < minimumWidths.length; columnIndex++) {
            sheet.autoSizeColumn(columnIndex);
            int currentWidth = sheet.getColumnWidth(columnIndex);
            sheet.setColumnWidth(columnIndex, Math.max(currentWidth + (2 * 256), minimumWidths[columnIndex]));
        }
    }

    /**
     * Creates an empty placeholder sheet for future chart placement.
     *
     * @param workbook the target workbook
     * @param sheetName the name of the sheet to create
     */
    public static void createPlaceholderSheet(XSSFWorkbook workbook, String sheetName) {
        Sheet sheet = workbook.createSheet(sheetName);
        applyDefaultSheetLayout(sheet);

        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("Chart placeholder");
    }

    /**
     * Returns an empty string when the provided value is null.
     *
     * @param value the source string
     * @return a non-null string value
     */
    public static String defaultString(String value) {
        return value == null ? "" : value;
    }
}