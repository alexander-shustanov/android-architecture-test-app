package com.example.alexander.library.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.kodein.di.Kodein

@Suppress("UNCHECKED_CAST")
class BookListViewModelFactory(private val kodein: Kodein) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = BookListViewModel(
        kodein
    ) as T
}