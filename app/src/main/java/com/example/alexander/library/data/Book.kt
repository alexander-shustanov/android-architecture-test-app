package com.example.alexander.library.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "BOOK")
data class Book(
    @PrimaryKey
    @ColumnInfo(name = "id") var id: String = "",
    var title: String,
    var author: String?
) : com.example.alexander.library.data.Entity {

    override fun copy(): Book = Book(this)

    companion object {
        val PLACEHOLDER_BOOK = Book("", "Book", "Author")

        operator fun invoke(book: Book) = Book(book.id, book.title, book.author)
    }
}