package com.atlassian.performance.tools.report.metric

import com.atlassian.performance.tools.report.api.result.RawCohortResult

interface CrossMetric<T> : Metric<T> {

    fun measure(
        results: List<RawCohortResult>
    ): T

    override fun  measure(
        result: RawCohortResult
    ): T = measure(listOf(result))
}
