package io.github.t45k.part

import io.github.t45k.part.core.preprocess.FinerGitExecutor
import io.github.t45k.part.core.preprocess.MethodMiner
import io.github.t45k.part.core.tracking.ParameterTracker
import io.github.t45k.part.sql.SQL
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import org.kohsuke.args4j.spi.PathOptionHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.system.exitProcess

class App {
    val logger: Logger = LoggerFactory.getLogger(App::class.java)
}

fun main(args: Array<String>) {
    val app = App()
    val config = Configuration()
    val parser = CmdLineParser(config)

    try {
        parser.parseArgument(*args)
    } catch (e: CmdLineException) {
        parser.printUsage(System.out)
        exitProcess(1)
    }

    when (config.mode) {
        Configuration.Mode.FINER_GIT -> {
            val finerGitExecutor = FinerGitExecutor(config)

            config.inputDir?.let {
                val input: Path = config.inputDir!!
                val output = input.parent.resolve("fg")
                finerGitExecutor.execute(input, output, finerGitExecutor::doOnAllProjects)
            } ?: config.project?.let {
                val input: Path = config.project!!
                val output = Path.of("$input-fg")
                finerGitExecutor.execute(input, output, finerGitExecutor::doOnSingleProject)
            } ?: throw InvalidRuntimeArgumentsException("Subject was not specified")
        }

        Configuration.Mode.MINING -> {
            val methodMiner = MethodMiner(config)
            val suffix = ".mjava"

            config.inputDir?.let {
                val input: Path = config.inputDir!!
                methodMiner.execute(input, suffix, methodMiner::doOnAllProjects)
            } ?: config.project?.let {
                val input: Path = config.project!!
                methodMiner.execute(input, suffix, methodMiner::doOnSingleProject)
            } ?: throw InvalidRuntimeArgumentsException("Subject was not specified")
        }

        Configuration.Mode.TRACKING -> {
            app.logger.info("[Start]\ttracking")
            val sql = SQL(config.dbPath)
            val tracker = ParameterTracker()
            Observable.fromIterable(sql.fetchAllFileNames())
                    .flatMap { tracker.track(it, sql).subscribeOn(Schedulers.io()) }
                    .flatMap { Observable.fromIterable(it) }
                    .blockingSubscribe { sql.insertResult(it) }
            sql.close()
            app.logger.info("[End]\ttracking")
        }
    }
}

class Configuration {
    @Option(name = "-i", aliases = ["--input-dir"], usage = "input dir", handler = PathOptionHandler::class)
    var inputDir: Path? = null

    @Option(name = "-p", aliases = ["--projects"], usage = "project dir", handler = PathOptionHandler::class)
    var project: Path? = null

    @Option(name = "-d", aliases = ["--db-path"], usage = "data base path")
    var dbPath: String = "./db.sqlite3"

    @Option(name = "-ifx", aliases = ["--infix"], usage = "infix that is contained by mining target files")
    var infix: String = "src/main/java"

    lateinit var mode: Mode

    @Option(name = "-m", aliases = ["--mode"], usage = "select mode: FINER_GIT, MINING, or TRACKING", required = true)
    fun setMode(s: String) {
        mode = when (s) {
            "FINER_GIT", "F", "f" -> Mode.FINER_GIT
            "MINING", "M", "m" -> Mode.MINING
            "TRACKING", "T", "t" -> Mode.TRACKING
            else -> throw InvalidModeSelectionException("Invalid mode selection")
        }
    }

    enum class Mode {
        FINER_GIT, MINING, TRACKING
    }
}

class InvalidRuntimeArgumentsException(override val message: String) : RuntimeException()
class InvalidModeSelectionException(override val message: String) : RuntimeException()
