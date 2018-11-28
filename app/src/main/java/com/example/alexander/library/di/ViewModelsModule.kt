package com.example.alexander.library.di

import com.example.alexander.library.ui.info.BookDetailViewModelFactory
import com.example.alexander.library.ui.list.BookListViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

fun viewModelsModule() = Kodein.Module("viewModules") {
    bind<BookListViewModelFactory>() with singleton {
        BookListViewModelFactory(instance())
    }

    bind<BookDetailViewModelFactory>() with singleton {
        BookDetailViewModelFactory(instance())
    }
}