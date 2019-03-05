package com.atlassian.performance.tools.report

class TimeSeries<D>(
    val name: String,
    val dimension: Dimension,
    val data: List<TimeDatum<D>>,
    val reduction: (Iterable<D>) -> D
)
