package io.github.t45k.part.mining

import com.github.kusumotolab.sdl4j.util.CommandLine
import com.google.common.annotations.VisibleForTesting
import java.nio.file.Path
import java.nio.file.Paths

class GitLogCommand(private val projectRootPath: Path, private val filePath: Path) {
    private val gitLogCommand: Array<String> = arrayOf("git", "log", "--follow")

    fun execute(): List<LogData> {
        val commandLineResult: CommandLine.CommandLineResult = CommandLine().forceExecute(projectRootPath.toFile(), *gitLogCommand, "$filePath")
                ?: return emptyList()

        return parseCommandLineResult(commandLineResult.outputLines)
    }

    @VisibleForTesting
    fun parseCommandLineResult(rawLog: List<String>): List<LogData> {
        return prettyPrintLog(rawLog)
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
        val prettyPrintedLogs: MutableList<List<String>> = rawLog
                .asSequence()
                .mapIndexed { index, s -> index to s }
                .filterIndexed { _, pair -> pair.second.isCommitHash() }
                .drop(1)
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
        return if (elements[0].matches(Regex("([RC])[0-9]+.*"))) {
            Paths.get(elements[2])
        } else {
            Paths.get(elements[1])
        }
    }

    data class LogData(val commitHash: String, val commitMessage: List<String>, val path: Path)
}
