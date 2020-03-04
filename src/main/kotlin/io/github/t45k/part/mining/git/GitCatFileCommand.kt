package io.github.t45k.part.mining.git

import com.github.kusumotolab.sdl4j.util.CommandLine
import java.nio.file.Path

class GitCatFileCommand(projectRootPath: Path, filePath: Path) : GitCommand<String, String>(projectRootPath, filePath) {
    private val gitCatFileCommand: Array<String> = arrayOf("git", "cat-file", "-p")

    /**
     * execute "git cat-file -p <hash>:<path>" command
     *
     * @param input target commit hash
     * @return file contents on target commit
     */
    override fun execute(input: String): String {
        val result: CommandLine.CommandLineResult = CommandLine().forceExecute(projectRootPath.toFile(), *gitCatFileCommand, "$input:$filePath")
                ?: return ""

        return if (result.isSuccess) {
            result.outputLines.joinToString(" ")
        } else {
            ""
        }
    }
}
