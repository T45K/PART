package io.github.t45k.part.mining

import io.github.t45k.part.mining.git.GitLogCommand
import io.github.t45k.part.mining.git.LogData
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GitLogCommandTest {
    @Test
    fun testParseCommandLineResult() {
        val gitLog = GitLogCommand(Paths.get("."), Paths.get("."))
        val rawLog = """commit 92bf42e8ac418a94793329508a40543f107a4266 (HEAD -> master)
            |Author: T45K <tasktas9@gmail.com>
            |Date:   Mon Mar 2 14:20:41 2020 +0900
            |    add
            |
            |M       B.md
            |
            |commit d80a810adcddf7e332dbd2e3c695bd288c7dec94
            |Author: T45K <tasktas9@gmail.com>
            |Date:   Mon Mar 2 14:19:08 2020 +0900
            |
            |    rename
            |
            |R100    A.md    B.md
            |
            |commit 470d3bc8a70f213e84c40ba21266e0774ddc9ad6
            |Author: T45K <tasktas9@gmail.com>
            |Date:   Mon Mar 2 14:18:50 2020 +0900
            |
            |    init
            |
            |A       A.md""".trimMargin().split("\n")
        val logCommandDataList: List<LogData> = gitLog.parseCommandLineResult(rawLog)

        assertEquals("92bf42e8ac418a94793329508a40543f107a4266", logCommandDataList[0].commitHash)
        assertEquals("    add", logCommandDataList[0].commitMessage[0])
        assertEquals(Paths.get("B.md"), logCommandDataList[0].path)

        assertEquals("d80a810adcddf7e332dbd2e3c695bd288c7dec94", logCommandDataList[1].commitHash)
        assertEquals("    rename", logCommandDataList[1].commitMessage[0])
        assertEquals(Paths.get("B.md"), logCommandDataList[1].path)

        assertEquals("470d3bc8a70f213e84c40ba21266e0774ddc9ad6", logCommandDataList[2].commitHash)
        assertEquals("    init", logCommandDataList[2].commitMessage[0])
        assertEquals(Paths.get("A.md"), logCommandDataList[2].path)
    }
}
