package io.github.t45k.part.core.preprocess

import io.github.t45k.part.Configuration
import io.github.t45k.part.entity.RawMethodHistory
import io.github.t45k.part.testUtil.cloneRepositoryIfNotExists
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals

internal class MethodMinerTest {

    @Test
    fun testMining() {
        val config = Configuration()
        config.infix = ""
        val miner = MethodMiner(config)

        val path = Paths.get("./src/test/resources/sample/fg-sample")
        cloneRepositoryIfNotExists(path, "https://github.com/T45K/fg-sample.git")
        val list: MutableList<RawMethodHistory> = miner.doOnSingleProject(path, ".mjava")
                .toList()
                .blockingGet()

        assertEquals(11, list.size)
    }
}
