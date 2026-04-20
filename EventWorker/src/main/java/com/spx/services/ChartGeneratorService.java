package com.spx.services;

import com.spx.dto.CampaignAggregatedDataDTO;
import com.spx.helper.HelperExcelCharts;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * The type Chart generator service.
 */
/*
 * Service responsible for generating the dashboard chart sheets
 * It defines:
 * - the target worksheet for each chart group
 * - the chart type to use for each business view
 * - the data source written into hidden support columns
 * - the visual order of charts inside the workbook
 */
@Service
@Slf4j
public class ChartGeneratorService {

    // Hidden support tables start from column U (index 20).
    private static final int SUPPORT_START_COLUMN = 20;

    private static final int OVERALL_SUPPORT_START_COLUMN = SUPPORT_START_COLUMN + 3;

    // Shared chart anchors
    private static final int CHART_LEFT_COLUMN = 0;
    private static final int CHART_RIGHT_COLUMN = 12;
    private static final int FIRST_CHART_TOP_ROW = 1;
    private static final int FIRST_CHART_BOTTOM_ROW = 20;
    private static final int SECOND_CHART_TOP_ROW = 22;
    private static final int SECOND_CHART_BOTTOM_ROW = 45;

    // Doughnut constants
    private static final int DOUGHNUT_LEFT_COLUMN = 3;
    private static final int DOUGHNUT_RIGHT_COLUMN = 11;

    private static final int DOUGHNUT_CENTER_LEFT_COLUMN = 3;
    private static final int DOUGHNUT_CENTER_RIGHT_COLUMN = 11;
    private static final int DOUGHNUT_CENTER_TOP_ROW = SECOND_CHART_TOP_ROW + 7;
    private static final int DOUGHNUT_CENTER_BOTTOM_ROW = SECOND_CHART_BOTTOM_ROW - 7;


    /* --- CREATING SHEETS --- */

    /**
     * Creates the attendance overview sheet and adds a horizontal bar chart
     * showing total attendees by campaign.
     *
     * @param workbook       the target workbook
     * @param aggregatedRows the aggregated campaign rows
     */
    public void createAttendanceOverviewSheet(XSSFWorkbook workbook, List<CampaignAggregatedDataDTO> aggregatedRows) {

        log.info("Building Attendance Overview chart sheet.");

        // Create a new horizontal bar chart
        XSSFSheet chartSheet = HelperExcelCharts.createChartSheet(workbook, "Total Attendees by Campaign");

        // Stop early when no dashboard data is available.
        if (writeNoDataMessageIfNeeded(chartSheet, aggregatedRows, "No attendance chart data available.")) {
            return;
        }

        // Sort campaigns by attendee count so the chart reads like a ranking.
        List<CampaignAggregatedDataDTO> sortedRows = sortByAttendeeCountDesc(aggregatedRows);

        // Write the hidden support table used as the chart source.
        HelperExcelCharts.writeHeaders(chartSheet, SUPPORT_START_COLUMN, "Campaign", "Attendees");

        int rowIndex = 1;

        for (CampaignAggregatedDataDTO aggregatedRow : sortedRows) {
            HelperExcelCharts.writeTextCell(chartSheet, rowIndex, SUPPORT_START_COLUMN, aggregatedRow.campaignDisplayName());
            HelperExcelCharts.writeNumericCell(chartSheet, rowIndex, SUPPORT_START_COLUMN + 1, aggregatedRow.attendeeCount());

            rowIndex++;
        }

        // Support table for the overall doughnut chart
        int totalMainAttendees = aggregatedRows
                .stream()
                .mapToInt(CampaignAggregatedDataDTO::mainAttendeeCount)
                .sum();

        int totalCompanions = aggregatedRows
                .stream()
                .mapToInt(CampaignAggregatedDataDTO::companionCount)
                .sum();


        int totalAttendees = totalMainAttendees + totalCompanions;

        HelperExcelCharts.writeHeaders(chartSheet, OVERALL_SUPPORT_START_COLUMN, "Type", "Count");
        HelperExcelCharts.writeTextCell(chartSheet, 1, OVERALL_SUPPORT_START_COLUMN, "Main Attendees");
        HelperExcelCharts.writeNumericCell(chartSheet, 1, OVERALL_SUPPORT_START_COLUMN + 1, totalMainAttendees);

        HelperExcelCharts.writeTextCell(chartSheet, 2, OVERALL_SUPPORT_START_COLUMN, "Companions");
        HelperExcelCharts.writeNumericCell(chartSheet, 2, OVERALL_SUPPORT_START_COLUMN + 1, totalCompanions);


        // Render the chart
       createAttendanceOverviewChart(chartSheet, sortedRows.size());
       createOverallCompositionChart(chartSheet, totalAttendees);

        HelperExcelCharts.hideSupportColumns(chartSheet, SUPPORT_START_COLUMN, 5);
    }

