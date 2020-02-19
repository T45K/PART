package io.github.t45k.part

import org.eclipse.jdt.core.dom.Block
import org.eclipse.jdt.core.dom.SingleVariableDeclaration

data class MethodHistory(val signature: String, val revisions: List<Revision>)

data class Revision(val commitHash: String, val parameters: List<SingleVariableDeclaration>, val body: Block, val commitMessage: String)
