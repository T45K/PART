package io.github.t45k.part.sql

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement

class SQL {
    private val connection: Connection = DriverManager.getConnection("jdbc:sqlite:./db.sqlite3")
            ?: throw RuntimeException("Bad db connection")
    private val paramInsertionStatement: PreparedStatement = connection.prepareStatement(paramInsertionQuery)
    private val methodInsertionStatement: PreparedStatement = connection.prepareStatement(methodInsertionQuery)

    companion object {
        const val paramInsertionQuery: String = "insert into param values(?, ?, ?)"
        const val methodInsertionQuery: String = "insert into method(name, isConstructor, numOfParams) values(?, ?, ?)"
    }

    init {
        val statement: Statement = connection.createStatement()
        statement.executeUpdate("create table if not exists method (name string, isConstructor integer, numOfParams integer, id integer primary key autoincrement)")
        statement.executeUpdate("create table if not exists param (id integer, declaredOrder integer, referredOrder integer)")
    }

    fun insert(method: Method) {
        val id: Int = insertMethod(method)
        if (method.numOfParams <= 1) {
            return
        }

        for ((declaredOrder, referredOrder) in method.order.withIndex()) {
            insertParam(id, declaredOrder, referredOrder)
        }
    }

    private fun insertParam(id: Int, declaredOrder: Int, referredOrder: Int) {
        val paramInsertionStatement: PreparedStatement = connection.prepareStatement(paramInsertionQuery)
        paramInsertionStatement.setInt(1, id)
        paramInsertionStatement.setInt(2, declaredOrder)
        paramInsertionStatement.setInt(3, referredOrder)
        paramInsertionStatement.executeUpdate()
    }

    private fun insertMethod(method: Method): Int {
        val name = "${method.path}#${method.name}"
        val isConstructor: Int = if (method.isConstructor) 1 else 0
        methodInsertionStatement.setString(1, name)
        methodInsertionStatement.setInt(2, isConstructor)
        methodInsertionStatement.setInt(3, method.numOfParams)
        methodInsertionStatement.executeUpdate()

        val generatedKey: ResultSet = methodInsertionStatement.generatedKeys
        generatedKey.next()
        val id: Int = generatedKey.getInt(1)
        generatedKey.close()

        return id
    }

    fun close() {
        paramInsertionStatement.close()
        methodInsertionStatement.close()
        connection.close()
    }
}