    /**
     * Creates the composition sheet and adds:
     * - clustered vertical columns for main attendees vs companions
     * - a line chart with markers for main attendee rate vs companion rate
     *
     * @param workbook       the target workbook
     * @param aggregatedRows the aggregated campaign rows
     */
    public void createCompositionSheet(XSSFWorkbook workbook, List<CampaignAggregatedDataDTO> aggregatedRows) {

        log.info("Building Composition chart sheet.");

        // Create a new chart sheet
        XSSFSheet chartSheet = HelperExcelCharts.createChartSheet(workbook, "Attendee Composition by Campaign");

        // Stop early when no dashboard data is available.
        if (writeNoDataMessageIfNeeded(chartSheet, aggregatedRows, "No composition chart data available.")) {
            return;
        }

        // Keep the same ordering across both charts in the same sheet.
        List<CampaignAggregatedDataDTO> sortedRows = sortByAttendeeCountDesc(aggregatedRows);

        // Write the support table for both charts in this worksheet.
        HelperExcelCharts.writeHeaders(chartSheet, SUPPORT_START_COLUMN, "Campaign", "Main Attendees", "Companions", "Main Attendee Rate", "Companion Rate");

        int rowIndex = 1;

        // Create the rows
        for (CampaignAggregatedDataDTO aggregatedRow : sortedRows) {

            HelperExcelCharts.writeTextCell(chartSheet, rowIndex, SUPPORT_START_COLUMN, aggregatedRow.campaignDisplayName());
            HelperExcelCharts.writeNumericCell(chartSheet, rowIndex,SUPPORT_START_COLUMN + 1, aggregatedRow.mainAttendeeCount());
            HelperExcelCharts.writeNumericCell(chartSheet, rowIndex,SUPPORT_START_COLUMN + 2, aggregatedRow.companionCount() );
            HelperExcelCharts.writeNumericCell(chartSheet, rowIndex,SUPPORT_START_COLUMN + 3, aggregatedRow.mainAttendeeRate());
            HelperExcelCharts.writeNumericCell(chartSheet, rowIndex,SUPPORT_START_COLUMN + 4, aggregatedRow.companionRate());

            rowIndex++;
        }

        // Render the absolute-count chart first, then the percentage chart below it
        createMainAttendeesVsCompanionsChart(chartSheet, sortedRows.size());
        createCompositionRateChart(chartSheet, sortedRows.size());

        // Hide technical columns to keep the worksheet visually clean
        HelperExcelCharts.hideSupportColumns(chartSheet, SUPPORT_START_COLUMN, 5);
    }

