package com.atlassian.performance.tools.report.color

class Color(
    val red: Int,
    val green: Int,
    val blue: Int
) {
    fun toCss(): String = "rgb($red, $green, $blue)"
}
