package io.github.t45k.part.mining

import io.github.t45k.part.entity.RawMethodHistory
import io.github.t45k.part.entity.RawRevision
import io.github.t45k.part.mining.git.GitCatFileCommand
import io.github.t45k.part.mining.git.GitLogCommand
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

class MethodMiner {
    private val logger: Logger = LoggerFactory.getLogger(MethodMiner::class.java)

    // root/organization/project
    fun miningAllProjects(rootPath: Path): List<RawMethodHistory> {
        val projects: List<Path> = Files.list(rootPath)
                .flatMap { Files.list(it) }
                .toList()

        return Observable.fromIterable(projects)
                .flatMap {
                    Observable.just(it)
                            .observeOn(Schedulers.computation())
                            .map { project -> mining(project) }
                }
                .toList()
                .blockingGet()
                .flatten()
    }

    fun mining(projectPath: Path): List<RawMethodHistory> {
        logger.info("Start mining in $projectPath")
        val rawMethodHistories: List<RawMethodHistory> = Files.walk(projectPath)
                .filter { it.toString().endsWith(".mjava") }
                .map { it to GitLogCommand(projectPath, it).execute(Unit) }
                .map { constructRawMethodHistory(projectPath, it) }
                .toList()
        logger.info("End mining in $projectPath")
        return rawMethodHistories
    }

    private fun constructRawMethodHistory(projectPath: Path, entity: Pair<Path, List<GitLogCommand.LogData>>): RawMethodHistory {
        val gitCheckOutCommand = GitCatFileCommand(projectPath, entity.first)
        val rawRevisions: List<RawRevision> = entity.second
                .map { RawRevision(it.commitHash, it.commitMessage.joinToString(" "), gitCheckOutCommand.execute(it.commitHash)) }
                .toList()
        return RawMethodHistory(entity.first.toString(), rawRevisions)
    }
}
