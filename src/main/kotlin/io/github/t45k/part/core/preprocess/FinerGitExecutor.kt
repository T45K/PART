package io.github.t45k.part.core.preprocess

import finergit.FinerGitConfig
import finergit.FinerGitMain
import io.github.t45k.part.Configuration
import io.github.t45k.part.util.listAsObservable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.kohsuke.args4j.CmdLineParser
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FinerGitExecutor(config: Configuration) : Preprocessor<Path, Observable<Unit>>(config) {

    override fun execute(input: Path, output: Path, executeMethod: (Path, Path) -> Observable<Unit>) {
        logger.info("[Start]\tFinerGit\texecution")

        executeMethod(input, output)
                .blockingSubscribe()

        logger.info("[End]\tFinerGit\texecution")
    }

    // root/organization/project
    override fun doOnAllProjects(rootPath: Path, outputPath: Path): Observable<Unit> =
            listAsObservable(rootPath)
                    .filter { Files.isDirectory(it) }
                    .flatMap { listAsObservable(it) }
                    .filter { Files.isDirectory(it) }
                    .flatMap { doOnSingleProject(it, createFGDir(outputPath, it)).subscribeOn(Schedulers.io()) }

    override fun doOnSingleProject(project: Path, outputPath: Path): Observable<Unit> =
            Observable
                    .fromCallable {
                        val config = FinerGitConfig()
                        val cmdLineParser = CmdLineParser(config)
                        cmdLineParser.parseArgument("-s", project.toString(), "-d", outputPath.toString())
                        FinerGitMain(config).exec()
                    }
                    .doOnSubscribe { logger.info("[Start]\tFinerGit execution on $project") }
                    .doFinally { logger.info("[End]\tFinerGit execution on $project") }


    private fun createFGDir(outputPath: Path, projectPath: Path): Path {
        val organizationPath: Path = projectPath.parent.fileName
        return Paths.get(outputPath.toString(), organizationPath.toString(), projectPath.fileName.toString())
    }
}
