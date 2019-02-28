package com.atlassian.performance.tools.report.vmstat

import java.util.stream.Stream

internal class VmstatLog {

    fun cleanUp(
        lines: Stream<String>
    ): Stream<String> {
        return lines
            .map { it.trim() }
            .filter { !it.startsWith("procs") }
            .filter { !it.startsWith("r") }
            .map { it.replace(Regex(" +"), " ") }
    }
}
