package com.atlassian.performance.tools.report.metric

import com.atlassian.performance.tools.infrastructure.api.metric.SystemMetric
import com.atlassian.performance.tools.report.api.parser.SystemMetricsParser
import com.atlassian.performance.tools.report.api.result.RawCohortResult

class MulticastSystemMetrics : Metric<List<SystemMetric>> {

    override fun measure(
        result: RawCohortResult
    ): List<SystemMetric> {
        return SystemMetricsParser().parse(result.results)
    }
}
