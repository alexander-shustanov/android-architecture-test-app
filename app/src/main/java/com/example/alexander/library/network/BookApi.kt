package com.example.alexander.library.network

import com.example.alexander.library.data.Book
import retrofit2.Call
import retrofit2.http.*

interface BookApi {
    @GET("entities/rs\$Book?sort=createTs")
    fun getBooks(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Call<List<Book>>

    @GET("queries/rs\$Book/booksAfter")
    fun getBooksAfter(
        @Query("id") id: String,
        @Query("limit") limit: Int
    ): Call<List<Book>>

    @PUT("entities/rs\$Book/{id}")
    fun commitBook(@Path("id") id: String, @Body @UpdateBody book: Book): Call<Void>
}

typealias BookListResponse = List<Book>