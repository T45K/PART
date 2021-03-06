package io.github.t45k.part.core.preprocess

import io.github.t45k.part.Configuration
import io.github.t45k.part.entity.RawMethodHistory
import io.github.t45k.part.entity.RawRevision
import io.github.t45k.part.git.GitCatFileCommand
import io.github.t45k.part.git.GitLogCommand
import io.github.t45k.part.git.MiningResult
import io.github.t45k.part.sql.SQL
import io.github.t45k.part.util.listAsObservable
import io.github.t45k.part.util.walkAsObservable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.eclipse.jgit.internal.storage.file.FileRepository
import java.nio.file.Files
import java.nio.file.Path

class MethodMiner(config: Configuration) : Preprocessor<String, Observable<RawMethodHistory>>(config) {

    override fun execute(input: Path, suffix: String, executeMethod: (Path, String) -> Observable<RawMethodHistory>) {
        logger.info("[Start]\tmining")

        val sql = SQL(config.dbPath)
        executeMethod(input, suffix).blockingSubscribe { sql.insertMethodHistory(it) }
        sql.close()

        logger.info("[End]\tmining")
    }

    // root/organization/project
    override fun doOnAllProjects(rootPath: Path, suffix: String): Observable<RawMethodHistory> =
            listAsObservable(rootPath)
                    .filter { Files.isDirectory(it) }
                    .flatMap { listAsObservable(it) }
                    .filter { Files.isDirectory(it) }
                    .flatMap { doOnSingleProject(it, suffix).subscribeOn(Schedulers.computation()) }

    override fun doOnSingleProject(project: Path, suffix: String): Observable<RawMethodHistory> {
        val repository = FileRepository("$project/.git")
        val gitLogCommand = GitLogCommand(repository)
        return Observable.just(project)
                .doOnSubscribe { logger.info("[Start]\tmining\ton $project") }
                .flatMap { walkAsObservable(it) }
                .filter { it.isProductFile(suffix) }
                .map { project.relativize(it) }
                .map { it to gitLogCommand.execute(it.toString()) }
                .filter { it.second.size > 1 }
                .flatMap { constructRawMethodHistory(repository, it.first, it.second).toObservable() }
                .doFinally { logger.info("[End]\tmining\ton $project") }
    }

    private fun Path.isProductFile(suffix: String): Boolean = this.toString().contains(config.infix) && this.toString().endsWith(suffix)

    private fun constructRawMethodHistory(repository: FileRepository, filePath: Path, entity: List<MiningResult>): Single<RawMethodHistory> {
        val catFileCommand = GitCatFileCommand(repository)
        return Observable.fromIterable(entity)
                .map { RawRevision(catFileCommand.execute(it.objectId), it.commitMessage) }
                .toList()
                .map { RawMethodHistory(filePath.toString(), it) }
    }
}
