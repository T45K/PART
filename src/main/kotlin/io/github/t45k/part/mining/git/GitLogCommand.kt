package io.github.t45k.part.mining.git

import com.github.kusumotolab.sdl4j.util.CommandLine
import com.google.common.annotations.VisibleForTesting
import java.nio.file.Path
import java.nio.file.Paths

class GitLogCommand(projectRootPath: Path, private val filePath: Path) : GitCommand<Unit, List<LogData>>(projectRootPath) {
    private val gitLogCommand: Array<String> = arrayOf("git", "log", "--follow", "--name-status")

    override fun execute(input: Unit): List<LogData> {
        val commandLineResult: CommandLine.CommandLineResult = CommandLine().forceExecute(projectRootPath.toFile(), *gitLogCommand, filePath.toString())
                ?: return emptyList()

        return parseCommandLineResult(commandLineResult.outputLines)
    }

    @VisibleForTesting
    fun parseCommandLineResult(rawLog: List<String>): List<LogData> {
        return prettyPrintLog(rawLog)
                .filter { it[it.size - 1][0].isChange() }
                .map { parseLog(it) }
    }

    private fun parseLog(prettyPrintedLog: List<String>): LogData {
        val commitHash: String = prettyPrintedLog[0].split(" ")[1]
        val commitMessage: List<String> = prettyPrintedLog.subList(3, prettyPrintedLog.size - 1)
        val path: Path = prettyPrintedLog[prettyPrintedLog.size - 1].getPathFromNameStatus()
        return LogData(commitHash, commitMessage, path)
    }

    private fun prettyPrintLog(rawLog: List<String>): List<List<String>> {
        var startIndex = 0
        val prettyPrintedLogs: MutableList<List<String>> = rawLog.asSequence()
                .mapIndexed { index, s -> index to s }
                .filter { it.second.isCommitHash() }
                .drop(1) // ignore first index(0)
                .map { pair ->
                    val oneCommit: List<String> = rawLog.subList(startIndex, pair.first).filter { it.isNotBlank() }
                    startIndex = pair.first
                    oneCommit
                }.toMutableList()
        prettyPrintedLogs.add(rawLog.subList(startIndex, rawLog.size).filter { it.isNotBlank() })
        return prettyPrintedLogs
    }

    private fun String.isCommitHash(): Boolean = this.contains(Regex("commit [0-9a-z]+"))

    private fun String.getPathFromNameStatus(): Path {
        val elements: List<String> = this.split(Regex("\\s")).filter { it.isNotEmpty() }
        return when {
            elements[0].matches(Regex("[RC][0-9]+.*")) -> {
                Paths.get(elements[2])
            }
            elements[0].matches(Regex("[AM]")) -> {
                Paths.get(elements[1])
            }
            else -> {
                throw RuntimeException("Unexpected modification $this of git")
            }
        }
    }

    private fun Char.isChange() = this == 'R' || this == 'C' || this == 'A' || this == 'M'

}
