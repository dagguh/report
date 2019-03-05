package com.atlassian.performance.tools.report.chart

import com.atlassian.performance.tools.report.Dimension
import com.atlassian.performance.tools.report.TimeDatum
import com.atlassian.performance.tools.report.TimeSeries
import com.atlassian.performance.tools.report.vmstat.VmstatBottleneck
import java.math.MathContext
import java.time.Instant
import com.atlassian.performance.tools.infrastructure.api.metric.Dimension as LegacyDimension

internal class BottleneckTimelineChart {

    fun plot(
        bottlenecksPerNode: Map<String, TimeSeries<VmstatBottleneck.Bottleneck>>
    ): Chart<Instant> {
        val bottlenecks = bottlenecksPerNode.entries.first().value
        return plotNode(countBottlenecks(bottlenecks))
    }

    private fun plotNode(
        countsPerBottleneck: Map<VmstatBottleneck.Bottleneck, TimeSeries<Int>>
    ): Chart<Instant> = Chart(
        countsPerBottleneck.map { (bottleneck, nodes) ->
            ChartLine(
                data = nodes.data.map { Tick(it.start, it.value.toDouble(), MathContext(0)) },
                label = bottleneck.name,
                type = "bar",
                yAxisId = "node-count-axis",
                hidden = false
            )
        }
    )

    private fun countBottlenecks(
        bottlenecksOverTime: TimeSeries<VmstatBottleneck.Bottleneck>
    ): Map<VmstatBottleneck.Bottleneck, TimeSeries<Int>> {
        val allPossibleBottlenecks = VmstatBottleneck.Bottleneck.values().toList()
        val dimension = Dimension("bottleneck count", "#")
        return bottlenecksOverTime
            .data
            .flatMap { (time, currentBottleneck) ->
                val absentBottlenecks = allPossibleBottlenecks - currentBottleneck
                absentBottlenecks
                    .map { it to TimeDatum(time, 0) }
                    .plus(currentBottleneck to TimeDatum(time, 1))
            }
            .groupBy { it.first }
            .mapValues { (bottleneck, countsOverTime) ->
                TimeSeries(
                    name = "$bottleneck counts",
                    dimension = dimension,
                    data = countsOverTime.map { it.second },
                    reduction = { counts -> counts.sum() }
                )
            }
    }
}
