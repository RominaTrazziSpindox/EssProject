package com.spx.helper;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.AxisCrosses;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.MarkerStyle;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// Utility class containing shared helpers for Excel chart sheets and chart configuration.
public final class HelperExcelCharts {

    private HelperExcelCharts() {
    }

    /**
     * Creates a chart sheet and applies the shared default sheet layout.
     *
     * @param workbook the target workbook
     * @param sheetName the sheet name
     * @return the created chart sheet
     */
    public static XSSFSheet createChartSheet(XSSFWorkbook workbook, String sheetName) {
        XSSFSheet sheet = workbook.createSheet(sheetName);
        HelperExcelStylesheet.applyDefaultSheetLayout(sheet);
        return sheet;
    }

    /**
     * Writes a fallback message when no chart data is available.
     *
     * @param sheet the target sheet
     * @param message the message to display
     */
    public static void writeNoDataMessage(XSSFSheet sheet, String message) {
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue(message);

        HelperExcelStylesheet.applyColumnSizing(sheet, new int[] {
                HelperExcelStylesheet.excelWidth(35)
        });
    }

    /**
     * Creates a chart with a shared visual configuration.
     *
     * @param sheet the target sheet
     * @param col1 the anchor start column
     * @param row1 the anchor start row
     * @param col2 the anchor end column
     * @param row2 the anchor end row
     * @param title the chart title
     * @param legendPosition the legend position
     * @return the created chart
     */
    public static XSSFChart createChart(XSSFSheet sheet,
                                        int col1, int row1, int col2, int row2,  String title,
                                        LegendPosition legendPosition) {

        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, col1, row1, col2, row2);
        XSSFChart chart = drawing.createChart(anchor);

        chart.setTitleText(title);
        chart.setTitleOverlay(false);
        chart.getOrAddLegend().setPosition(legendPosition);

        return chart;
    }

    /**
     * Creates a value axis configured for count-based charts.
     *
     * @param chart the target chart
     * @param position the axis position
     * @param title the axis title
     * @return the configured value axis
     */
    public static XDDFValueAxis createCountAxis(XSSFChart chart, AxisPosition position, String title) {

        XDDFValueAxis valueAxis = chart.createValueAxis(position);
        valueAxis.setTitle(title);
        valueAxis.setCrosses(AxisCrosses.AUTO_ZERO);

        return valueAxis;
    }

    /**
     * Creates and configures a generic value axis.
     *
     * @param chart the target chart
     * @param position the axis position
     * @param title the axis title
     * @param minValue the optional minimum value
     * @param maxValue the optional maximum value
     * @param numberFormat the optional number format
     * @return the configured value axis
     */
    public static XDDFValueAxis createValueAxis(XSSFChart chart,
                                                AxisPosition position, String title,
                                                Double minValue, Double maxValue, String numberFormat) {

        XDDFValueAxis valueAxis = chart.createValueAxis(position);
        valueAxis.setTitle(title);

        if (minValue != null) {
            valueAxis.setMinimum(minValue);
        }

        if (maxValue != null) {
            valueAxis.setMaximum(maxValue);
        }

        if (numberFormat != null && !numberFormat.isBlank()) {
            valueAxis.setNumberFormat(numberFormat);
        }

        return valueAxis;
    }

    /**
     * Creates a category data source from the given sheet range.
     *
     * @param sheet the source sheet
     * @param firstDataRow the first row index
     * @param lastDataRow the last row index
     * @param columnIndex the source column index
     * @return the category data source
     */
    public static XDDFCategoryDataSource categorySource(XSSFSheet sheet,
                                                        int firstDataRow, int lastDataRow, int columnIndex) {

        return XDDFDataSourcesFactory.fromStringCellRange(sheet,
                new CellRangeAddress(firstDataRow, lastDataRow, columnIndex, columnIndex)
        );
    }

    /**
     * Creates a numeric data source from the given sheet range.
     *
     * @param sheet the source sheet
     * @param firstDataRow the first row index
     * @param lastDataRow the last row index
     * @param columnIndex the source column index
     * @return the numeric data source
     */
    public static XDDFNumericalDataSource<Double> numericSource(XSSFSheet sheet, int firstDataRow, int lastDataRow, int columnIndex) {

        return XDDFDataSourcesFactory.fromNumericCellRange(
                sheet, new CellRangeAddress(firstDataRow, lastDataRow, columnIndex, columnIndex)
        );
    }

    /**
     * Applies the standard configuration used by line-chart series.
     *
     * @param series the target line series
     * @param seriesTitle the series title
     * @param markerStyle the marker style
     */
    public static void configureLineSeries(XDDFLineChartData.Series series, String seriesTitle, MarkerStyle markerStyle) {

        series.setTitle(seriesTitle, null);
        series.setSmooth(false);
        series.setMarkerStyle(markerStyle);
        series.setMarkerSize((short) 6);
    }

    /**
     * Writes a header row starting from the given column.
     *
     * @param sheet the target sheet
     * @param startColumn the first header column
     * @param headers the header values
     */
    public static void writeHeaders(XSSFSheet sheet, int startColumn, String... headers) {
        for (int index = 0; index < headers.length; index++) {
            writeTextCell(sheet, 0, startColumn + index, headers[index]);
        }
    }

    /**
     * Hides the support columns used as chart data sources.
     *
     * @param sheet the target sheet
     * @param startColumn the first column to hide
     * @param columnCount the number of columns to hide
     */
    public static void hideSupportColumns(XSSFSheet sheet, int startColumn, int columnCount) {
        for (int columnIndex = startColumn; columnIndex < startColumn + columnCount; columnIndex++) {
            sheet.setColumnHidden(columnIndex, true);
        }
    }

    /**
     * Writes a string value into the given sheet cell.
     *
     * @param sheet the target sheet
     * @param rowIndex the target row index
     * @param columnIndex the target column index
     * @param value the text to write
     */
    public static void writeTextCell(XSSFSheet sheet, int rowIndex, int columnIndex, String value) {
        Row row = sheet.getRow(rowIndex) == null ? sheet.createRow(rowIndex) : sheet.getRow(rowIndex);
        row.createCell(columnIndex).setCellValue(HelperExcelStylesheet.defaultString(value));
    }

    /**
     * Writes a numeric value into the given sheet cell.
     *
     * @param sheet the target sheet
     * @param rowIndex the target row index
     * @param columnIndex the target column index
     * @param value the number to write
     */
    public static void writeNumericCell(XSSFSheet sheet, int rowIndex, int columnIndex, Number value) {
        Row row = sheet.getRow(rowIndex) == null ? sheet.createRow(rowIndex) : sheet.getRow(rowIndex);
        row.createCell(columnIndex).setCellValue(value == null ? 0d : value.doubleValue());
    }
}