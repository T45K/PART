package io.github.t45k.part.core.preprocess

import io.github.t45k.part.Configuration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path

abstract class Preprocessor<Auxiliary, Output>(protected val config: Configuration) {
    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)

    abstract fun execute(input: Path, auxiliary: Auxiliary, executeMethod: (Path, Auxiliary) -> Output)
    abstract fun doOnAllProjects(rootPath: Path, auxiliary: Auxiliary): Output
    abstract fun doOnSingleProject(input: Path, auxiliary: Auxiliary): Output
}
