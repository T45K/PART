package io.github.t45k.part.mining.git

import com.github.kusumotolab.sdl4j.util.CommandLine
import java.nio.file.Files
import java.nio.file.Path

class GitCatFileCommand(projectRootPath: Path, filePath: Path) : GitCommand<String, String>(projectRootPath, filePath) {
    private val gitCheckOutCommand: Array<String> = arrayOf("git", "checkout")

    override fun execute(input: String): String {
        val result: CommandLine.CommandLineResult = CommandLine().forceExecute(projectRootPath.toFile(), *gitCheckOutCommand, input, filePath.toString())
                ?: return ""

        return if (result.isSuccess) {
            String(Files.readAllBytes(filePath))
        } else {
            ""
        }
    }
}
