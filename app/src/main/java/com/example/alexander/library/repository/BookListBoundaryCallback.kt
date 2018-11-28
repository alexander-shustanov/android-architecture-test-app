package com.example.alexander.library.repository

import android.os.Handler
import androidx.paging.PagedList
import com.example.alexander.library.data.Book
import com.example.alexander.library.network.BookApi
import com.example.alexander.library.network.BookListResponse
import com.example.alexander.library.network.PagingRequestHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor


class BookListBoundaryCallback(
    val bookApi: BookApi,
    val ioExecutor: Executor,
    val handleResponse: (BookListResponse) -> Unit,
    requestQueueHandler: Handler
) :
    PagedList.BoundaryCallback<Book>() {

    val helper = PagingRequestHelper(ioExecutor, requestQueueHandler)
    val networkState = helper.createStatusLiveData()

    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) { callback ->
            bookApi.getBooks(0, BookRepository.PAGE_SIZE).enqueue(createCallback(callback))
        }
    }

    override fun onItemAtEndLoaded(itemAtEnd: Book) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) { callback ->
            bookApi.getBooksAfter(itemAtEnd.id, BookRepository.PAGE_SIZE).enqueue(createCallback(callback))
        }
    }

    private fun createCallback(callback: PagingRequestHelper.Callback): Callback<BookListResponse> {
        return object : Callback<BookListResponse> {
            override fun onFailure(call: Call<BookListResponse>, t: Throwable) {
                callback.recordFailure(t)
            }

            override fun onResponse(call: Call<BookListResponse>, response: Response<BookListResponse>) {
                ioExecutor.execute {
                    response.body()?.let(handleResponse)
                    callback.recordSuccess()
                }
            }
        }
    }
}