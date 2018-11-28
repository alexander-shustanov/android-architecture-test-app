package com.example.alexander.library.ui.list

import androidx.lifecycle.ViewModel
import com.example.alexander.library.repository.BookRepository
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

class BookListViewModel(override val kodein: Kodein) : ViewModel(), KodeinAware {

    private val bookRepository: BookRepository by instance()

    private val repoResult = bookRepository.getBooks()

    init {
        refresh()
    }

    val books = repoResult.pagedList
    val networkState = repoResult.networkState
    val refreshState = repoResult.refreshState

    fun refresh() {
        repoResult.refresh()
    }

    fun retry() {
        repoResult.retry()
    }
}
