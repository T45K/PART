package io.github.t45k.part.mining

import com.github.kusumotolab.sdl4j.util.CommandLine
import com.google.common.annotations.VisibleForTesting
import java.nio.file.Path

class GitLog(private val projectRootPath: Path, private val filePath: Path) {
    private val gitLogCommand: Array<String> = arrayOf("git", "log", "--follow")

    fun execute(): List<LogData> {
        val commandLineResult: CommandLine.CommandLineResult = CommandLine().forceExecute(projectRootPath.toFile(), *gitLogCommand, "$filePath")
                ?: return emptyList()

        return parseCommandLineResult(commandLineResult.outputLines)
    }

    @VisibleForTesting
    fun parseCommandLineResult(rawLog: List<String>): List<LogData> {
        val logDataList: MutableList<LogData> = mutableListOf()

        var i = 0
        while (i < rawLog.size) {
            val commitLine = rawLog[i]
            val commitHash = commitLine.split(" ")[1]
            i += 3

            val commitMessages = mutableListOf<String>()
            while (i < rawLog.size) {
                val line: String = rawLog[i]
                if (line.isEmpty()) {
                    i++
                    continue
                }
                if (line.isCommitHash()) {
                    break
                }
                commitMessages.add(line)
                i++
            }

            logDataList.add(LogData(commitHash, commitMessages.joinToString("\n")))
        }

        return logDataList
    }

    private fun String.isCommitHash(): Boolean = this.matches(Regex("commit [0-9a-z]+"))

    data class LogData(val commitHash: String, val commitMessage: List<String>, val path: Path)
}
