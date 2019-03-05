package com.atlassian.performance.tools.report.report

import com.atlassian.performance.tools.infrastructure.api.metric.SystemMetric
import com.atlassian.performance.tools.jiraactions.api.ActionMetric
import com.atlassian.performance.tools.report.api.result.InteractionStats
import com.atlassian.performance.tools.report.api.result.RawCohortResult
import com.atlassian.performance.tools.report.metric.*
import java.io.File

// TODO how to replace just the timeline?
class DefaultReport(
    private val crossCohortTable: Report,
    private val crossCohortChart: Report,
    private val cohortTimeline: Report
) : Report {

    constructor(
        stats: Metric<InteractionStats>,
        actionLabels: CrossMetric<List<String>>,
        actionMetrics: Metric<List<ActionMetric>>,
        systemMetrics: Metric<List<SystemMetric>>
    ) : this(
        crossCohortTable = CrossCohortTable(stats, actionLabels),
        crossCohortChart = CrossCohortChart(stats, actionLabels),
        cohortTimeline = CohortTimeline(actionMetrics, systemMetrics)
    )

    constructor(
        actionMetrics: Metric<List<ActionMetric>>,
        actionLabels: CrossMetric<List<String>>
    ) : this(
        stats = ActionMetricStats(actionMetrics, actionLabels),
        actionLabels = actionLabels,
        actionMetrics = actionMetrics,
        systemMetrics = MulticastSystemMetrics()
    )

    constructor(
        actionMetrics: Metric<List<ActionMetric>>
    ) : this(
        actionMetrics = actionMetrics,
        actionLabels = ExtractedActionLabels(actionMetrics)
    )

    constructor() : this(
        actionMetrics = MulticastActionMetrics()
    )

    override fun analyze(
        results: List<RawCohortResult>,
        outputDirectory: File
    ) {
        listOf(
            crossCohortTable,
            crossCohortChart,
            cohortTimeline
        ).forEach {
            it.analyze(results, outputDirectory)
        }
    }
}
