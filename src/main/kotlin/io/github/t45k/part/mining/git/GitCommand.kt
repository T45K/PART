package io.github.t45k.part.mining.git

import java.nio.file.Path

abstract class GitCommand<I, O>(protected val projectRootPath: Path, protected val filePath: Path) {
    abstract fun execute(input: I): O
}
