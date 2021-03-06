package com.atlassian.performance.tools.report.api

import com.atlassian.performance.tools.jiraactions.api.ActionType
import com.atlassian.performance.tools.virtualusers.api.VirtualUserLoad
import java.time.Duration

class PerformanceCriteria(
    val actionCriteria: Map<ActionType<*>, Criteria>,
    val virtualUserLoad: VirtualUserLoad,
    val maxVirtualUsersImbalance: Int = 8,
    val maxInactiveVirtualUsers: Int = 1,
    val nodes: Int = 1
) {
    fun getCenterCriteria() = actionCriteria.mapValues { (_, criteria) -> criteria.centerToleranceRatio }

    fun getDispersionCriteria() = actionCriteria.mapValues { (_, criteria) -> criteria.maxDispersionDifference }

    fun getSampleSizeCriteria() = actionCriteria.mapValues { (_, criteria) -> criteria.sampleSizeCriteria }

    fun getErrorCriteria() = actionCriteria.mapValues { (_, criteria) -> criteria.errorCriteria }
}

class Criteria(
    val centerToleranceRatio: Float,
    val maxDispersionDifference: Duration,
    val sampleSizeCriteria: SampleSizeCriteria,
    val errorCriteria: ErrorCriteria,
    val outlierTrimming: OutlierTrimming
) {
    @JvmOverloads
    constructor(
        toleranceRatio: Float = Float.NaN,
        minimumSampleSize: Long,
        acceptableErrorCount: Int = 0,
        maxDispersionDifference : Duration = Duration.ofMillis(500),
        outlierTrimming : OutlierTrimming = OutlierTrimming(
            lowerTrim = 0.01,
            upperTrim = 0.99
        )
    ) : this (
        centerToleranceRatio = toleranceRatio,
        maxDispersionDifference = maxDispersionDifference,
        sampleSizeCriteria = SampleSizeCriteria(minimumSampleSize),
        errorCriteria = ErrorCriteria(acceptableErrorCount),
        outlierTrimming = outlierTrimming
    )
}

class SampleSizeCriteria(
    val minimumSampleSize: Long
)

class ErrorCriteria(
    val acceptableErrorCount: Int
)