    /**
     * Creates the age analysis sheet and adds:
     * - a line chart with markers for average age by campaign
     * - a doughnut chart for overall age distribution
     *
     * @param workbook       the target workbook
     * @param aggregatedRows the aggregated campaign rows
     */
    public void createAgeAnalysisSheet(XSSFWorkbook workbook, List<CampaignAggregatedDataDTO> aggregatedRows) {

        log.info("Building Age Analysis chart sheet.");

        XSSFSheet chartSheet = HelperExcelCharts.createChartSheet(workbook, "Age Analysis");

        // Stop early when no dashboard data is available.
        if (writeNoDataMessageIfNeeded(chartSheet, aggregatedRows, "No age analysis chart data available.")) {
            return;
        }

        // Write the support table for average age by campaign.
        HelperExcelCharts.writeHeaders(chartSheet, SUPPORT_START_COLUMN, "Campaign", "Average Age");

        int rowIndex = 1;

        for (CampaignAggregatedDataDTO aggregatedRow : aggregatedRows) {
            HelperExcelCharts.writeTextCell(chartSheet, rowIndex, SUPPORT_START_COLUMN, aggregatedRow.campaignDisplayName());
            HelperExcelCharts.writeNumericCell(chartSheet, rowIndex,SUPPORT_START_COLUMN + 1, aggregatedRow.averageAge());

            rowIndex++;
        }

        // Write the support table for the overall age distribution chart.
        int ageDistributionStartColumn = SUPPORT_START_COLUMN + 4;

        int overallYoungCount = aggregatedRows.stream()
                .mapToInt(CampaignAggregatedDataDTO::youngAttendeeCount)
                .sum();

        int overallAdultCount = aggregatedRows.stream()
                .mapToInt(CampaignAggregatedDataDTO::adultAttendeeCount)
                .sum();

        int overallSeniorCount = aggregatedRows.stream()
                .mapToInt(CampaignAggregatedDataDTO::seniorAttendeeCount)
                .sum();

        int overallUnknownCount = aggregatedRows.stream()
                .mapToInt(CampaignAggregatedDataDTO::missingBirthDateCount)
                .sum();

        HelperExcelCharts.writeHeaders(chartSheet, ageDistributionStartColumn, "Age Band", "Count");

        HelperExcelCharts.writeTextCell(chartSheet, 1, ageDistributionStartColumn, "Young (<=29)");
        HelperExcelCharts.writeNumericCell(chartSheet, 1, ageDistributionStartColumn + 1, overallYoungCount);

        HelperExcelCharts.writeTextCell(chartSheet, 2, ageDistributionStartColumn, "Adult (30-49)");
        HelperExcelCharts.writeNumericCell(chartSheet, 2, ageDistributionStartColumn + 1, overallAdultCount);

        HelperExcelCharts.writeTextCell(chartSheet, 3, ageDistributionStartColumn, "Senior (50+)");
        HelperExcelCharts.writeNumericCell(chartSheet, 3, ageDistributionStartColumn + 1, overallSeniorCount);

        HelperExcelCharts.writeTextCell(chartSheet, 4, ageDistributionStartColumn, "Unknown");
        HelperExcelCharts.writeNumericCell(chartSheet, 4, ageDistributionStartColumn + 1, overallUnknownCount);

        HelperExcelCharts.hideSupportColumns(chartSheet, SUPPORT_START_COLUMN, 2);
        HelperExcelCharts.hideSupportColumns(chartSheet, ageDistributionStartColumn, 2);

        // Render the campaign-level chart first, then the overall distribution chart below it.
        createAverageAgeByCampaignChart(chartSheet, aggregatedRows.size());
        createOverallAgeDistributionChart(chartSheet, ageDistributionStartColumn);
    }

    /**
     * Creates the data completeness sheet and adds a doughnut chart
     * showing data completeness by campaign.
     *
     * @param workbook       the target workbook
     * @param aggregatedRows the aggregated campaign rows
     */
    public void createDataCompletenessSheet(XSSFWorkbook workbook, List<CampaignAggregatedDataDTO> aggregatedRows) {

        log.info("Building Data Completeness chart sheet.");

        XSSFSheet chartSheet = HelperExcelCharts.createChartSheet(workbook, "Data Completeness");

        // Stop early when no dashboard data is available.
        if (writeNoDataMessageIfNeeded(chartSheet, aggregatedRows, "No data completeness chart data available.")) {
            return;
        }

        // Write the support table used by the doughnut chart.
        HelperExcelCharts.writeHeaders(chartSheet, SUPPORT_START_COLUMN, "Campaign", "Completeness");

        int rowIndex = 1;
        for (CampaignAggregatedDataDTO aggregatedRow : aggregatedRows) {
            HelperExcelCharts.writeTextCell(chartSheet, rowIndex, SUPPORT_START_COLUMN, aggregatedRow.campaignDisplayName());
            HelperExcelCharts.writeNumericCell(chartSheet, rowIndex,SUPPORT_START_COLUMN + 1, aggregatedRow.dataCompletenessRate());

            rowIndex++;
        }

        HelperExcelCharts.hideSupportColumns(chartSheet, SUPPORT_START_COLUMN, 2);

        createDataCompletenessChart(chartSheet, aggregatedRows.size());
    }


    /* --- CREATING CHARTS --- */

    // FIRST SHEET (after Summary sheet)

