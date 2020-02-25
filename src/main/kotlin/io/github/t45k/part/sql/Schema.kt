package io.github.t45k.part.sql

class FileNameSchema {
    companion object {
        override fun toString() = "$TABLE_NAME (${Column.FILE_NAME} string primary key)"
        private const val TABLE_NAME = "file_names"
        const val INSERTION_QUERY = "insert into $TABLE_NAME values(?)"
        const val ALL_SELECTION_QUERY = "select * from $TABLE_NAME"
    }
}

class RevisionSchema {
    companion object {
        override fun toString() = "$TABLE_NAME (${Column.FILE_NAME} string, ${Column.COMMIT_HASH} string, ${Column.COMMIT_MESSAGE} string, ${Column.ID} integer primary key autoincrement)"
        private const val TABLE_NAME = "revisions"
        const val INSERTION_QUERY = "insert into $TABLE_NAME (${Column.FILE_NAME}, ${Column.COMMIT_HASH}, ${Column.COMMIT_MESSAGE}) values(?, ?, ?)"
        const val SELECTION_QUERY = "select * from $TABLE_NAME where ${Column.FILE_NAME} = ?"
    }
}

class ContentsSchema {
    companion object {
        override fun toString(): String = "$TABLE_NAME (${Column.CONTENT} string, ${Column.ID} integer primary key)"
        private const val TABLE_NAME = "contents"
        const val INSERTION_QUERY = "insert into $TABLE_NAME values(?, ?)"
        const val SELECTION_QUERY = "select * from $TABLE_NAME where ${Column.ID}=?"
    }
}

class Column {
    companion object {
        const val FILE_NAME = "file_name"
        const val COMMIT_HASH = "commit_hash"
        const val COMMIT_MESSAGE = "commit_message"
        const val ID = "id"
        const val CONTENT = "content"
    }
}
