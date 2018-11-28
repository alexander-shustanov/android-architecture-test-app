package com.example.alexander.library.di

import android.content.Context
import androidx.room.Room
import com.example.alexander.library.data.AppDatabase
import com.example.alexander.library.data.BookDao
import com.example.alexander.library.repository.BookRepository
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

fun mainModule() = Kodein.Module("mainModule") {
    bind<AppDatabase>() with singleton {
        val context = instance<Context>()
        return@singleton Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "books"
        ).build()
    }

    bind<BookRepository>() with singleton {
        BookRepository(instance())
    }

    bind<BookDao>() with provider {
        return@provider instance<AppDatabase>().bookDao()
    }
}