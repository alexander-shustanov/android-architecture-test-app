package com.example.alexander.library.ui.info

import androidx.lifecycle.*
import com.example.alexander.library.data.Book
import com.example.alexander.library.repository.BookRepository
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

class BookDetailsViewModel(override val kodein: Kodein) : ViewModel(), KodeinAware {
    private val bookId: MutableLiveData<String> = MutableLiveData()
    private val bookRepository: BookRepository by instance()

    private val editingBook: MediatorLiveData<Book> = MediatorLiveData()

    private val dbBook: LiveData<Book> = Transformations.switchMap(bookId) {
        bookRepository.getBook(it)
    }

    private val changes: MutableLiveData<Void> = MutableLiveData()

    val book: LiveData<Book> = editingBook

    val showSave: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        val shouldSave: () -> Boolean = {
            val editingBook = editingBook.value
            val dbBook = dbBook.value
            if (dbBook != null && editingBook != null) {
                dbBook.title != editingBook.title || dbBook.author != editingBook.author
            } else false
        }

        addSource(changes) {
            value = shouldSave()
        }
        addSource(dbBook) {
            value = shouldSave()
        }
    }

    init {
        editingBook.addSource(dbBook) {
            if (editingBook.value == null) {
                editingBook.value = Book(it)
            } else {
// TODO
            }
        }
    }

    fun setBookId(bookId: String) {
        this.bookId.value = bookId
    }

    fun save() {
        editingBook.value?.let { bookRepository.save(it) }
    }

    fun notifyChange() {
        changes.value = null
    }
}
