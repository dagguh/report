package com.atlassian.performance.tools.report

import java.time.Instant

class TimeDatum(
    internal val start: Instant,
    internal val value: Double
)
