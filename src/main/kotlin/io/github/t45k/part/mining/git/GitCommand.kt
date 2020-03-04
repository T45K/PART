package io.github.t45k.part.mining.git

import java.nio.file.Path

abstract class GitCommand<Input, Output>(protected val projectRootPath: Path, protected val filePath: Path) {
    abstract fun execute(input: Input): Output
}
