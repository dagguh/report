package com.atlassian.performance.tools.report.color

class PaletteLabelColor(
    private val palette: List<Color>
) : LabelColor {

    private val labelsSeenSoFar = LinkedHashSet<String>()

    override fun color(label: String): Color {
        labelsSeenSoFar.add(label)
        return palette[labelsSeenSoFar.indexOf(label) % palette.size]
    }
}
