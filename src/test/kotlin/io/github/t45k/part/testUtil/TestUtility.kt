package io.github.t45k.part.testUtil

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import java.nio.file.Files
import java.nio.file.Path


fun cloneRepositoryIfNotExists(path: Path, cloneUrl: String): FileRepository {
    if (Files.notExists(path)) {
        Git.cloneRepository()
                .setDirectory(path.toFile())
                .setURI(cloneUrl)
                .call()
    }

    return FileRepository(path.resolve(".git").toFile())
}
