package com.spx.services;

import com.spx.dto.CampaignAggregatedDataDTO;
import com.spx.helper.HelperExcelStylesheet;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFPieChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.util.List;

// Service responsible for building the chart sheets inside the dashboard workbook.
@Service
@Slf4j
public class DashboardChartBuilderService {

    /**
     * Creates the attendance overview sheet and adds the horizontal bar chart
     * showing the number of attendees for each campaign.
     *
     * @param workbook the target workbook
     * @param summarySheet the summary sheet containing chart source data
     * @param aggregateRows the aggregated campaign rows
     */
    public void createAttendanceOverviewSheet(XSSFWorkbook workbook,
                                              XSSFSheet summarySheet,
                                              List<CampaignAggregatedDataDTO> aggregateRows) {

        log.info("CHART DEBUG - createAttendanceOverviewSheet started. aggregateRows={}",
                aggregateRows != null ? aggregateRows.size() : 0);

        // Create sheet
        XSSFSheet chartSheet = workbook.createSheet("Attendance Overview");
        HelperExcelStylesheet.applyDefaultSheetLayout(chartSheet);

        // When no chart data is available
        if (aggregateRows == null || aggregateRows.isEmpty()) {
            log.info("CHART DEBUG - Attendance Overview skipped because no chart data is available.");
            Row row = chartSheet.createRow(0);
            row.createCell(0).setCellValue("No chart data available.");
            return;
        }

        XSSFDrawing drawing = chartSheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, 0, 1, 12, 25);
        XSSFChart chart = drawing.createChart(anchor);

        // Chart title and legend
        chart.setTitleText("Top Campaigns by Attendance");
        chart.setTitleOverlay(false);
        chart.getOrAddLegend().setPosition(LegendPosition.BOTTOM);

        // Chart axes
        XDDFCategoryAxis categoryAxis = chart.createCategoryAxis(AxisPosition.LEFT);
        XDDFValueAxis valueAxis = chart.createValueAxis(AxisPosition.BOTTOM);
        valueAxis.setTitle("Attendees");

        int firstDataRow = 1;
        int lastDataRow = aggregateRows.size();

        log.info("CHART DEBUG - Attendance Overview source sheet='{}', firstDataRow={}, lastDataRow={}",
                summarySheet.getSheetName(), firstDataRow, lastDataRow);

        log.info("CHART DEBUG - Attendance Overview categories from Summary column A, values from Summary column B");

        for (int rowIndex = firstDataRow; rowIndex <= lastDataRow; rowIndex++) {
            Row row = summarySheet.getRow(rowIndex);

            String categoryValue = readCellAsString(row, 0);
            String numericValue = readCellAsString(row, 1);

            log.info("CHART DEBUG - Attendance Overview row={}, category={}, value={}",
                    rowIndex, categoryValue, numericValue);
        }

        // Categories come from Summary column A: Campaign
        XDDFCategoryDataSource categories = XDDFDataSourcesFactory.fromStringCellRange(
                summarySheet,
                new CellRangeAddress(firstDataRow, lastDataRow, 0, 0)
        );

        // Values come from Summary column B: Attendees
        XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(
                summarySheet,
                new CellRangeAddress(firstDataRow, lastDataRow, 1, 1)
        );

        // Build chart data
        XDDFBarChartData data = (XDDFBarChartData) chart.createData(
                ChartTypes.BAR,
                categoryAxis,
                valueAxis
        );

        data.setBarDirection(BarDirection.BAR);

        XDDFBarChartData.Series series = (XDDFBarChartData.Series) data.addSeries(categories, values);
        series.setTitle("Attendees", null);

        log.info("CHART DEBUG - Attendance Overview plotting chart with {} data rows.", aggregateRows.size());

