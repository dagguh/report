package com.atlassian.performance.tools.report

class TimeSeries(
    val name: String,
    val dimension: Dimension,
    val data: List<TimeDatum>,
    val reduction: (Iterable<Double>) -> Double
)
