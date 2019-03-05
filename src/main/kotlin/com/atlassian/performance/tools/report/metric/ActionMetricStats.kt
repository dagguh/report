package com.atlassian.performance.tools.report.metric

import com.atlassian.performance.tools.jiraactions.api.ActionMetric
import com.atlassian.performance.tools.jiraactions.api.ActionMetricStatistics
import com.atlassian.performance.tools.report.ActionMetricsReader
import com.atlassian.performance.tools.report.api.OutlierTrimming
import com.atlassian.performance.tools.report.api.result.DurationData
import com.atlassian.performance.tools.report.api.result.InteractionStats
import com.atlassian.performance.tools.report.api.result.RawCohortResult
import org.apache.commons.math3.stat.descriptive.UnivariateStatistic
import org.apache.commons.math3.stat.descriptive.moment.Mean
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation
import java.time.Duration

class ActionMetricStats(
    private val actionMetrics: Metric<List<ActionMetric>>,
    private val actionLabels: Metric<List<String>>,
    private val centralTendencyMetric: UnivariateStatistic,
    private val dispersionMetric: UnivariateStatistic,
    private val outlierTrimming: OutlierTrimming
) : Metric<InteractionStats> {

    constructor(
        actionMetrics: Metric<List<ActionMetric>>,
        actionLabels: Metric<List<String>>
    ) : this(
        actionMetrics = actionMetrics,
        actionLabels = actionLabels,
        centralTendencyMetric = Mean(),
        dispersionMetric = StandardDeviation(),
        outlierTrimming = OutlierTrimming(
            lowerTrim = 0.01,
            upperTrim = 0.99
        )
    )

    override fun measure(
        result: RawCohortResult
    ): InteractionStats {
        if (result.failure != null) {
            return InteractionStats(
                cohort = result.cohort,
                sampleSizes = null,
                centers = null,
                dispersions = null,
                errors = null
            )
        }
        val metrics = actionMetrics.measure(result)
        val statistics = ActionMetricStatistics(metrics)
        val centers = calculate(metrics, centralTendencyMetric)
        val dispersions = calculate(metrics, dispersionMetric)
        val sampleSizes = mutableMapOf<String, Long>()
        val errors = mutableMapOf<String, Int>()
        for (label in actionLabels.measure(result)) {
            sampleSizes[label] = statistics.sampleSize.getOrDefault(label, 0).toLong()
            errors[label] = statistics.errors.getOrDefault(label, 0)
        }
        return InteractionStats(result.cohort, sampleSizes, centers, dispersions, errors)
    }

    private fun calculate(
        metrics: List<ActionMetric>,
        metric: UnivariateStatistic
    ): Map<String, Duration> {
        val labels = metrics.map { it.label }.toSet()
        return labels
            .asSequence()
            .map { label -> calculate(label, metrics, metric) }
            .toMap()
    }

    private fun calculate(
        label: String,
        metrics: List<ActionMetric>,
        metric: UnivariateStatistic
    ): Pair<String, Duration> {
        val durationData = ActionMetricsReader().read(metrics)[label] ?: DurationData.createEmptyMilliseconds()
        val results = measure(durationData, metric)
        return label to results
    }

    private fun measure(
        data: DurationData,
        metric: UnivariateStatistic
    ): Duration {
        val measurement = outlierTrimming.measureWithoutOutliers(
            data = data.stats,
            metric = metric
        )
        return data.durationMapping(measurement)
    }
}
