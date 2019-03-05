package com.atlassian.performance.tools.report.vmstat

import com.atlassian.performance.tools.report.Dimension
import com.atlassian.performance.tools.report.TimeDatum
import com.atlassian.performance.tools.report.TimeSeries
import java.io.BufferedReader
import java.time.*
import kotlin.streams.asSequence

class VmstatBottleneck {

    fun plot(
        vmstat: BufferedReader,
        computer: String
    ): TimeSeries<Bottleneck> = TimeSeries(
        name = computer,
        dimension = Dimension("vmstat bottleneck", null),
        data = VmstatLog()
            .cleanUp(vmstat.lines())
            .asSequence()
            .map { parse(it) }
            .map { TimeDatum(it.time, findBottleneck(it.cpuUtilization)) }
            .toList(),
        reduction = { bottlenecks -> throw Exception("Cannot reduce multiple bottlenecks into one: $bottlenecks") }
    )

    private fun parse(
        line: String
    ): VmstatMetric {
        val values = line.split(" ")
        return VmstatMetric(
            cpuUtilization = CpuUtilization(
                user = values[12].toInt(),
                system = values[13].toInt(),
                idle = values[14].toInt(),
                waiting = values[15].toInt(),
                stolen = values[16].toInt()
            ),
            time = ZonedDateTime.of(
                LocalDate.parse(values[17]),
                LocalTime.parse(values[18]),
                ZoneId.of("UTC")
            ).toInstant()
        )
    }

    private fun findBottleneck(
        cpu: CpuUtilization
    ): Bottleneck = when {
        cpu.system > cpu.user / 10 -> Bottleneck.SYSTEM
        cpu.idle > 0.1 -> Bottleneck.IDLE
        else -> Bottleneck.APPLICATION
    }

    private data class VmstatMetric(
        val cpuUtilization: CpuUtilization,
        val time: Instant
    )

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
        /**
         * If you're bottlenecked by IDLE, then you're not utilizing your hardware to the fullest.
         * If your performance is good, it's fine, because it means you have spare capacity.
         * If your performance is poor, you can improve your service or change your usage patterns.
         * Adding more hardware will not meaningfully improve performance.
         * In order to improve your service, you need to take a look at thread dumps and analyze HTTP thread pool
         * status breakdown. If all are RUNNABLE, increase your HTTP pool. If a portion is TIMED_WAITING, then you have
         * spare HTTP capacity. If there's a high WAITING or BLOCKED percentage, then they're locked on some external
         * resource. See where HTTP threads are stuck and who's holding the locks.
         */
        IDLE,
        APPLICATION
    }
}
