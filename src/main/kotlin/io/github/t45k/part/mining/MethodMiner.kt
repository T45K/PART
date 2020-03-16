package io.github.t45k.part.mining

import io.github.t45k.part.entity.RawMethodHistory
import io.github.t45k.part.entity.RawRevision
import io.github.t45k.part.mining.git.GitCatFileCommand
import io.github.t45k.part.mining.git.GitLogCommand
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.ObjectId
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

        val repository = FileRepository("$projectPath/.git")
        val rawMethodHistories: Observable<RawMethodHistory> = Observable.fromIterable(mJavaFiles)
                .map { it to GitLogCommand(repository).execute(it) }
                .map { constructRawMethodHistory(repository, it.first, it.second) }
        logger.info("[End]\tmining\ton $projectPath")

        return rawMethodHistories
    }

    private fun constructRawMethodHistory(repository: FileRepository, filePath: Path, entity: List<Pair<ObjectId, String>>): RawMethodHistory {
        val catFileCommand = GitCatFileCommand(repository)
        val rawRevisions: List<RawRevision> = entity
                .map { RawRevision(catFileCommand.execute(it.first), it.second) }
                .toList()
        return RawMethodHistory(filePath.toString(), rawRevisions)
    }
}
