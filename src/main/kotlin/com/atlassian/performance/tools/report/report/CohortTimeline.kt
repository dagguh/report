package com.atlassian.performance.tools.report.report

import com.atlassian.performance.tools.infrastructure.api.metric.SystemMetric
import com.atlassian.performance.tools.jiraactions.api.ActionMetric
import com.atlassian.performance.tools.report.api.result.RawCohortResult
import com.atlassian.performance.tools.report.chart.TimelineChart
import com.atlassian.performance.tools.report.metric.Metric
import com.atlassian.performance.tools.workspace.api.git.GitRepo
import java.io.File

// TODO refactor to accept (a list of?) charts
// TODO make the charts either easy to reproduce or easy to extract from the "default"
class CohortTimeline(
    private val actionMetrics: Metric<List<ActionMetric>>,
    private val systemMetrics: Metric<List<SystemMetric>>
) : Report {

    override fun analyze(
        results: List<RawCohortResult>,
        outputDirectory: File
    ) {
        val repo = GitRepo.findFromCurrentDirectory()
        val timelineChart = TimelineChart(repo)
        results.forEach { result ->
            timelineChart.generate(
                output = outputDirectory.resolve(result.cohort).toPath(),
                actionMetrics = actionMetrics.measure(result),
                systemMetrics = systemMetrics.measure(result)
            )
        }
    }
}
