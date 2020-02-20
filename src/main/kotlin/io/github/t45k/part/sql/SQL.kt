package io.github.t45k.part.sql

import io.github.t45k.part.entity.RawMethodHistory
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement

class SQL {
    private val connection: Connection = DriverManager.getConnection("jdbc:sqlite:./db.sqlite3")
            ?: throw RuntimeException("Bad db connection")
    private val methodInsertionStatement: PreparedStatement = connection.prepareStatement(methodInsertionQuery)
    private val revisionInsertionStatement: PreparedStatement = connection.prepareStatement(revisionInsertionQuery)
    private val contentsInsertionStatement: PreparedStatement = connection.prepareStatement(contentsInsertionQuery)

    companion object {
        private const val methodInsertionQuery: String = "insert into method values(?)"
        private const val revisionInsertionQuery: String = "insert into revision(fileName, hash, message) values(?, ?, ?)"
        private const val contentsInsertionQuery: String = "insert into contents values(?, ?)"
    }

    init {
        val statement: Statement = connection.createStatement()
        statement.executeUpdate("create table if not exists method (fileName string primary key)")
        statement.executeUpdate("create table if not exists revision (fileName string, hash string, message string, id integer primary key autoincrement)")
        statement.executeUpdate("create table if not exists contents (body string, id integer)")
        statement.close()
    }

    fun insert(methodHistory: RawMethodHistory) {
        val fileName: String = methodHistory.fileName
        methodInsertionStatement.setString(1, fileName)
        methodInsertionStatement.executeUpdate()

        for (revision in methodHistory.rawRevisions) {
            val (commitHash, commitMessage, rawBody) = revision
            revisionInsertionStatement.setString(1, fileName)
            revisionInsertionStatement.setString(2, commitHash)
            revisionInsertionStatement.setString(3, commitMessage)
            revisionInsertionStatement.executeUpdate()

            val generatedKey: ResultSet = revisionInsertionStatement.generatedKeys
            generatedKey.next()
            val id: Int = generatedKey.getInt(1)
            generatedKey.close()

            contentsInsertionStatement.setString(1, rawBody)
            contentsInsertionStatement.setInt(2, id)
            contentsInsertionStatement.executeUpdate()
        }
    }

    fun close() {
        methodInsertionStatement.close()
        revisionInsertionStatement.close()
        contentsInsertionStatement.close()
        methodInsertionStatement.clearParameters()
        connection.close()
    }
}