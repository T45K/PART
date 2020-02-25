package io.github.t45k.part.sql

import io.github.t45k.part.entity.MethodHistory
import io.github.t45k.part.entity.RawMethodHistory
import io.github.t45k.part.entity.Revision
import io.github.t45k.part.parser.MethodASTParser
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.SingleVariableDeclaration
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement

class SQL {
    private val connection: Connection = DriverManager.getConnection("jdbc:sqlite:./db.sqlite3")
            ?: throw RuntimeException("Bad db connection")
    private val fileNameInsertionStatement: PreparedStatement = connection.prepareStatement(FileNameSchema.INSERTION_QUERY)
    private val allFileNamesSelectionStatement: PreparedStatement = connection.prepareStatement(FileNameSchema.ALL_SELECTION_QUERY)

    private val revisionInsertionStatement: PreparedStatement = connection.prepareStatement(RevisionSchema.INSERTION_QUERY)
    private val revisionSelectionStatement: PreparedStatement = connection.prepareStatement(RevisionSchema.SELECTION_QUERY)

    private val contentsInsertionStatement: PreparedStatement = connection.prepareStatement(ContentsSchema.INSERTION_QUERY)
    private val contentsSelectionStatement: PreparedStatement = connection.prepareStatement(ContentsSchema.SELECTION_QUERY)

    init {
        val statement: Statement = connection.createStatement()
        statement.executeUpdate("create table if not exists $FileNameSchema")
        statement.executeUpdate("create table if not exists $RevisionSchema")
        statement.executeUpdate("create table if not exists $ContentsSchema")
        statement.close()
    }

    fun insert(methodHistory: RawMethodHistory) {
        val fileName: String = methodHistory.fileName
        fileNameInsertionStatement.setString(1, fileName)
        fileNameInsertionStatement.executeUpdate()

        for (revision in methodHistory.rawRevisions) {
            val (commitHash, commitMessage, contents) = revision
            revisionInsertionStatement.setString(1, fileName)
            revisionInsertionStatement.setString(2, commitHash)
            revisionInsertionStatement.setString(3, commitMessage)
            revisionInsertionStatement.executeUpdate()

            val generatedKey: ResultSet = revisionInsertionStatement.generatedKeys
            generatedKey.next()
            val id: Int = generatedKey.getInt(1)
            generatedKey.close()

            contentsInsertionStatement.setString(1, contents)
            contentsInsertionStatement.setInt(2, id)
            contentsInsertionStatement.executeUpdate()
        }
    }

    fun fetchAllFileNames(): List<String> {
        val results: ResultSet = allFileNamesSelectionStatement.executeQuery()
        val methods: MutableList<String> = mutableListOf()
        while (results.next()) {
            methods.add(results.getString(Column.FILE_NAME))
        }

        return methods
    }

    fun fetchMethodHistory(fileName: String): MethodHistory {
        revisionSelectionStatement.setString(1, fileName)
        val revisionSelectionResults: ResultSet = revisionSelectionStatement.executeQuery()
        val revisionAndIds = mutableListOf<Triple<String, String, Int>>()
        while (revisionSelectionResults.next()) {
            val commit: Pair<String, String> = revisionSelectionResults.getString(Column.COMMIT_HASH) to revisionSelectionResults.getString(Column.COMMIT_MESSAGE)
            val id: Int = revisionSelectionResults.getInt(Column.ID)
            revisionAndIds.add(Triple(commit.first, commit.second, id))
        }
        revisionSelectionResults.close()

        val revisions = mutableListOf<Revision>()
        for (revisionAndId in revisionAndIds) {
            val (commitHash, commitMessage, id) = revisionAndId
            contentsSelectionStatement.setInt(1, id)
            val contentSelectionResult: ResultSet = contentsSelectionStatement.executeQuery()
            contentSelectionResult.next()
            val content: String = contentSelectionResult.getString(Column.CONTENT)
            contentSelectionResult.close()

            val methodDeclaration: MethodDeclaration = MethodASTParser(content).parse()

            @Suppress("UNCHECKED_CAST")
            revisions.add(Revision(commitHash, methodDeclaration.parameters() as List<SingleVariableDeclaration>, methodDeclaration.body, commitMessage))
        }

        return MethodHistory(fileName, revisions)
    }

    fun close() {
        fileNameInsertionStatement.close()
        revisionInsertionStatement.close()
        contentsInsertionStatement.close()
        fileNameInsertionStatement.clearParameters()
        connection.close()
    }
}