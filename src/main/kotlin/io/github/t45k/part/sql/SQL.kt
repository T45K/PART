package io.github.t45k.part.sql

import io.github.t45k.part.entity.RawMethodHistory
import io.github.t45k.part.entity.RawRevision
import io.github.t45k.part.entity.TrackingResult
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement

class SQL(dbPath: String) {
    private val connection: Connection = DriverManager.getConnection("jdbc:sqlite:$dbPath")
            ?: throw RuntimeException("Bad db connection")
    private val fileNameInsertionStatement: PreparedStatement
    private val allFileNamesSelectionStatement: PreparedStatement

    private val revisionInsertionStatement: PreparedStatement
    private val revisionSelectionStatement: PreparedStatement

    private val resultInsertionStatement: PreparedStatement

    init {
        val statement: Statement = connection.createStatement()
        statement.executeUpdate("create table if not exists $FileNameSchema")
        statement.executeUpdate("create table if not exists $RevisionSchema")
        statement.executeUpdate("create table if not exists $TrackingResultSchema")
        statement.close()

        fileNameInsertionStatement = connection.prepareStatement(FileNameSchema.INSERTION_QUERY)
        allFileNamesSelectionStatement = connection.prepareStatement(FileNameSchema.ALL_SELECTION_QUERY)

        revisionInsertionStatement = connection.prepareStatement(RevisionSchema.INSERTION_QUERY)
        revisionSelectionStatement = connection.prepareStatement(RevisionSchema.SELECTION_QUERY)

        resultInsertionStatement = connection.prepareStatement(TrackingResultSchema.INSERTION_QUERY)
    }

    fun insertMethodHistory(methodHistory: RawMethodHistory) {
        val fileName: String = methodHistory.fileName
        fileNameInsertionStatement.setString(1, fileName)
        fileNameInsertionStatement.executeUpdate()

        for (revision in methodHistory.rawRevisions) {
            val (rawBody, commitMessage) = revision
            revisionInsertionStatement.setString(1, fileName)
            revisionInsertionStatement.setString(2, rawBody)
            revisionInsertionStatement.setString(3, commitMessage)
            revisionInsertionStatement.executeUpdate()
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

    @Synchronized
    fun fetchMethodHistory(fileName: String): RawMethodHistory {
        revisionSelectionStatement.setString(1, fileName)
        val revisionSelectionResults: ResultSet = revisionSelectionStatement.executeQuery()
        val rawRevisions = mutableListOf<RawRevision>()
        while (revisionSelectionResults.next()) {
            rawRevisions.add(RawRevision(revisionSelectionResults.getString(Column.CONTENT), revisionSelectionResults.getString(Column.COMMIT_MESSAGE)))
        }
        revisionSelectionResults.close()

        return RawMethodHistory(fileName, rawRevisions)
    }

    fun insertResult(result: TrackingResult) {
        resultInsertionStatement.setString(1, result.fileName)
        resultInsertionStatement.setString(2, result.diffPattern.toString())
        resultInsertionStatement.executeUpdate()
    }

    fun close() {
        fileNameInsertionStatement.close()
        revisionInsertionStatement.close()
        allFileNamesSelectionStatement.close()
        revisionSelectionStatement.close()
        resultInsertionStatement.close()

        connection.close()
    }
}