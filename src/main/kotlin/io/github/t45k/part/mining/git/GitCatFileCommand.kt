package io.github.t45k.part.mining.git

import com.github.kusumotolab.sdl4j.util.CommandLine
import java.nio.file.Path

class GitCatFileCommand(projectRootPath: Path) : GitCommand<LogData, String>(projectRootPath) {
    private val gitCatFileCommand: Array<String> = arrayOf("git", "cat-file", "-p")

    /**
     * execute "git cat-file -p <hash>:<path>" command
     *
     * @param input LogData(commit hash + file path)
     * @return file contents on target commit
     */
    override fun execute(input: LogData): String {
        val (commitHash: String, _, filePath: Path) = input
        val result: CommandLine.CommandLineResult = CommandLine().forceExecute(projectRootPath.toFile(), *gitCatFileCommand, "$commitHash:$filePath")
                ?: return ""

        return if (result.isSuccess) {
            result.outputLines.joinToString(" ")
        } else {
            logger.warn(result.outputLines.toString())
            ""
        }
    }
}
