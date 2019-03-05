package com.atlassian.performance.tools.report.chart

import com.atlassian.performance.tools.infrastructure.api.metric.SystemMetric
import com.atlassian.performance.tools.io.api.ensureParentDirectory
import com.atlassian.performance.tools.jiraactions.api.ActionMetric
import com.atlassian.performance.tools.report.Dimension
import com.atlassian.performance.tools.report.JsonStyle
import com.atlassian.performance.tools.report.TimeDatum
import com.atlassian.performance.tools.report.TimeSeries
import com.atlassian.performance.tools.report.vmstat.VmstatBottleneck
import com.atlassian.performance.tools.workspace.api.git.GitRepo
import org.apache.logging.log4j.LogManager
import java.nio.file.Path
import java.time.Instant
import javax.json.Json
import javax.json.JsonArray
import com.atlassian.performance.tools.infrastructure.api.metric.Dimension as LegacyDimension

internal class TimelineChart(
    private val repo: GitRepo
) {
    private val logger = LogManager.getLogger(this::class.java)
    private val dimensions = listOf(
        LegacyDimension.CPU_LOAD,
        LegacyDimension.JSTAT_SURVI_0,
        LegacyDimension.JSTAT_SURVI_1,
        LegacyDimension.JSTAT_EDEN,
        LegacyDimension.JSTAT_OLD,
        LegacyDimension.JSTAT_COMPRESSED_CLASS,
        LegacyDimension.JSTAT_YOUNG_GEN_GC,
        LegacyDimension.JSTAT_YOUNG_GEN_GC_TIME,
        LegacyDimension.JSTAT_FULL_GC,
        LegacyDimension.JSTAT_FULL_GC_TIME,
        LegacyDimension.JSTAT_TOTAL_GC_TIME
    ).map { Dimension(it) }

    fun generate(
        output: Path,
        actionMetrics: List<ActionMetric>,
        systemMetrics: List<SystemMetric>,
        bottlenecksChart: Chart<Instant>
    ) {
        val trimmedSystemMetrics = trimSystemMetrics(actionMetrics, systemMetrics)
        val systemSeries = convert(trimmedSystemMetrics)
        val report = this::class
            .java
            .getResourceAsStream("timeline-chart-template.html")
            .bufferedReader()
            .use { it.readText() }
            .replace(
                oldValue = "'<%= virtualUserChartData =%>'",
                newValue = JsonStyle().prettyPrint(ChartBuilder().build(actionMetrics).toJson())
            )
            .replace(
                oldValue = "'<%= bottleneckChartData =%>'",
                newValue = JsonStyle().prettyPrint(bottlenecksChart.toJson())
            )
            .replace(
                oldValue = "'<%= systemMetricsCharts =%>'",
                newValue = JsonStyle().prettyPrint(systemMetricsCharts(systemSeries))
            )
            .replace(
                oldValue = "'<%= commit =%>'",
                newValue = repo.getHead()
            )
        output.toFile().ensureParentDirectory().printWriter().use { it.print(report) }
        logger.info("Timeline chart available at ${output.toUri()}")
    }

    private fun systemMetricsCharts(
        seriesPerDimension: Map<Dimension, List<TimeSeries<Double>>>
    ): JsonArray {
        val json = Json.createArrayBuilder()
        dimensions
            .mapNotNull {
                SystemMetricsChart(
                    title = it.name,
                    allSeries = seriesPerDimension[it] ?: return@mapNotNull null,
                    dimension = it
                )
            }
            .map { it.toJson() }
            .forEach { json.add(it) }
        return json.build()
    }

    private fun trimSystemMetrics(
        actionMetrics: List<ActionMetric>,
        systemMetrics: List<SystemMetric>
    ): List<SystemMetric> {
        if (actionMetrics.isEmpty()) {
            return emptyList()
        }

        val metricsSortedByTime = actionMetrics
            .sortedBy { it.start }

        val beginning = metricsSortedByTime
            .first()
            .start

        val end = metricsSortedByTime
            .last()
            .start

        return systemMetrics
            .filter { it.start.isAfter(beginning) }
            .filter { it.start.isBefore(end) }
    }

    private fun convert(
        allSystemMetrics: List<SystemMetric>
    ): Map<Dimension, List<TimeSeries<Double>>> = allSystemMetrics
        .groupBy { it.dimension }
        .mapValues { (legacyDimension, dimensionMetrics) ->
            dimensionMetrics
                .groupBy { it.system }
                .map { (system, systemMetrics) ->
                    TimeSeries(
                        name = system,
                        data = systemMetrics.map { TimeDatum(it.start, it.value) },
                        dimension = Dimension(legacyDimension),
                        reduction = legacyDimension.reduction.lambda
                    )
                }
        }
        .mapKeys { (legacyDimension, _) -> Dimension(legacyDimension) }
}