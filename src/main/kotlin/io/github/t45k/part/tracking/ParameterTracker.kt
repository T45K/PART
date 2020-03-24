package io.github.t45k.part.tracking

import com.google.common.annotations.VisibleForTesting
import io.github.t45k.part.entity.TrackingResult
import io.github.t45k.part.parser.MethodASTParser
import io.github.t45k.part.sql.SQL
import io.reactivex.Observable
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.SingleVariableDeclaration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ParameterTracker {
    private val logger: Logger = LoggerFactory.getLogger(ParameterTracker::class.java)

    @Suppress("UNCHECKED_CAST")
    fun track(fileName: String, sql: SQL): Observable<TrackingResult> {
        if (!fileName.contains("src/main/java")) {
            return Observable.empty()
        }

        val methodASTs: Iterator<MethodDeclaration> = sql.fetchMethodHistory(fileName).rawRevisions
                .map { MethodASTParser(it.rawBody).parse() }
                .iterator()

        // TODO ここに入ることは本来ないはず．どっかで原因調査
        // 可能性: メソッドのパースに失敗してる
        if (!methodASTs.hasNext()) {
            logger.warn("Histories of $fileName was not found")
            return Observable.empty()
        }

        val trackingResults: MutableList<TrackingResult> = mutableListOf()
        var parent: MethodDeclaration = methodASTs.next()
        while (methodASTs.hasNext()) {
            val child: MethodDeclaration = methodASTs.next()
            val parentParams = convertSimpleParams(parent.parameters() as List<SingleVariableDeclaration>)
            val childParams = convertSimpleParams(child.parameters() as List<SingleVariableDeclaration>)
            parent = child
            if (parentParams != childParams) {
                trackingResults.add(TrackingResult(fileName, detectParametersDifferencing(parentParams, childParams)))
            }
        }
        return Observable.fromIterable(trackingResults)
    }

    @VisibleForTesting
    fun detectParametersDifferencing(params1: List<Pair<String, String>>, params2: List<Pair<String, String>>): DiffPattern {
        if (params1.toSet() == params2.toSet()) {
            return DiffPattern.REORDERING
        }

        if (params1.size != params2.size) {
            return DiffPattern.SIZE_CHANGED
        }

        for (i in params1.indices) {
            if (params1[i].first != params2[i].first) {
                return DiffPattern.TYPE_CHANGED
            }

            if (params1[i].second != params2[i].second) {
                return DiffPattern.RENAMING_VARIABLE
            }
        }

        return DiffPattern.OTHER
    }

    enum class DiffPattern {
        REORDERING, SIZE_CHANGED, RENAMING_VARIABLE, TYPE_CHANGED, OTHER
    }

    private fun convertSimpleParams(params: List<SingleVariableDeclaration>): List<Pair<String, String>> = params.map { it.type.toString() to it.name.toString() }
}