    /**
     * Creates the horizontal bar chart for the attendance overview sheet.
     *
     * @param chartSheet the target chart sheet
     * @param campaignCount the number of campaigns in the support table
     */
    private void createAttendanceOverviewChart(XSSFSheet chartSheet, int campaignCount) {

        XSSFChart chart = HelperExcelCharts.createChart(chartSheet, CHART_LEFT_COLUMN, FIRST_CHART_TOP_ROW, CHART_RIGHT_COLUMN, FIRST_CHART_BOTTOM_ROW,
                "Total Attendees by Campaign", LegendPosition.RIGHT
        );

        // Configure axes for a horizontal ranking chart.
        XDDFCategoryAxis categoryAxis = chart.createCategoryAxis(AxisPosition.LEFT);
        XDDFValueAxis valueAxis = HelperExcelCharts.createCountAxis(chart, AxisPosition.BOTTOM,"Number of Attendees");
        categoryAxis.crossAxis(valueAxis);
        valueAxis.crossAxis(categoryAxis);

        XDDFCategoryDataSource categories = HelperExcelCharts.categorySource(chartSheet,1, campaignCount ,SUPPORT_START_COLUMN);
        XDDFNumericalDataSource<Double> values = HelperExcelCharts.numericSource(chartSheet,1, campaignCount,SUPPORT_START_COLUMN + 1);
        XDDFBarChartData chartData = (XDDFBarChartData) chart.createData(ChartTypes.BAR, categoryAxis, valueAxis);

        chartData.setBarDirection(BarDirection.BAR);
        chartData.setBarGrouping(BarGrouping.CLUSTERED);
        chartData.setGapWidth(35);

        XDDFBarChartData.Series series = (XDDFBarChartData.Series) chartData.addSeries(categories, values);
        series.setTitle("Attendees", null);

        // Add labels at the end of the horizontal bars
        HelperExcelCharts.addOutsideValueLabelsToBar(series);

        chart.plot(chartData);
    }

    /**
     * Creates the doughnut chart for the overall composition sheet.
     *
     * @param chartSheet the target chart sheet
     * @param totalAttendees the number of total of attendees' campaigns
     */
    private void createOverallCompositionChart(XSSFSheet chartSheet, int totalAttendees) {

        XSSFChart chart = HelperExcelCharts.createChart(chartSheet, DOUGHNUT_LEFT_COLUMN, SECOND_CHART_TOP_ROW, DOUGHNUT_RIGHT_COLUMN,
                                                        SECOND_CHART_BOTTOM_ROW,"Overall Attendee Composition", LegendPosition.RIGHT
        );

        XDDFCategoryDataSource categories = HelperExcelCharts.categorySource( chartSheet, 1,  2,  OVERALL_SUPPORT_START_COLUMN);

        XDDFNumericalDataSource<Double> values = HelperExcelCharts.numericSource( chartSheet,  1,2,OVERALL_SUPPORT_START_COLUMN + 1);

        XDDFDoughnutChartData chartData = (XDDFDoughnutChartData) chart.createData(ChartTypes.DOUGHNUT, null,null);

        chartData.setVaryColors(true);
        chartData.setHoleSize(65);

        XDDFChartData.Series series = chartData.addSeries(categories, values);
        series.setTitle("Overall Composition", null);

        chart.plot(chartData);

        // Percentage labels on the slices
        HelperExcelCharts.addPercentageLabelsToDoughnut(chart);

        // Number in the center
        HelperExcelCharts.addCenteredChartText(chartSheet, DOUGHNUT_CENTER_LEFT_COLUMN, DOUGHNUT_CENTER_TOP_ROW, DOUGHNUT_CENTER_RIGHT_COLUMN,
                                                DOUGHNUT_CENTER_BOTTOM_ROW, "Total Attendees", String.valueOf(totalAttendees)
        );
    }

    // SECOND SHEET

