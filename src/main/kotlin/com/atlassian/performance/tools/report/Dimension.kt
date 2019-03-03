package com.atlassian.performance.tools.report

data class Dimension(
    val name: String,
    val unit: String?
) {
    val label: String = if (unit != null) "$name [$unit]" else name

    constructor(
        dimension: com.atlassian.performance.tools.infrastructure.api.metric.Dimension
    ) : this(
        name = dimension.description,
        unit = null
    )
}
