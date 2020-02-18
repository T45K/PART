package io.github.t45k.part.mining

import com.github.kusumotolab.sdl4j.util.CommandLine
import java.nio.file.Files
import java.nio.file.Path

class GitCheckOut(private val projectRootPath: Path, private val filePath: Path) {
    private val gitCheckOutCommand: Array<String> = arrayOf("git", "checkout")

    fun execute(commitHash: String): String {
        val result: CommandLine.CommandLineResult = CommandLine().forceExecute(projectRootPath.toFile(), *gitCheckOutCommand, commitHash, filePath.toString())
                ?: return ""

        return if (result.isSuccess) {
            String(Files.readAllBytes(filePath))
        } else {
            ""
        }
    }
}
