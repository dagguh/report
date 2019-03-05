package com.atlassian.performance.tools.report.metric

import com.atlassian.performance.tools.report.api.result.RawCohortResult

interface Metric<T> {

    fun measure(
        result: RawCohortResult
    ): T
}