    /**
     * Creates the clustered vertical column chart for main attendees vs companions.
     *
     * @param chartSheet the target chart sheet
     * @param campaignCount the number of campaigns in the support table
     */
    private void createMainAttendeesVsCompanionsChart(XSSFSheet chartSheet, int campaignCount) {

        XSSFChart chart = HelperExcelCharts.createChart(chartSheet, CHART_LEFT_COLUMN, FIRST_CHART_TOP_ROW, CHART_RIGHT_COLUMN, FIRST_CHART_BOTTOM_ROW,
                "Main Attendees vs Companions by Campaign", LegendPosition.BOTTOM
        );

        XDDFCategoryAxis categoryAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis valueAxis = HelperExcelCharts.createCountAxis(chart, AxisPosition.LEFT, "Number of Attendees");

        XDDFCategoryDataSource categories = HelperExcelCharts.categorySource( chartSheet, 1,campaignCount, SUPPORT_START_COLUMN);

        XDDFNumericalDataSource<Double> mainValues = HelperExcelCharts.numericSource(chartSheet,1, campaignCount,SUPPORT_START_COLUMN + 1);
        XDDFNumericalDataSource<Double> companionValues = HelperExcelCharts.numericSource(chartSheet, 1,  campaignCount,  SUPPORT_START_COLUMN + 2);
        XDDFBarChartData chartData = (XDDFBarChartData) chart.createData(ChartTypes.BAR,categoryAxis,valueAxis );

        chartData.setBarDirection(BarDirection.COL);
        chartData.setBarGrouping(BarGrouping.CLUSTERED);
        chartData.setGapWidth(45);

        XDDFBarChartData.Series mainSeries =  (XDDFBarChartData.Series) chartData.addSeries(categories, mainValues);
        mainSeries.setTitle("Main Attendees", null);

        XDDFBarChartData.Series companionSeries =   (XDDFBarChartData.Series) chartData.addSeries(categories, companionValues);
        companionSeries.setTitle("Companions", null);

        // Add value labels above the vertical columns
        HelperExcelCharts.addOutsideValueLabelsToBar(mainSeries);
        HelperExcelCharts.addOutsideValueLabelsToBar(companionSeries);

        chart.plot(chartData);
    }

    /**
     * Creates the 100% stacked horizontal bar chart for main attendees vs companions by campaign.
     *
     * @param chartSheet the target chart sheet
     * @param campaignCount the number of campaigns in the support table
     */
    private void createCompositionRateChart(XSSFSheet chartSheet, int campaignCount) {

        XSSFChart chart = HelperExcelCharts.createChart(chartSheet, CHART_LEFT_COLUMN, SECOND_CHART_TOP_ROW,
                                                        CHART_RIGHT_COLUMN, SECOND_CHART_BOTTOM_ROW,
                                                    "Main vs Companion Share by Campaign (%)", LegendPosition.RIGHT
        );

        // Horizontal composition chart: campaigns on the left, percentage share on the bottom.
        XDDFCategoryAxis categoryAxis = chart.createCategoryAxis(AxisPosition.LEFT);
        XDDFValueAxis valueAxis = HelperExcelCharts.createValueAxis(chart, AxisPosition.BOTTOM,"Share (%)",
                                                            0d,100d, "0%"
        );

        categoryAxis.crossAxis(valueAxis);
        valueAxis.crossAxis(categoryAxis);

        XDDFCategoryDataSource categories = HelperExcelCharts.categorySource(chartSheet, 1, campaignCount, SUPPORT_START_COLUMN);

        XDDFNumericalDataSource<Double> mainRateValues = HelperExcelCharts.numericSource(chartSheet,1, campaignCount,SUPPORT_START_COLUMN + 3);

        XDDFNumericalDataSource<Double> companionRateValues = HelperExcelCharts.numericSource(chartSheet,1,campaignCount,SUPPORT_START_COLUMN + 4);

        XDDFBarChartData chartData = (XDDFBarChartData) chart.createData(ChartTypes. BAR, categoryAxis, valueAxis);

        chartData.setBarDirection(BarDirection.BAR);
        chartData.setBarGrouping(BarGrouping.STACKED);
        chartData.setOverlap((byte) 100);
        chartData.setGapWidth(45);

        XDDFBarChartData.Series mainRateSeries = (XDDFBarChartData.Series) chartData.addSeries(categories, mainRateValues);
        mainRateSeries.setTitle("Main Attendees", null);

        XDDFBarChartData.Series companionRateSeries = (XDDFBarChartData.Series) chartData.addSeries(categories, companionRateValues);
        companionRateSeries.setTitle("Companions", null);

        chart.plot(chartData);

        HelperExcelCharts.addPercentageLabelsToStackedBar(chart);
    }

    // THIRD SHEET

