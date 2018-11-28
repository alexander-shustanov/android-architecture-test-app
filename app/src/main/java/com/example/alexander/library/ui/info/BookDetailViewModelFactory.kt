package com.example.alexander.library.ui.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.kodein.di.Kodein

@Suppress("UNCHECKED_CAST")
class BookDetailViewModelFactory(private val kodein: Kodein) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = BookDetailsViewModel(kodein) as T
}