package com.atlassian.performance.tools.report.report

import com.atlassian.performance.tools.report.api.DataReporter
import com.atlassian.performance.tools.report.api.result.InteractionStats
import com.atlassian.performance.tools.report.api.result.RawCohortResult
import com.atlassian.performance.tools.report.metric.CrossMetric
import com.atlassian.performance.tools.report.metric.Metric
import java.io.File

class CrossCohortTable(
    val stats: Metric<InteractionStats>,
    val labels: CrossMetric<List<String>>
) : Report {

    override fun analyze(
        results: List<RawCohortResult>,
        outputDirectory: File
    ) {
        DataReporter(
            output = outputDirectory.resolve("summary-per-cohort.csv"),
            labels = labels.measure(results)
        ).report(
            data = results.map { stats.measure(it) }
        )
    }
}
