package io.github.t45k.part.git

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


class GitLogCommand(repository: FileRepository) : GitCommand<Path, List<Pair<ObjectId, String>>>(repository) {
    private val git: Git = Git(repository)

    override fun execute(input: Path): List<Pair<ObjectId, String>> {
        val followFilter: FollowFilter = FollowFilter.create(input.toString(), Config().get(DiffConfig.KEY))
        followFilter.renameCallback = DiffCollector()

        val walk = git.log().addPath(input.toString()).call() as RevWalk
        walk.treeFilter = followFilter

        return parseRevWalk(walk.toList(), input.toString())
    }

    private fun parseRevWalk(commits: List<RevCommit>, startPath: String): List<Pair<ObjectId, String>> {
        val diffFormatter = DiffFormatter(DisabledOutputStream.INSTANCE)
        diffFormatter.setRepository(repository)
        diffFormatter.setDiffComparator(RawTextComparator.DEFAULT)
        diffFormatter.isDetectRenames = true

        var followPath: String = startPath
        val fileChanges: MutableList<Pair<ObjectId, String>> = mutableListOf()
        for (i in 0 until commits.size - 1) {
            val diff: DiffEntry = diffFormatter.scan(commits[i + 1], commits[i]).firstOrNull { it.newPath == followPath }
                    ?: continue

            if (diff.isFileChanged()) {
                followPath = diff.oldPath
            }

            fileChanges.addObjectIdIfAppropriate(diff.newId.toObjectId(), commits[i].fullMessage)

            // Add initial file
            if (i == commits.size - 2) {
                fileChanges.addObjectIdIfAppropriate(diff.oldId.toObjectId(), "<init>")
            }
        }
        return fileChanges
    }

    private fun MutableList<Pair<ObjectId, String>>.addObjectIdIfAppropriate(objectId: ObjectId, commitMessage: String) {
        if (this.size == 0 || this.last().first != objectId) {
            this.add(objectId to commitMessage)
        }
    }

    private fun DiffEntry.isFileChanged(): Boolean = this.changeType == DiffEntry.ChangeType.RENAME || this.changeType == DiffEntry.ChangeType.COPY

    private class DiffCollector : RenameCallback() {
        var diffs: MutableList<DiffEntry> = mutableListOf()
        override fun renamed(diff: DiffEntry) {
            diffs.add(diff)
        }
    }
}