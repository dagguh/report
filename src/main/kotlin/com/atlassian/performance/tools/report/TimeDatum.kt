package com.atlassian.performance.tools.report

import java.time.Instant

class TimeDatum<V>(
    internal val start: Instant,
    internal val value: V
) {
    operator fun component1(): Instant = start
    operator fun component2(): V = value
}
