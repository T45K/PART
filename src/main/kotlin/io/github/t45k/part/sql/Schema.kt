package io.github.t45k.part.sql

class FileNameSchema {
    companion object {
        private const val TABLE_NAME = "file_names"
        override fun toString() = "$TABLE_NAME (${Column.FILE_NAME} string primary key)"
        const val INSERTION_QUERY = "insert into $TABLE_NAME values(?)"
        const val ALL_SELECTION_QUERY = "select * from $TABLE_NAME"
    }
}

class RevisionSchema {
    companion object {
        private const val TABLE_NAME = "revisions"
        override fun toString() = "$TABLE_NAME (${Column.FILE_NAME} string, ${Column.CONTENT} string, ${Column.COMMIT_MESSAGE} string)"
        const val INSERTION_QUERY = "insert into $TABLE_NAME (${Column.FILE_NAME}, ${Column.CONTENT}, ${Column.COMMIT_MESSAGE}) values(?, ?, ?)"
        const val SELECTION_QUERY = "select * from $TABLE_NAME where ${Column.FILE_NAME}=?"
    }
}

class TrackingResultSchema {
    companion object {
        private const val TABLE_NAME = "results"
        override fun toString() = "$TABLE_NAME (${Column.FILE_NAME} string, ${Column.DIFF_PATTERN} string)"
        const val INSERTION_QUERY = "insert into $TABLE_NAME (${Column.FILE_NAME}, ${Column.DIFF_PATTERN}) values(?, ?)"
    }
}

class Column {
    companion object {
        const val FILE_NAME = "file_name"
        const val COMMIT_MESSAGE = "commit_message"
        const val CONTENT = "content"
        const val DIFF_PATTERN = "diff_pattern"
    }

}
