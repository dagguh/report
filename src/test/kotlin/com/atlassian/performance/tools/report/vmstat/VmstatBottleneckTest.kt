package com.atlassian.performance.tools.report.vmstat

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.BufferedReader
import kotlin.streams.asSequence

class VmstatBottleneckTest {

    @Test
    fun shouldFindIdleBottleneck() {
        val vmstat = this::class.java
            .getResourceAsStream("./c5.18xlarge-3nodes-run1-jiranode1-vmstat.log")
            .bufferedReader()

        val bottleneckCounts = vmstat.use { findBottlenecks(it) }

        assertThat(bottleneckCounts).isEqualTo(mapOf(
            Bottleneck.SYSTEM to 8,
            Bottleneck.IDLE to 911
        ))
    }


    @Test
    fun shouldFindIdleBottleneckOnSecondNode() {
        val vmstat = this::class.java
            .getResourceAsStream("./c5.18xlarge-3nodes-run1-jiranode2-vmstat.log")
            .bufferedReader()

        val bottleneckCounts = vmstat.use { findBottlenecks(it) }

        assertThat(bottleneckCounts).isEqualTo(mapOf(
            Bottleneck.SYSTEM to 11,
            Bottleneck.IDLE to 793
        ))
    }

    private fun findBottlenecks(
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

    private fun findBottleneck(
        cpu: CpuUtilization
    ): Bottleneck = when {
        cpu.system > cpu.user / 10 -> Bottleneck.SYSTEM
        cpu.idle < 0.1 -> Bottleneck.APPLICATION
        else -> Bottleneck.IDLE
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

    private enum class Bottleneck {
        SYSTEM,
        IDLE,
        APPLICATION
    }
}
