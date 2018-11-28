package com.example.alexander.library.repository

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.toLiveData
import androidx.room.RoomDatabase
import com.example.alexander.library.IO
import com.example.alexander.library.REQUEST_QUEUE
import com.example.alexander.library.data.Book
import com.example.alexander.library.data.BookDao
import com.example.alexander.library.network.BookApi
import com.example.alexander.library.network.BookListResponse
import com.example.alexander.library.network.NetworkState
import com.google.gson.Gson
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ExecutorService

class BookRepository(override val kodein: Kodein) : KodeinAware {
    private val ioExecutor: ExecutorService by instance(IO)
    private val db: RoomDatabase by instance()
    private val bookApi: BookApi by instance()
    private val bookDao: BookDao by instance()
    private val gson: Gson by instance()
    private val requestQueueHandler: Handler by instance(REQUEST_QUEUE)

    fun getBooks(): Listing<Book> {
        val boundaryCallback = BookListBoundaryCallback(
            requestQueueHandler = requestQueueHandler,
            bookApi = bookApi,
            ioExecutor = ioExecutor,
            handleResponse = { response ->
                Thread.sleep(1000)
                bookDao.insert(*response.toTypedArray())
            }
        )

        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refreshBooks()
        }

        val data = bookDao.getBooks().toLiveData(pageSize = 20, boundaryCallback = boundaryCallback)

        return Listing(
            pagedList = data,
            refresh = { refreshTrigger.value = null },
            refreshState = refreshState,
            retry = { boundaryCallback.helper.retryAllFailed() },
            networkState = boundaryCallback.networkState
        )
    }

    fun getBook(id: String): LiveData<Book> = bookDao.getBook(id)

    private fun refreshBooks(): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        bookApi.getBooks(0, PAGE_SIZE).enqueue(object : Callback<BookListResponse?> {
            override fun onFailure(call: Call<BookListResponse?>, t: Throwable) {
                networkState.value = NetworkState.error(t.message)
            }

            override fun onResponse(call: Call<BookListResponse?>, response: Response<BookListResponse?>) {
                ioExecutor.execute {
                    db.runInTransaction {
                        bookDao.deleteAll()
                        response.body()?.let {
                            bookDao.insert(it)
                        }
                    }
                    networkState.postValue(NetworkState.LOADED)
                }
            }
        })
        return networkState
    }

    fun save(book: Book) {
        ioExecutor.submit {
            bookDao.insert(book)
        }

        bookApi.commitBook(book.id, book).enqueue(object: Callback<Void?> {
            override fun onFailure(call: Call<Void?>, t: Throwable) {

            }

            override fun onResponse(call: Call<Void?>, response: Response<Void?>) {

            }
        })
    }

    companion object {
        const val PAGE_SIZE = 2
    }
}