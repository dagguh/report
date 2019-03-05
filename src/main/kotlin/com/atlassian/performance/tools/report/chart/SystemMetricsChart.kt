package com.atlassian.performance.tools.report.chart

import com.atlassian.performance.tools.report.Dimension
import com.atlassian.performance.tools.report.TimeDatum
import com.atlassian.performance.tools.report.TimeSeries
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.json.Json
import javax.json.JsonObject

internal class SystemMetricsChart(
    private val title: String,
    private val allSeries: List<TimeSeries<Double>>,
    private val dimension: Dimension
) {
    private val chartAxis = ChartAxis(
        dimension.name,
        if (dimension.unit != null) "${dimension.name} [${dimension.unit}]" else dimension.name
    )

    fun toJson(): JsonObject {
        val dimensionSeries = allSeries.filter { it.dimension == dimension }
        val chartData = Chart(plot(dimensionSeries))
        return Json.createObjectBuilder()
            .add("title", title)
            .add("axis", chartAxis.toJson())
            .add("data", chartData.toJson())
            .build()
    }

    private fun plot(
        series: List<TimeSeries<Double>>
    ): List<ChartLine<Instant>> = series
        .sortedBy { it.name }
        .map { plot(it) }

    private fun plot(
        series: TimeSeries<Double>
    ): ChartLine<Instant> = ChartLine(
        label = series.name,
        type = "line",
        yAxisId = chartAxis.id,
        data = series
            .data
            .groupBy { it.start.truncatedTo(ChronoUnit.MINUTES) }
            .map { (time, data) -> tick(time, data, series) },
        hidden = false
    )

    private fun tick(
        time: Instant,
        data: List<TimeDatum<Double>>,
        series: TimeSeries<Double>
    ): Tick = Tick(
        time,
        series.reduction(data.map { it.value })
    )
}
