package io.github.t45k.part.mining.git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffConfig
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.FollowFilter
import org.eclipse.jgit.revwalk.RenameCallback
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.util.io.DisabledOutputStream
import java.nio.file.Path


class JGitLogCommand(private val repository: FileRepository) {
    private val git: Git = Git(repository)

    fun execute(filePath: Path): List<Pair<ObjectId, String>> {
        val followFilter: FollowFilter = FollowFilter.create(filePath.toString(), Config().get(DiffConfig.KEY))
        followFilter.renameCallback = DiffCollector()

        val walk = git.log().addPath(filePath.toString()).call() as RevWalk
        walk.treeFilter = followFilter

        return parseRevWalk(walk.toList(), filePath.toString())
    }

    private fun parseRevWalk(commits: List<RevCommit>, startPath: String): List<Pair<ObjectId, String>> {
        val diffFormatter = DiffFormatter(DisabledOutputStream.INSTANCE)
        diffFormatter.setRepository(repository)
        diffFormatter.setDiffComparator(RawTextComparator.DEFAULT)
        diffFormatter.isDetectRenames = true

        var parentCommit: RevCommit = commits[0]
        var currentPath: String = startPath
        return commits.drop(1)
                .mapNotNull {
                    val targetDiff: DiffEntry? = diffFormatter.scan(it, parentCommit).firstOrNull { diffEntry -> diffEntry.oldPath == currentPath || diffEntry.newPath == currentPath }
                    parentCommit = it

                    if (targetDiff == null || targetDiff.changeType == DiffEntry.ChangeType.DELETE) {
                        return@mapNotNull null
                    }

                    if (targetDiff.isChanged(currentPath)) {
                        currentPath = targetDiff.oldPath
                    }
                    targetDiff.newId.toObjectId() to it.fullMessage
                }
    }

    private fun DiffEntry.isChanged(path: String): Boolean = this.changeType == DiffEntry.ChangeType.RENAME || this.changeType == DiffEntry.ChangeType.COPY && this.newPath.contains(path)

    private class DiffCollector : RenameCallback() {
        var diffs: MutableList<DiffEntry> = mutableListOf()
        override fun renamed(diff: DiffEntry) {
            diffs.add(diff)
        }
    }
}