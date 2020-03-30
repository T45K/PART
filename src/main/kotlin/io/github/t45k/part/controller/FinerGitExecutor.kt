package io.github.t45k.part.controller

import finergit.FinerGitConfig
import finergit.FinerGitMain
import org.kohsuke.args4j.CmdLineParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FinerGitExecutor {
    private val logger: Logger = LoggerFactory.getLogger(FinerGitExecutor::class.java)

    // root/organization/project
    fun executeAllProject(rootPath: Path) {
        Files.list(rootPath)
                .filter { Files.isDirectory(it) }
                .flatMap { Files.list(it) }
                .filter { Files.isDirectory(it) }
                .forEach { execute(it, createFGDir(rootPath, it)) }
    }

    private fun createFGDir(rootPath: Path, projectPath: Path): Path {
        val organizationPath: Path = projectPath.parent.fileName
        return Paths.get(rootPath.parent.toString(), "fg", organizationPath.toString(), projectPath.fileName.toString())
    }

    fun execute(projectPath: Path, outputPath: Path = Path.of("$projectPath-fg")) {
        logger.info("[Start]\tFinerGit execution on $projectPath")

        val config = FinerGitConfig()
        val cmdLineParser = CmdLineParser(config)
        cmdLineParser.parseArgument("create", "-s", projectPath.toString(), "-d", outputPath.toString())

        val finerGitMain = FinerGitMain(config)
        finerGitMain.exec()

        logger.info("[End]\tFinerGit execution on $projectPath")
    }
}
