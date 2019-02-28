package com.atlassian.performance.tools.report.vmstat

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.BufferedReader

class VmstatBottleneckTest {

    @Test
    fun shouldFindIdleBottleneck() {
        val vmstat = this::class.java
            .getResourceAsStream("./c5.18xlarge-3nodes-run1-jiranode1-vmstat.log")
            .bufferedReader()

        val bottleneck = vmstat.use { findBottlenecks(it) }

        assertThat(bottleneck).isEqualTo(Bottleneck.IDLE)
    }

    private fun findBottlenecks(
        vmstat: BufferedReader
    ): Bottleneck {
        val cpu = CpuUtilization(
            user = 2,
            system = 0,
            idle = 97,
            waiting = 1,
            stolen = 0
        )
        return when {
            cpu.system > cpu.user / 10 -> Bottleneck.SYSTEM
            cpu.idle < 0.1 -> Bottleneck.APPLICATION
            else -> Bottleneck.IDLE
        }
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
    private class CpuUtilization(
        val system: Int,
        val user: Int,
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
