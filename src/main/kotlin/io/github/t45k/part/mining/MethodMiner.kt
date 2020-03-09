package io.github.t45k.part.mining

import io.github.t45k.part.entity.RawMethodHistory
import io.github.t45k.part.entity.RawRevision
import io.github.t45k.part.mining.git.GitCatFileCommand
import io.github.t45k.part.mining.git.GitLogCommand
import io.github.t45k.part.mining.git.LogData
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
    fun miningAllProjects(rootPath: Path): Observable<RawMethodHistory> {
        val projects: List<Path> = Files.list(rootPath)
                .filter { Files.isDirectory(it) }
                .flatMap { Files.list(it) }
                .toList()

        return Observable.fromIterable(projects)
                .flatMap {
                    Observable
                            .just(it)
                            .observeOn(Schedulers.io())
                            .flatMap { project -> mining(project) }
                }
    }

    fun mining(projectPath: Path): Observable<RawMethodHistory> {
        logger.info("[Start]\tmining\ton $projectPath")
        val mJavaFiles = Files.walk(projectPath)
                .filter { it.toString().endsWith(".mjava") }.toList()

        val rawMethodHistories: Observable<RawMethodHistory> = Observable.fromIterable(mJavaFiles)
                .map { it to GitLogCommand(projectPath, it).execute(Unit) }
                .map { constructRawMethodHistory(projectPath, it) }
        logger.info("[End]\tmining\ton $projectPath")

        return rawMethodHistories
    }

    private fun constructRawMethodHistory(projectPath: Path, entity: Pair<Path, List<LogData>>): RawMethodHistory {
        val catFileCommand = GitCatFileCommand(projectPath)
        val rawRevisions: List<RawRevision> = entity.second
                .map { RawRevision(it.commitHash, it.commitMessage.joinToString(" "), catFileCommand.execute(it)) }
                .toList()
        return RawMethodHistory(entity.first.toString(), rawRevisions)
    }
}
