package io.github.t45k.part.mining.git

import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.ObjectLoader
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.stream.Collectors

class GitCatFileCommand(repository: FileRepository) : GitCommand<ObjectId, String>(repository) {
    override fun execute(input: ObjectId): String {
        val loader: ObjectLoader = repository.open(input)
        val inputStream: InputStream = loader.openStream()
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)
        return bufferedReader.lines().collect(Collectors.joining(" "))
    }
}
