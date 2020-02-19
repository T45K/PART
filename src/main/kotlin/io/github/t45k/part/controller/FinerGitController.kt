package io.github.t45k.part.controller

import com.github.kusumotolab.sdl4j.util.CommandLine
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FinerGitController {
    private val logger: Logger = LoggerFactory.getLogger(FinerGitController::class.java)

    // root/organization/project
    fun executeAllProject(rootPath: Path) {
        Files.list(rootPath)
                .flatMap { Files.list(it) }
                .parallel()
                .forEach { execute(it, createFGDir(rootPath, it)) }
    }

    private fun createFGDir(rootPath: Path, projectPath: Path): Path {
        val organizationPath: Path = projectPath.parent.fileName
        return Paths.get(rootPath.toString(), "fg", organizationPath.toString(), projectPath.fileName.toString())
    }

    // TODO `Paths.get` may be deprecated in future. Use `Path.of` instead of `Paths.get` in JDK11.
    fun execute(projectPath: Path, outputPath: Path = Paths.get("$projectPath-fg")) {
        logger.info("start FinerGit execution on ${projectPath.fileName}")
        val result: CommandLine.CommandLineResult? = CommandLine().forceExecute("git", "fg", "-s", projectPath.toString(), "-d", outputPath.toString())

        if (result == null) {
            logger.warn("failed force execution in $projectPath")
        } else if (!result.isSuccess) {
            logger.warn(result.outputLines.joinToString("\n"))
        } else {
            logger.info("finish FinerGit execution on ${projectPath.fileName}")
        }
    }
}