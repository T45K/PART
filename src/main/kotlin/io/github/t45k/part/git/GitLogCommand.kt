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

class GitLogCommand(repository: FileRepository) : GitCommand<String, List<MiningResult>>(repository) {
    private val git: Git = Git(repository)

    override fun execute(fileName: String): List<MiningResult> {
        val followFilter: FollowFilter = FollowFilter.create(fileName, Config().get(DiffConfig.KEY))
        followFilter.renameCallback = DiffCollector()

        val walk = git.log().addPath(fileName).call() as RevWalk
        walk.treeFilter = followFilter

        val commits: MutableList<RevCommit> = mutableListOf()
        for (revCommit in walk.iterator()) {
            commits.add(revCommit)
        }
        return if (commits.size >= 2) {
            val firstObjectId: ObjectId? = getObjectIdFromFile(fileName, commits[0], commits[1]) ?: return emptyList()
            parseRevWalk(commits, firstObjectId!!)
        } else {
            emptyList()
        }
    }

    private fun parseRevWalk(commits: List<RevCommit>, firstObjectId: ObjectId): List<MiningResult> {
        val diffFormatter = setUpDiffFormatter()
        val appearedObjectIds = mutableSetOf(firstObjectId)
        val fileChanges = mutableListOf(firstObjectId to commits[0].fullMessage)
        for (i in 1 until commits.size) {
            val newerCommit: RevCommit = commits[i - 1]
            val olderCommit: RevCommit = commits[i]
            val diff: DiffEntry = diffFormatter.scan(olderCommit, newerCommit).firstOrNull { appearedObjectIds.contains(it.newId.toObjectId()) }
                    ?: continue

            val candidate: ObjectId? = diff.oldId.toObjectId()
            if (diff.isNoChange() || candidate.isEmpty()) {
                continue
            }

            fileChanges.add(candidate!! to olderCommit.fullMessage)
            appearedObjectIds.add(candidate)
        }
        return fileChanges
    }

    private fun setUpDiffFormatter(): DiffFormatter {
        val diffFormatter = DiffFormatter(DisabledOutputStream.INSTANCE)
        diffFormatter.setRepository(repository)
        diffFormatter.setDiffComparator(RawTextComparator.DEFAULT)
        diffFormatter.isDetectRenames = true

        return diffFormatter
    }

    private fun getObjectIdFromFile(fileName: String, newerCommit: RevCommit, olderCommit: RevCommit): ObjectId? =
            setUpDiffFormatter()
                    .scan(olderCommit, newerCommit)
                    .firstOrNull { it.newPath == fileName }
                    ?.newId
                    ?.toObjectId()

    private fun DiffEntry.isNoChange(): Boolean = this.newId.toObjectId() == this.oldId.toObjectId()

    private fun ObjectId?.isEmpty(): Boolean = this == null || this == ObjectId.zeroId()

    private class DiffCollector : RenameCallback() {
        var diffs: MutableList<DiffEntry> = mutableListOf()
        override fun renamed(diff: DiffEntry) {
            diffs.add(diff)
        }
    }
}

data class MiningResult(val objectId: ObjectId, val commitMessage: String)

infix fun ObjectId.to(commitMessage: String): MiningResult = MiningResult(this, commitMessage)
