package io.github.t45k.part.entity

import org.eclipse.jdt.core.dom.Block
import org.eclipse.jdt.core.dom.SingleVariableDeclaration

data class MethodHistory(val fileName: String, val revisions: List<Revision>)

data class Revision(val commitHash: String, val parameters: List<SingleVariableDeclaration>, val body: Block, val commitMessage: String)

data class RawMethodHistory(val fileName: String, val rawRevisions: List<RawRevision>)

data class RawRevision(val commitHash: String, val commitMessage: String, val rawBody: String)
