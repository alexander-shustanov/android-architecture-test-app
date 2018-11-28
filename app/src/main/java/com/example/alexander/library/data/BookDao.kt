package com.example.alexander.library.data

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BookDao {
    @Query("SELECT * FROM BOOK")
    fun getBooks(): DataSource.Factory<Int, Book>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg books: Book)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(books: List<Book>)

    @Query("SELECT COUNT(*) FROM BOOK")
    fun getBookCount(): Int

    @Query("DELETE FROM BOOK")
    fun deleteAll()

    @Query("SELECT * FROM BOOK where id = :id")
    fun getBook(id: String): LiveData<Book>
}