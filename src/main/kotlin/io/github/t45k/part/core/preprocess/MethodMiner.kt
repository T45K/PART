package io.github.t45k.part.core.preprocess

import io.github.t45k.part.Configuration
import io.github.t45k.part.entity.RawMethodHistory
import io.github.t45k.part.entity.RawRevision
import io.github.t45k.part.mining.git.GitCatFileCommand
import io.github.t45k.part.mining.git.GitLogCommand
import io.github.t45k.part.sql.SQL
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.ObjectId
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

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

    override fun doOnSingleProject(input: Path, suffix: String): Observable<RawMethodHistory> {
        val repository = FileRepository("$input/.git")
        val gitLogCommand = GitLogCommand(repository)
        return Observable.just(input)
                .doOnSubscribe { logger.info("[Start]\tmining\ton $input") }
                .flatMap { Observable.fromIterable(Files.walk(input).toList()) }
                .filter { it.isProductFile(suffix) }
                .map { input.relativize(it) }
                .map { it to gitLogCommand.execute(it) }
                .flatMap { constructRawMethodHistory(repository, it.first, it.second).toObservable() }
                .doFinally { logger.info("[End]\tmining\ton $input") }
    }

    private fun Path.isProductFile(suffix: String, infix: String = "/src/main/java"): Boolean = this.toString().contains(infix) && this.toString().endsWith(suffix)

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
