package com.atlassian.performance.tools.report.chart

import com.atlassian.performance.tools.infrastructure.api.metric.Dimension
import com.atlassian.performance.tools.infrastructure.api.metric.SystemMetric
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.json.Json
import javax.json.JsonObject

internal class SystemMetricsChart(
    private val title: String,
    private val allMetrics: List<SystemMetric>,
    private val dimension: Dimension,
    private val axis: ChartAxis
) {
    constructor(
        allMetrics: List<SystemMetric>,
        dimension: Dimension,
        axisId: String
    ) : this(
        title = dimension.description,
        allMetrics = allMetrics,
        dimension = dimension,
        axis = ChartAxis(
            id = axisId,
            text = dimension.description
        )
    )

    fun toJson(): JsonObject {
        val dimensionMetrics = allMetrics
            .filter { it.dimension == dimension }
            .sortedBy { it.start }
        val chartData = Chart(plot(dimensionMetrics))
        return Json.createObjectBuilder()
            .add("title", title)
            .add("axis", axis.toJson())
            .add("data", chartData.toJson())
            .build()
    }

    private fun plot(
        metrics: List<SystemMetric>
    ): List<ChartLine<Instant>> = metrics
        .groupBy { it.system }
        .entries
        .sortedBy { it.key }
        .map { (system, systemMetrics) -> plot(system, systemMetrics) }

    private fun plot(
        system: String,
        systemMetrics: List<SystemMetric>
    ): ChartLine<Instant> = ChartLine(
        label = system,
        type = "line",
        yAxisId = axis.id,
        data = systemMetrics
            .groupBy { it.start.truncatedTo(ChronoUnit.MINUTES) }
            .map { (time, groupedMetrics) -> Tick(time, reduce(groupedMetrics)) },
        hidden = false
    )

    private fun reduce(
        metrics: List<SystemMetric>
    ): Double = dimension
        .reduction
        .lambda(
            metrics.map { it.value }
        )
}
