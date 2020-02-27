package io.github.t45k.part.tracking

import com.google.common.annotations.VisibleForTesting
import io.github.t45k.part.entity.MethodHistory
import org.eclipse.jdt.core.dom.SingleVariableDeclaration

class ParameterTracker(private val methodHistory: MethodHistory) {
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
