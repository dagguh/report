package com.atlassian.performance.tools.report.vmstat

import com.atlassian.performance.tools.report.vmstat.VmstatBottleneck.Bottleneck.IDLE
import com.atlassian.performance.tools.report.vmstat.VmstatBottleneck.Bottleneck.SYSTEM
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.BufferedReader

class VmstatBottleneckTest {

    private val vmstatBottleneck = VmstatBottleneck()

    @Test
    fun shouldFindIdleBottleneckInJswHardwareExplorationNodeOne() {
        val vmstat = readResource("./jsw-7.13.0-hwr-c5.18xlarge-node1.log")

        val bottleneckCounts = vmstat.use { vmstatBottleneck.find(it) }

        assertThat(bottleneckCounts).isEqualTo(mapOf(
            SYSTEM to 8,
            IDLE to 911
        ))
    }

    @Test
    fun shouldFindIdleBottleneckInJswHardwareExplorationNodeTwo() {
        val vmstat = readResource("./jsw-7.13.0-hwr-c5.18xlarge-node2.log")

        val bottleneckCounts = vmstat.use { vmstatBottleneck.find(it) }

        assertThat(bottleneckCounts).isEqualTo(mapOf(
            SYSTEM to 11,
            IDLE to 793
        ))
    }

    @Test
    fun shouldFindMixedBottlenecksInJswRegressionTest() {
        val vmstat = readResource("./jsw-8.0.1-regression.log")

        val bottleneckCounts = vmstat.use { vmstatBottleneck.find(it) }

        assertThat(bottleneckCounts).isEqualTo(mapOf(
            SYSTEM to 444,
            IDLE to 388
        ))
    }

    @Test
    fun shouldFindMostlyIdleBottlenecksInJswDataScalingReport() {
        val vmstat = readResource("./jsw-7.8.0-dsr.log")

        val bottleneckCounts = vmstat.use { vmstatBottleneck.find(it) }

        assertThat(bottleneckCounts).isEqualTo(mapOf(
            SYSTEM to 222,
            IDLE to 650
        ))
    }

    private fun readResource(
        resourceName: String
    ): BufferedReader = this::class.java
        .getResourceAsStream(resourceName)
        .bufferedReader()
}
