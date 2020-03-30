package io.github.t45k.part.mining

import io.github.t45k.part.entity.RawMethodHistory
import io.github.t45k.part.entity.RawRevision
import io.github.t45k.part.mining.git.GitCatFileCommand
import io.github.t45k.part.mining.git.GitLogCommand
import io.reactivex.Observable
import io.reactivex.Single
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
    fun miningAllProjects(rootPath: Path, suffix: String = ".mjava"): Observable<RawMethodHistory> =
            listAsObservable(rootPath)
                    .filter { Files.isDirectory(it) }
                    .flatMap { listAsObservable(it) }
                    .filter { Files.isDirectory(it) }
                    .flatMap { mining(it).subscribeOn(Schedulers.computation()) }

    fun mining(projectPath: Path, suffix: String = ".mjava"): Observable<RawMethodHistory> {
        val repository = FileRepository("$projectPath/.git")
        val gitLogCommand = GitLogCommand(repository)
        return Observable.just(projectPath)
                .doOnSubscribe { logger.info("[Start]\tmining\ton $projectPath") }
                .flatMap { Observable.fromIterable(Files.walk(projectPath).toList()) }
                .map { projectPath.relativize(it) }
                .map { it to gitLogCommand.execute(it) }
                .flatMap { constructRawMethodHistory(repository, it.first, it.second).toObservable() }
                .doFinally { logger.info("[End]\tmining\ton $projectPath") }
    }

    private fun constructRawMethodHistory(repository: FileRepository, filePath: Path, entity: List<Pair<ObjectId, String>>): Single<RawMethodHistory> {
        val catFileCommand = GitCatFileCommand(repository)
        return Observable.fromIterable(entity)
                .filter { it.first != ObjectId.zeroId() }
                .map { RawRevision(catFileCommand.execute(it.first), it.second) }
                .toList()
                .map { RawMethodHistory(filePath.toString(), it) }
    }

    private fun listAsObservable(path: Path) = Observable.fromIterable(Files.list(path).toList())
}
