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
    fun miningAllProjects(rootPath: Path, suffix: String = ".mjava"): Observable<RawMethodHistory> {
        val projects: List<Path> = Files.list(rootPath)
                .filter { Files.isDirectory(it) }
                .flatMap { Files.list(it) }
                .toList()

        return Observable.fromIterable(projects)
                .flatMap {
                    Observable
                            .just(it)
                            .observeOn(Schedulers.io())
                            .flatMap { project -> mining(project, suffix) }
                }
    }

    fun mining(projectPath: Path, suffix: String = ".mjava"): Observable<RawMethodHistory> {
        logger.info("[Start]\tmining\ton $projectPath")
        val repository = FileRepository("$projectPath/.git")
        val rawMethodHistories = Files.walk(projectPath)
                .filter { it.toString().endsWith(suffix) }
                .map { projectPath.relativize(it) }
                .map { it to GitLogCommand(repository).execute(it) }
                .map { constructRawMethodHistory(repository, it.first, it.second) }
                .toList()

        logger.info("[End]\tmining\ton $projectPath")
        return Observable.fromIterable(rawMethodHistories)
    }

    private fun constructRawMethodHistory(repository: FileRepository, filePath: Path, entity: List<Pair<ObjectId, String>>): RawMethodHistory {
        val catFileCommand = GitCatFileCommand(repository)
        val rawRevisions: List<RawRevision> = entity
                .filter { it.first != ObjectId.zeroId() }
                .map { RawRevision(catFileCommand.execute(it.first), it.second) }
                .toList()
        return RawMethodHistory(filePath.toString(), rawRevisions)
    }
}
