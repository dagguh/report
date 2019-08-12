package com.atlassian.performance.tools.report.distribution

import com.atlassian.performance.tools.report.api.result.EdibleResult
import com.atlassian.performance.tools.report.api.result.LocalRealResult
import com.atlassian.performance.tools.workspace.api.git.GitRepo
import com.atlassian.performance.tools.workspace.api.git.HardcodedGitRepo
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration

class DistributionComparisonTest {

    /**
     * If you want to update the expected HTML, just copy the contents of the file located by the printed path.
     * Don't use IDEA to paste, because it reformats the content. Use a simple text editor or bash.
     */
    @Test
    fun shouldOutputHtml() {
        val output = Paths.get("build/actual-distribution-comparison.html")
        val repo = HardcodedGitRepo(head = "1234")

        DistributionComparison(repo).compare(
            output = output,
            results = listOf(
                LocalRealResult(Paths.get("JIRA-JPT760-JOB1-8/alpha")).loadEdible(),
                LocalRealResult(Paths.get("JIRA-JPT760-JOB1-8/beta")).loadEdible()
            )
        )

        println("Test distribution comparison available at $output")
        val actualOutput = output.toFile()
        val expectedOutput = File(javaClass.getResource("expected-distribution-comparison.html").toURI())
        assertThat(actualOutput).hasSameContentAs(expectedOutput)
    }

    @Test
    fun shouldShowBrowseBoardsFix() {
        val xlHwr = Paths.get("xl-hwr") // hardware recommendations results for Jira XL
        val brokenBoardsResults = xlHwr.resolve("jsw-7.2.0")
            .let {
                listOf(
                    it.resolve("run-1"),
                    it.resolve("run-2")
                )
            }
            .map { loadHwrRun(it) }
        val fixedBoardsResults = xlHwr.resolve("jsw-7.13.0")
            .let {
                listOf(
                    it.resolve("run-1"),
                    it.resolve("run-2")
                )
            }
            .map { loadHwrRun(it) }

        DistributionComparison(GitRepo.findFromCurrentDirectory()).compare(
            results = brokenBoardsResults.map { avoidBuggedErrorDetectionTimeouts(it) } + fixedBoardsResults,
            output = Paths.get("build/browse-boards-quantile.html")
        )
    }

    private fun loadHwrRun(
        path: Path
    ): EdibleResult {
        val full = LocalRealResult(path).loadEdible()
        val focusedActionMetrics = full
            .actionMetrics
            .filter { it.label == "Browse Boards" }
        return EdibleResult.Builder(full.cohort)
            .actionMetrics(focusedActionMetrics)
            .build()
    }

    private fun avoidBuggedErrorDetectionTimeouts(
        full: EdibleResult
    ): EdibleResult {
        val fairActionMetrics = full
            .actionMetrics
            .filter { it.duration < Duration.ofMinutes(10) }
        return EdibleResult.Builder(full.cohort)
            .actionMetrics(fairActionMetrics)
            .build()
    }
}
