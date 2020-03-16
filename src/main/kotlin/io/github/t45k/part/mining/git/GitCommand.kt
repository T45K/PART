package io.github.t45k.part.mining.git

import org.eclipse.jgit.internal.storage.file.FileRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class GitCommand<Input, Output>(protected val repository: FileRepository) {
    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)
    abstract fun execute(input: Input): Output
}