    /**
     * Creates the line chart with markers for average age by campaign.
     *
     * @param chartSheet the target chart sheet
     * @param campaignCount the number of campaigns in the support table
     */
    private void createAverageAgeByCampaignChart(XSSFSheet chartSheet, int campaignCount) {

        XSSFChart chart = HelperExcelCharts.createChart(chartSheet, CHART_LEFT_COLUMN, FIRST_CHART_TOP_ROW, CHART_RIGHT_COLUMN, FIRST_CHART_BOTTOM_ROW,
                                                "Average Age by Campaign",LegendPosition.BOTTOM);

        XDDFCategoryAxis categoryAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis valueAxis = HelperExcelCharts.createValueAxis(
                chart,  AxisPosition.LEFT,

                "Average Age",
                null,
                null,
                "0.0"
        );

        XDDFCategoryDataSource categories = HelperExcelCharts.categorySource(chartSheet, 1, campaignCount, SUPPORT_START_COLUMN);

        XDDFNumericalDataSource<Double> averageAgeValues = HelperExcelCharts.numericSource(chartSheet, 1,campaignCount,SUPPORT_START_COLUMN + 1);

        XDDFLineChartData chartData = (XDDFLineChartData) chart.createData(ChartTypes.LINE, categoryAxis, valueAxis);

        XDDFLineChartData.Series series = (XDDFLineChartData.Series) chartData.addSeries(categories, averageAgeValues);

        HelperExcelCharts.configureLineSeries(series, "Average Age", MarkerStyle.CIRCLE);

        chart.plot(chartData);
    }

    /**
     * Creates the doughnut chart for the overall age distribution.
     *
     * @param chartSheet the target chart sheet
     * @param ageDistributionStartColumn the first support column for age distribution data
     */
    private void createOverallAgeDistributionChart(XSSFSheet chartSheet, int ageDistributionStartColumn) {

        XSSFChart chart = HelperExcelCharts.createChart(chartSheet, CHART_LEFT_COLUMN,SECOND_CHART_TOP_ROW, CHART_RIGHT_COLUMN,
                                                        SECOND_CHART_BOTTOM_ROW,"Overall Age Distribution", LegendPosition.RIGHT
        );

        XDDFCategoryDataSource categories = HelperExcelCharts.categorySource(chartSheet,1,4, ageDistributionStartColumn);
        XDDFNumericalDataSource<Double> values = HelperExcelCharts.numericSource(chartSheet,1,4,ageDistributionStartColumn + 1);
        XDDFChartData chartData = chart.createData(ChartTypes.DOUGHNUT, null, null);
        XDDFChartData.Series series = chartData.addSeries(categories, values);
        series.setTitle("Age Distribution", null);

        chart.plot(chartData);
    }

    // FOURTH SHEET

    /**
     * Creates the doughnut chart for data completeness by campaign.
     *
     * @param chartSheet the target chart sheet
     * @param campaignCount the number of campaigns in the support table
     */
    private void createDataCompletenessChart(XSSFSheet chartSheet, int campaignCount) {

        XSSFChart chart = HelperExcelCharts.createChart(chartSheet,CHART_LEFT_COLUMN, FIRST_CHART_TOP_ROW, CHART_RIGHT_COLUMN,
                                                       SECOND_CHART_BOTTOM_ROW,"Data Completeness by Campaign", LegendPosition.RIGHT);

        XDDFCategoryDataSource categories = HelperExcelCharts.categorySource(chartSheet,1, campaignCount, SUPPORT_START_COLUMN);
        XDDFNumericalDataSource<Double> values = HelperExcelCharts.numericSource(chartSheet, 1,campaignCount,SUPPORT_START_COLUMN + 1);
        XDDFChartData chartData = chart.createData(ChartTypes.DOUGHNUT, null, null);
        XDDFChartData.Series series = chartData.addSeries(categories, values);
        series.setTitle("Completeness (%)", null);

        chart.plot(chartData);
    }


    /* --- OTHERS FUNCTIONS --- */

    /**
     * Writes a fallback message and returns true when the dataset is empty.
     *
     * @param chartSheet the target chart sheet
     * @param aggregatedRows the source rows
     * @param message the fallback message
     * @return true when no data is available, false otherwise
     */
    private boolean writeNoDataMessageIfNeeded(XSSFSheet chartSheet, List<CampaignAggregatedDataDTO> aggregatedRows, String message) {
        if (aggregatedRows == null || aggregatedRows.isEmpty()) {
            HelperExcelCharts.writeNoDataMessage(chartSheet, message);
            return true;
        }

        return false;
    }

    /**
     * Sorts the aggregated rows by attendee count in descending order.
     *
     * @param aggregatedRows the source rows
     * @return the sorted rows
     */
    private List<CampaignAggregatedDataDTO> sortByAttendeeCountDesc(List<CampaignAggregatedDataDTO> aggregatedRows) {
        return aggregatedRows
                .stream()
                .sorted(Comparator.comparingInt(CampaignAggregatedDataDTO::attendeeCount).reversed())
                .toList();
    }
}