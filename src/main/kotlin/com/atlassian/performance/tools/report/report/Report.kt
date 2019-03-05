package com.atlassian.performance.tools.report.report

import com.atlassian.performance.tools.report.api.result.RawCohortResult
import java.io.File

interface Report {

    fun analyze(
        results: List<RawCohortResult>,
        outputDirectory: File
    )
}
