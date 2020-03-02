package io.github.t45k.part.mining

import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GitLogTest {
    @Test
    fun testParseCommandLineResult() {
        val gitLog = GitLog(Paths.get("."), Paths.get("."))
        val logDataList: List<GitLog.LogData> = gitLog.parseCommandLineResult(
                """commit 076b8df71bd2eba7fa74240c1008523a6147255a
            |Author: T45K <tasktas9@gmail.com>
            |Date:   Sat Nov 16 13:39:17 2019 +0900
            |
            |    <OriginalCommitID:e25b492> add run method
            |
            |commit 1c331c9c861384daf0fba5d0c8e90281c80829ed
            |Author: T45K <tasktas9@gmail.com>
            |Date:   Sat Nov 16 10:49:15 2019 +0900
            |
            |    <OriginalCommitID:926c929> extract methods
            |""".trimMargin().split("\n"))

        assertEquals("076b8df71bd2eba7fa74240c1008523a6147255a", logDataList[0].commitHash)
        assertEquals("    <OriginalCommitID:e25b492> add run method", logDataList[0].commitMessage)

        assertEquals("1c331c9c861384daf0fba5d0c8e90281c80829ed", logDataList[1].commitHash)
        assertEquals("    <OriginalCommitID:926c929> extract methods", logDataList[1].commitMessage)
    }
}
