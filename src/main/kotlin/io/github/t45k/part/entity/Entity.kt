package io.github.t45k.part.entity

import io.github.t45k.part.core.tracking.ParameterTracker
import org.eclipse.jdt.core.dom.Block
import org.eclipse.jdt.core.dom.SingleVariableDeclaration

data class MethodHistory(val fileName: String, val revisions: List<Revision>)

data class Revision(val parameters: List<SingleVariableDeclaration>, val body: Block, val commitMessage: String)

data class RawMethodHistory(val fileName: String, val rawRevisions: List<RawRevision>)

data class RawRevision(val rawBody: String, val commitMessage: String)

data class TrackingResult(val fileName: String, val diffPattern: ParameterTracker.DiffPattern)
