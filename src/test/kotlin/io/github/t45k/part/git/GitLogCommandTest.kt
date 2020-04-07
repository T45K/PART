package io.github.t45k.part.git

import io.github.t45k.part.testUtil.cloneRepositoryIfNotExists
import org.eclipse.jgit.internal.storage.file.FileRepository
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GitLogCommandTest {
    @Test
    fun testLogCommand() {
        val repository: FileRepository = cloneRepositoryIfNotExists(Paths.get("./src/test/resources/sample/fg-sample"), "https://github.com/T45K/fg-sample.git")
        val logCommand = GitLogCommand(repository)
        val result: List<MiningResult> = logCommand.execute("src/org/DogManager#public_void_barkBark(Dog).mjava")

        assertEquals(5, result.size)
        assertEquals("<OriginalCommitID:d4bce13> Move Method barkBark from Dog to DogManager", result[0].commitMessage)
        assertEquals("<OriginalCommitID:a5a7f85> No refactoring.", result[1].commitMessage)
        assertEquals("<OriginalCommitID:0bb0526> Rename Operation bark(): void to barkBark(): void in class\n" +
                "org.animals.Dog.", result[2].commitMessage)
        assertEquals("<OriginalCommitID:40950c3> Extract Operation\tpublic takeABreath() : void extracted from public\n" +
                "bark() : void in class org.animals.Dog", result[3].commitMessage)
        assertEquals("<OriginalCommitID:cd61fd2> Versão inicial.", result[4].commitMessage)
    }
}