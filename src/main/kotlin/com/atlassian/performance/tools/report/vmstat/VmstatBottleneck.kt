package com.atlassian.performance.tools.report.vmstat

import java.io.BufferedReader
import kotlin.streams.asSequence

class VmstatBottleneck {

    fun find(
        vmstat: BufferedReader
    ): Map<Bottleneck, Int> {
        return VmstatLog()
            .cleanUp(vmstat.lines())
            .asSequence()
            .map { readCpuUtilization(it) }
            .map { findBottleneck(it) }
            .groupingBy { it }
            .eachCount()
    }

    private fun readCpuUtilization(
        line: String
    ): CpuUtilization {
        val values = line.split(" ")
        return CpuUtilization(
            user = values[12].toInt(),
            system = values[13].toInt(),
            idle = values[14].toInt(),
            waiting = values[15].toInt(),
            stolen = values[16].toInt()
        )
    }

    private fun findBottleneck(
        cpu: CpuUtilization
    ): Bottleneck = when {
        cpu.system > cpu.user / 10 -> Bottleneck.SYSTEM
        cpu.idle > 0.1 -> Bottleneck.IDLE
        else -> Bottleneck.APPLICATION
    }

    /**
     * [Vmstat columns](https://access.redhat.com/solutions/1160343):
     * These are percentages of total CPU time.
     * * `us`: Time spent running non-kernel code. (user time, including nice time)
     * * `sy`: Time spent running kernel code. (system time)
     * * `id`: Time spent idle. Prior to Linux 2.5.41, this includes IO-wait time.
     * * `wa`: Time spent waiting for IO. Prior to Linux 2.5.41, included in idle.
     * * `st`: Time stolen from a virtual machine. Prior to Linux 2.6.11, unknown.
     */
    private data class CpuUtilization(
        val user: Int,
        val system: Int,
        val idle: Int,
        val waiting: Int,
        val stolen: Int
    )

    enum class Bottleneck {
        SYSTEM,
        IDLE,
        APPLICATION
    }
}
