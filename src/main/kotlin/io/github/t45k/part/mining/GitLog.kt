package io.github.t45k.part.mining

import com.github.kusumotolab.sdl4j.util.CommandLine
import java.nio.file.Path

class GitLog(private val projectRoot: Path, private val filePath: Path) {
    private val gitLogCommand: Array<String> = arrayOf("git", "log", "--follow")

    fun execute(): List<LogData> {
        val commandLineResult: CommandLine.CommandLineResult = CommandLine().execute(projectRoot.toFile(), *gitLogCommand, "$filePath")
                ?: return emptyList()

        return parseCommandLineResult(commandLineResult.outputLines)
    }

    private fun parseCommandLineResult(rawLog: List<String>): List<LogData> {
        val logDataList: MutableList<LogData> = mutableListOf()

        var i = 0
        while (i < rawLog.size) {
            val commitLine = rawLog[i]
            val commitHash = commitLine.split(" ")[1]
            i += 3

            val commitMessages = mutableListOf<String>()
            while (i < rawLog.size) {
                val line: String = rawLog[i]
                if (line.matches(Regex("commit [0-9a-z]+"))) {
                    break
                }
                commitMessages.add(line)
                i++
            }

            logDataList.add(LogData(commitHash, commitMessages.joinToString("\n")))
        }

        return logDataList
    }

    data class LogData(val commitHash: String, val commitMessage: String)
}