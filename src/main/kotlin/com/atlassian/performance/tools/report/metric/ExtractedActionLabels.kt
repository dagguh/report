package com.atlassian.performance.tools.report.metric

import com.atlassian.performance.tools.jiraactions.api.ActionMetric
import com.atlassian.performance.tools.report.api.result.RawCohortResult

class ExtractedActionLabels(
    private val actionMetrics: Metric<List<ActionMetric>>
) : CrossMetric<List<String>> {

    override fun measure(
        results: List<RawCohortResult>
    ): List<String> = results
        .flatMap { extractLabels(it) }
        .toSet()
        .sorted()

    private fun extractLabels(
        result: RawCohortResult
    ): Set<String> = actionMetrics
        .measure(result)
        .map { it.label }
        .toSet()
}
