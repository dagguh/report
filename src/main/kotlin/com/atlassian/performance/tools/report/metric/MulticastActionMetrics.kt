package com.atlassian.performance.tools.report.metric

import com.atlassian.performance.tools.io.api.directories
import com.atlassian.performance.tools.jiraactions.api.ActionMetric
import com.atlassian.performance.tools.jiraactions.api.parser.ActionMetricsParser
import com.atlassian.performance.tools.report.api.result.RawCohortResult
import java.io.InputStream

class MulticastActionMetrics : Metric<List<ActionMetric>> {

    private val parser = ActionMetricsParser()

    override fun measure(
        result: RawCohortResult
    ): List<ActionMetric> = result
        .results
        .resolve("virtual-users")
        .toFile()
        .directories()
        .map { it.resolve("test-results") }
        .flatMap { it.directories() }
        .asSequence()
        .map { it.resolve("action-metrics.jpt") }
        .filter { it.exists() }
        .map { it.inputStream() }
        .map { parse(it) }
        .flatten()
        .toList()

    private fun parse(
        stream: InputStream
    ): List<ActionMetric> = stream.use { parser.parse(it) }
}