        // Render chart
        chart.plot(data);
    }

    /**
     * Creates the composition sheet and adds:
     * - the stacked chart for main attendees vs companions
     * - the clustered chart for main attendee rate vs companion rate
     *
     * @param workbook the target workbook
     * @param summarySheet the summary sheet containing chart source data
     * @param aggregateRows the aggregated campaign rows
     */
    public void createCompositionSheet(XSSFWorkbook workbook,
                                       XSSFSheet summarySheet,
                                       List<CampaignAggregatedDataDTO> aggregateRows) {

        log.info("CHART DEBUG - createCompositionSheet started. aggregateRows={}",
                aggregateRows != null ? aggregateRows.size() : 0);

        // Create sheet
        XSSFSheet chartSheet = workbook.createSheet("Composition");
        HelperExcelStylesheet.applyDefaultSheetLayout(chartSheet);

        // When no chart data is available
        if (aggregateRows == null || aggregateRows.isEmpty()) {
            log.info("CHART DEBUG - Composition skipped because no chart data is available.");
            Row row = chartSheet.createRow(0);
            row.createCell(0).setCellValue("No chart data available.");
            return;
        }

        int firstDataRow = 1;
        int lastDataRow = aggregateRows.size();

        log.info("CHART DEBUG - Composition source sheet='{}', firstDataRow={}, lastDataRow={}",
                summarySheet.getSheetName(), firstDataRow, lastDataRow);

        log.info("CHART DEBUG - Composition categories from Summary column A");

        for (int rowIndex = firstDataRow; rowIndex <= lastDataRow; rowIndex++) {
            Row row = summarySheet.getRow(rowIndex);

            String campaignValue = readCellAsString(row, 0);
            String mainAttendeesValue = readCellAsString(row, 2);
            String companionsValue = readCellAsString(row, 3);
            String mainRateValue = readCellAsString(row, 4);
            String companionRateValue = readCellAsString(row, 5);

            log.info(
                    "CHART DEBUG - Composition row={}, campaign={}, mainAttendees={}, companions={}, mainRate={}, companionRate={}",
                    rowIndex,
                    campaignValue,
                    mainAttendeesValue,
                    companionsValue,
                    mainRateValue,
                    companionRateValue
            );
        }

        XDDFCategoryDataSource categories = XDDFDataSourcesFactory.fromStringCellRange(
                summarySheet,
                new CellRangeAddress(firstDataRow, lastDataRow, 0, 0)
        );

        XSSFDrawing drawing = chartSheet.createDrawingPatriarch();

        // --- Chart 1: Main Attendees vs Companions by Campaign

        XSSFClientAnchor firstAnchor = new XSSFClientAnchor(0, 0, 0, 0, 0, 1, 12, 20);
        XSSFChart firstChart = drawing.createChart(firstAnchor);

        firstChart.setTitleText("Main Attendees vs Companions by Campaign");
        firstChart.setTitleOverlay(false);
        firstChart.getOrAddLegend().setPosition(LegendPosition.BOTTOM);

        XDDFCategoryAxis firstCategoryAxis = firstChart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis firstValueAxis = firstChart.createValueAxis(AxisPosition.LEFT);
        firstValueAxis.setTitle("Attendees");

        XDDFNumericalDataSource<Double> mainAttendeesValues = XDDFDataSourcesFactory.fromNumericCellRange(
                summarySheet,
                new CellRangeAddress(firstDataRow, lastDataRow, 2, 2)
        );

        XDDFNumericalDataSource<Double> companionValues = XDDFDataSourcesFactory.fromNumericCellRange(
                summarySheet,
                new CellRangeAddress(firstDataRow, lastDataRow, 3, 3)
        );

        log.info("CHART DEBUG - Composition chart 1 ranges -> categories=A{}:A{}, mainAttendees=C{}:C{}, companions=D{}:D{}",
                firstDataRow + 1, lastDataRow + 1,
                firstDataRow + 1, lastDataRow + 1,
                firstDataRow + 1, lastDataRow + 1);

        XDDFBarChartData firstChartData = (XDDFBarChartData) firstChart.createData(
                ChartTypes.BAR,
                firstCategoryAxis,
                firstValueAxis
        );

        firstChartData.setBarDirection(BarDirection.COL);
        // firstChartData.setOverlap((byte) 100);

        XDDFBarChartData.Series mainSeries =
                (XDDFBarChartData.Series) firstChartData.addSeries(categories, mainAttendeesValues);
        mainSeries.setTitle("Main Attendees", null);

        XDDFBarChartData.Series companionSeries =
                (XDDFBarChartData.Series) firstChartData.addSeries(categories, companionValues);
        companionSeries.setTitle("Companions", null);

        log.info("CHART DEBUG - Composition chart 1 plotting with {} data rows.", aggregateRows.size());

        firstChart.plot(firstChartData);

        // --- Chart 2: Main Attendee Rate % vs Companion Rate % by Campaign

        XSSFClientAnchor secondAnchor = new XSSFClientAnchor(0, 0, 0, 0, 0, 22, 12, 41);
        XSSFChart secondChart = drawing.createChart(secondAnchor);

        secondChart.setTitleText("Main Attendee Rate % vs Companion Rate % by Campaign");
        secondChart.setTitleOverlay(false);
        secondChart.getOrAddLegend().setPosition(LegendPosition.BOTTOM);

        XDDFCategoryAxis secondCategoryAxis = secondChart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis secondValueAxis = secondChart.createValueAxis(AxisPosition.LEFT);
        secondValueAxis.setTitle("Rate %");

        XDDFNumericalDataSource<Double> mainRateValues = XDDFDataSourcesFactory.fromNumericCellRange(
                summarySheet,
                new CellRangeAddress(firstDataRow, lastDataRow, 4, 4)
        );

        XDDFNumericalDataSource<Double> companionRateValues = XDDFDataSourcesFactory.fromNumericCellRange(
                summarySheet,
                new CellRangeAddress(firstDataRow, lastDataRow, 5, 5)
        );

        log.info("CHART DEBUG - Composition chart 2 ranges -> categories=A{}:A{}, mainRate=E{}:E{}, companionRate=F{}:F{}",
                firstDataRow + 1, lastDataRow + 1,
                firstDataRow + 1, lastDataRow + 1,
                firstDataRow + 1, lastDataRow + 1);

        XDDFBarChartData secondChartData = (XDDFBarChartData) secondChart.createData(
                ChartTypes.BAR,
                secondCategoryAxis,
                secondValueAxis
        );

        secondChartData.setBarDirection(BarDirection.COL);

        XDDFBarChartData.Series mainRateSeries =
                (XDDFBarChartData.Series) secondChartData.addSeries(categories, mainRateValues);
        mainRateSeries.setTitle("Main Attendee Rate %", null);

        XDDFBarChartData.Series companionRateSeries =
                (XDDFBarChartData.Series) secondChartData.addSeries(categories, companionRateValues);
        companionRateSeries.setTitle("Companion Rate %", null);

        log.info("CHART DEBUG - Composition chart 2 plotting with {} data rows.", aggregateRows.size());

        secondChart.plot(secondChartData);
    }

    /**
     * Creates the age analysis sheet and adds:
     * - the bar chart for average age by campaign
     * - the pie chart for overall age band distribution
     *
     * @param workbook the target workbook
     * @param summarySheet the summary sheet containing chart source data
     * @param aggregateRows the aggregated campaign rows
     */
    public void createAgeAnalysisSheet(XSSFWorkbook workbook,
                                       XSSFSheet summarySheet,
                                       List<CampaignAggregatedDataDTO> aggregateRows) {

        log.info("CHART DEBUG - createAgeAnalysisSheet started. aggregateRows={}",
                aggregateRows != null ? aggregateRows.size() : 0);

        // Create sheet
        XSSFSheet chartSheet = workbook.createSheet("Age Analysis");
        HelperExcelStylesheet.applyDefaultSheetLayout(chartSheet);

        // When no chart data is available
        if (aggregateRows == null || aggregateRows.isEmpty()) {
            log.info("CHART DEBUG - Age Analysis skipped because no chart data is available.");
            Row row = chartSheet.createRow(0);
            row.createCell(0).setCellValue("No chart data available.");
            return;
        }

        int firstDataRow = 1;
        int lastDataRow = aggregateRows.size();

        log.info("CHART DEBUG - Age Analysis source sheet='{}', firstDataRow={}, lastDataRow={}",
                summarySheet.getSheetName(), firstDataRow, lastDataRow);

        for (int rowIndex = firstDataRow; rowIndex <= lastDataRow; rowIndex++) {
            Row row = summarySheet.getRow(rowIndex);

            String campaignValue = readCellAsString(row, 0);
            String averageAgeValue = readCellAsString(row, 6);

            log.info("CHART DEBUG - Age Analysis row={}, campaign={}, averageAge={}",
                    rowIndex, campaignValue, averageAgeValue);
        }

        XDDFCategoryDataSource categories = XDDFDataSourcesFactory.fromStringCellRange(
                summarySheet,
                new CellRangeAddress(firstDataRow, lastDataRow, 0, 0)
        );

        XSSFDrawing drawing = chartSheet.createDrawingPatriarch();

        // --- Chart 1: Average Age by Campaign

        XSSFClientAnchor firstAnchor = new XSSFClientAnchor(0, 0, 0, 0, 0, 1, 12, 20);
        XSSFChart firstChart = drawing.createChart(firstAnchor);

        firstChart.setTitleText("Average Age by Campaign");
        firstChart.setTitleOverlay(false);
        firstChart.getOrAddLegend().setPosition(LegendPosition.BOTTOM);

        XDDFCategoryAxis firstCategoryAxis = firstChart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis firstValueAxis = firstChart.createValueAxis(AxisPosition.LEFT);
        firstValueAxis.setTitle("Average Age");

        XDDFNumericalDataSource<Double> averageAgeValues = XDDFDataSourcesFactory.fromNumericCellRange(
                summarySheet,
                new CellRangeAddress(firstDataRow, lastDataRow, 6, 6)
        );

        log.info("CHART DEBUG - Age Analysis chart 1 ranges -> categories=A{}:A{}, averageAge=G{}:G{}",
                firstDataRow + 1, lastDataRow + 1,
                firstDataRow + 1, lastDataRow + 1);

        XDDFBarChartData firstChartData = (XDDFBarChartData) firstChart.createData(
                ChartTypes.BAR,
                firstCategoryAxis,
                firstValueAxis
        );

        firstChartData.setBarDirection(BarDirection.COL);

        XDDFBarChartData.Series averageAgeSeries =
                (XDDFBarChartData.Series) firstChartData.addSeries(categories, averageAgeValues);
        averageAgeSeries.setTitle("Average Age", null);

        log.info("CHART DEBUG - Age Analysis chart 1 plotting with {} data rows.", aggregateRows.size());

        firstChart.plot(firstChartData);

        // --- Support data for pie chart: overall age band distribution

        int overallYoungCount = aggregateRows.stream()
                .mapToInt(CampaignAggregatedDataDTO::youngAttendeeCount)
                .sum();

        int overallAdultCount = aggregateRows.stream()
                .mapToInt(CampaignAggregatedDataDTO::adultAttendeeCount)
                .sum();

        int overallSeniorCount = aggregateRows.stream()
                .mapToInt(CampaignAggregatedDataDTO::seniorAttendeeCount)
                .sum();

        int overallUnknownCount = aggregateRows.stream()
                .mapToInt(CampaignAggregatedDataDTO::missingBirthDateCount)
                .sum();

        log.info("CHART DEBUG - Age Analysis pie totals -> young={}, adult={}, senior={}, unknown={}",
                overallYoungCount, overallAdultCount, overallSeniorCount, overallUnknownCount);

        Row supportRow1 = chartSheet.createRow(0);
        Row supportRow2 = chartSheet.createRow(1);
        Row supportRow3 = chartSheet.createRow(2);
        Row supportRow4 = chartSheet.createRow(3);

        supportRow1.createCell(20).setCellValue("Young");
        supportRow1.createCell(21).setCellValue(overallYoungCount);

        supportRow2.createCell(20).setCellValue("Adult");
        supportRow2.createCell(21).setCellValue(overallAdultCount);

        supportRow3.createCell(20).setCellValue("Senior");
        supportRow3.createCell(21).setCellValue(overallSeniorCount);

        supportRow4.createCell(20).setCellValue("Unknown");
        supportRow4.createCell(21).setCellValue(overallUnknownCount);

        log.info("CHART DEBUG - Age Analysis pie support data written to columns U:V, rows 1:4");

        // --- Chart 2: Age Band Distribution Overall

        XSSFClientAnchor secondAnchor = new XSSFClientAnchor(0, 0, 0, 0, 0, 22, 12, 41);
        XSSFChart secondChart = drawing.createChart(secondAnchor);

        secondChart.setTitleText("Age Band Distribution Overall");
        secondChart.setTitleOverlay(false);
        secondChart.getOrAddLegend().setPosition(LegendPosition.RIGHT);

        XDDFCategoryDataSource pieCategories = XDDFDataSourcesFactory.fromStringCellRange(
                chartSheet,
                new CellRangeAddress(0, 3, 20, 20)
        );

        XDDFNumericalDataSource<Double> pieValues = XDDFDataSourcesFactory.fromNumericCellRange(
                chartSheet,
                new CellRangeAddress(0, 3, 21, 21)
        );

        log.info("CHART DEBUG - Age Analysis pie ranges -> categories=U1:U4, values=V1:V4");

        XDDFPieChartData pieChartData = (XDDFPieChartData) secondChart.createData(
                ChartTypes.PIE,
                null,
                null
        );

        XDDFPieChartData.Series pieSeries = (XDDFPieChartData.Series) pieChartData.addSeries(pieCategories, pieValues);
        pieSeries.setTitle("Age Bands", null);

        log.info("CHART DEBUG - Age Analysis pie chart plotting.");

        secondChart.plot(pieChartData);
    }

    /**
     * Creates the data quality sheet and adds the bar chart
     * showing the data completeness rate for each campaign.
     *
     * @param workbook the target workbook
     * @param summarySheet the summary sheet containing chart source data
     * @param aggregateRows the aggregated campaign rows
     */
    public void createDataQualitySheet(XSSFWorkbook workbook,
                                       XSSFSheet summarySheet,
                                       List<CampaignAggregatedDataDTO> aggregateRows) {

        log.info("CHART DEBUG - createDataQualitySheet started. aggregateRows={}",
                aggregateRows != null ? aggregateRows.size() : 0);

        // Create sheet
        XSSFSheet chartSheet = workbook.createSheet("Data Quality");
        HelperExcelStylesheet.applyDefaultSheetLayout(chartSheet);

        // When no chart data is available
        if (aggregateRows == null || aggregateRows.isEmpty()) {
            log.info("CHART DEBUG - Data Quality skipped because no chart data is available.");
            Row row = chartSheet.createRow(0);
            row.createCell(0).setCellValue("No chart data available.");
            return;
        }

        int firstDataRow = 1;
        int lastDataRow = aggregateRows.size();

        log.info("CHART DEBUG - Data Quality source sheet='{}', firstDataRow={}, lastDataRow={}",
                summarySheet.getSheetName(), firstDataRow, lastDataRow);

        for (int rowIndex = firstDataRow; rowIndex <= lastDataRow; rowIndex++) {
            Row row = summarySheet.getRow(rowIndex);

            String campaignValue = readCellAsString(row, 0);
            String completenessValue = readCellAsString(row, 13);

            log.info("CHART DEBUG - Data Quality row={}, campaign={}, completeness={}",
                    rowIndex, campaignValue, completenessValue);
        }

        XDDFCategoryDataSource categories = XDDFDataSourcesFactory.fromStringCellRange(
                summarySheet,
                new CellRangeAddress(firstDataRow, lastDataRow, 0, 0)
        );

        XSSFDrawing drawing = chartSheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, 0, 1, 12, 20);
        XSSFChart chart = drawing.createChart(anchor);

        // Chart title and legend
        chart.setTitleText("Data Completeness Rate by Campaign");
        chart.setTitleOverlay(false);
        chart.getOrAddLegend().setPosition(LegendPosition.BOTTOM);

        // Chart axes
        XDDFCategoryAxis categoryAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis valueAxis = chart.createValueAxis(AxisPosition.LEFT);
        valueAxis.setTitle("Completeness %");

        // Values come from Summary column N: Data Completeness %
        XDDFNumericalDataSource<Double> completenessValues = XDDFDataSourcesFactory.fromNumericCellRange(
                summarySheet,
                new CellRangeAddress(firstDataRow, lastDataRow, 13, 13)
        );

        log.info("CHART DEBUG - Data Quality ranges -> categories=A{}:A{}, completeness=N{}:N{}",
                firstDataRow + 1, lastDataRow + 1,
                firstDataRow + 1, lastDataRow + 1);

        // Build chart data
        XDDFBarChartData chartData = (XDDFBarChartData) chart.createData(
                ChartTypes.BAR,
                categoryAxis,
                valueAxis
        );

        chartData.setBarDirection(BarDirection.COL);

        XDDFBarChartData.Series completenessSeries =
                (XDDFBarChartData.Series) chartData.addSeries(categories, completenessValues);
        completenessSeries.setTitle("Data Completeness %", null);

        log.info("CHART DEBUG - Data Quality plotting chart with {} data rows.", aggregateRows.size());

        // Render chart
        chart.plot(chartData);
    }

    private String readCellAsString(Row row, int cellIndex) {
        if (row == null) {
            return null;
        }

        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return null;
        }

        return cell.toString();
    }
}
