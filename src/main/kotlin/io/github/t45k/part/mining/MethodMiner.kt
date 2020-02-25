package io.github.t45k.part.mining

import io.github.t45k.part.entity.RawMethodHistory
import io.github.t45k.part.entity.RawRevision
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

class MethodMiner {
    // root/organization/project
    fun miningAllProjects(rootPath: Path): List<RawMethodHistory> {
        val projects: List<Path> = Files.list(rootPath)
                .flatMap { Files.list(it) }
                .toList()

        return Observable.fromIterable(projects)
                .subscribeOn(Schedulers.computation())
                .flatMap { Observable.fromIterable(mining(it)) }
                .toList()
                .blockingGet()
    }

    fun mining(projectPath: Path): List<RawMethodHistory> {
        return Files.walk(projectPath)
                .filter { it.toString().endsWith(".mjava") }
                .map { it to GitLog(projectPath, it).execute() }
                .map { constructRawMethodHistory(projectPath, it) }
                .toList()
    }

    private fun constructRawMethodHistory(projectPath: Path, entity: Pair<Path, List<GitLog.LogData>>): RawMethodHistory {
        val gitCheckOutCommand = GitCheckOut(projectPath, entity.first)
        val rawRevisions: List<RawRevision> = entity.second
                .map { RawRevision(it.commitHash, it.commitMessage, gitCheckOutCommand.execute(it.commitHash)) }
                .toList()
        return RawMethodHistory(entity.first.toString(), rawRevisions)
    }
}