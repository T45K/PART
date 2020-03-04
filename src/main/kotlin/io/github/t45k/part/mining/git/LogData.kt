package io.github.t45k.part.mining.git

import java.nio.file.Path

data class LogData(val commitHash: String, val commitMessage: List<String>, val path: Path)
