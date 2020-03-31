package io.github.t45k.part.core.preprocess

import finergit.FinerGitConfig
import finergit.FinerGitMain
import io.github.t45k.part.Configuration
import org.kohsuke.args4j.CmdLineParser
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FinerGitExecutor(config: Configuration) : Preprocessor<Path, Unit>(config) {

    override fun execute(input: Path, output: Path, executeMethod: (Path, Path) -> Unit) {
        logger.info("[Start]\tFinerGit\texecution")

        executeMethod(input, output)

        logger.info("[End]\tFinerGit\texecution")
    }

    // root/organization/project
    override fun doOnAllProjects(rootPath: Path, outputPath: Path) {
        Files.list(rootPath)
                .filter { Files.isDirectory(it) }
                .flatMap { Files.list(it) }
                .filter { Files.isDirectory(it) }
                .parallel()
                .forEach { doOnSingleProject(it, createFGDir(outputPath, it)) }
    }

    override fun doOnSingleProject(input: Path, outputPath: Path) {
        logger.info("[Start]\tFinerGit execution on $input")

        val config = FinerGitConfig()
        val cmdLineParser = CmdLineParser(config)
        cmdLineParser.parseArgument("-s", input.toString(), "-d", outputPath.toString())

        val finerGitMain = FinerGitMain(config)
        finerGitMain.exec()

        logger.info("[End]\tFinerGit execution on $input")
    }

    private fun createFGDir(outputPath: Path, projectPath: Path): Path {
        val organizationPath: Path = projectPath.parent.fileName
        return Paths.get(outputPath.toString(), organizationPath.toString(), projectPath.fileName.toString())
    }
}
