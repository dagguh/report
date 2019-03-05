package com.atlassian.performance.tools.report.vmstat

import com.atlassian.performance.tools.io.api.directories
import com.atlassian.performance.tools.report.TimeSeries
import com.atlassian.performance.tools.report.api.result.RawCohortResult

class MultiNodeVmstatBottleneck {

    fun plotBottlenecksPerNode(
        result: RawCohortResult
    ): Map<String, TimeSeries<VmstatBottleneck.Bottleneck>> {
        return result
            .results
            .toFile()
            .directories()
            .map { it.name to it.resolve("jpt-vmstat.log") }
            .toMap()
            .filterValues { it.exists() }
            .mapValues { (node, vmstatLog) ->
                vmstatLog.bufferedReader().use {
                    VmstatBottleneck().plot(it, node)
                }
            }
    }
}
