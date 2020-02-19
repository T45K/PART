package io.github.t45k.part

import io.github.t45k.part.controller.FinerGitController
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import org.kohsuke.args4j.spi.PathOptionHandler
import java.nio.file.Path
import kotlin.system.exitProcess

fun main(args: Array<String>) {
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
            val controller = FinerGitController()
            controller.executeAllProject(config.inputDir)
        }
        Configuration.Mode.MINING -> {
        }
        Configuration.Mode.TRACKING -> {
        }
    }
}

class Configuration {
    @Option(name = "-i", aliases = ["--input-dir"], usage = "input dir", required = true, handler = PathOptionHandler::class)
    lateinit var inputDir: Path
    lateinit var mode: Mode

    @Option(name = "-m", aliases = ["--mode"], usage = "select mode: FINER_GIT, MINING, or TRACKING", required = true)
    fun setMode(s: String) {
        mode = when (s) {
            "FINER_GIT", "F" -> Mode.FINER_GIT
            "MINING", "M" -> Mode.MINING
            "TRACKING", "T" -> Mode.TRACKING
            else -> throw RuntimeException("Invalid mode selection")
        }
    }

    enum class Mode {
        FINER_GIT, MINING, TRACKING
    }
}


