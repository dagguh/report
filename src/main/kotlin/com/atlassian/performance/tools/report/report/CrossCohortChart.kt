package com.atlassian.performance.tools.report.report

import com.atlassian.performance.tools.report.api.result.InteractionStats
import com.atlassian.performance.tools.report.api.result.RawCohortResult
import com.atlassian.performance.tools.report.chart.MeanLatencyChart
import com.atlassian.performance.tools.report.metric.CrossMetric
import com.atlassian.performance.tools.report.metric.Metric
import java.io.File

class CrossCohortChart(
    private val stats: Metric<InteractionStats>,
    private val actionLabels: CrossMetric<List<String>>
) : Report {

    override fun analyze(
        results: List<RawCohortResult>,
        outputDirectory: File
    ) {
        MeanLatencyChart().plot(
            stats = results.map { stats.measure(it) },
            labels = actionLabels.measure(results),
            output = outputDirectory.resolve("mean-latency-chart.html")
        )
    }
}
