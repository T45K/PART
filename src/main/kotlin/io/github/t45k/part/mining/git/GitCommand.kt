package io.github.t45k.part.mining.git

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path

abstract class GitCommand<Input, Output>(protected val projectRootPath: Path) {
    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)
    abstract fun execute(input: Input): Output
}
