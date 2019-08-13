package com.atlassian.performance.tools.report.chart

import com.atlassian.performance.tools.report.Point
import com.atlassian.performance.tools.report.color.LabelColor
import com.atlassian.performance.tools.report.color.SeedLabelColor
import javax.json.Json
import javax.json.JsonObject

internal class ChartLine<X>(
    val data: List<Point<X>>,
    private val label: String,
    private val type: String,
    private val yAxisId: String,
    private val hidden: Boolean = false,
    private val cohort: String = "",
    private val color: LabelColor = SeedLabelColor()
) where X : Comparable<X> {
    fun toJson(): JsonObject {
        val dataBuilder = Json.createArrayBuilder()
        data.forEach { point ->
            dataBuilder.add(
                Json.createObjectBuilder()
                    .add("x", point.labelX())
                    .add("y", point.y)
                    .build()
            )
        }
        val chartDataBuilder = Json.createObjectBuilder()
        chartDataBuilder.add("type", type)
        chartDataBuilder.add("label", label)

        if (!cohort.isBlank()) {
            chartDataBuilder.add("cohort", cohort)
        }

        val colorCss = color.color(label).toCss()
        chartDataBuilder.add("borderColor", colorCss)
        chartDataBuilder.add("backgroundColor", colorCss)
        chartDataBuilder.add("fill", false)
        chartDataBuilder.add("data", dataBuilder)
        chartDataBuilder.add("yAxisID", yAxisId)
        chartDataBuilder.add("hidden", hidden)
        chartDataBuilder.add("lineTension", 0)

        return chartDataBuilder.build()
    }
}
