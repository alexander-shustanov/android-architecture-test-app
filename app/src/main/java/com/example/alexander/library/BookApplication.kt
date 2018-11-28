package com.example.alexander.library

import android.app.Application
import android.content.Context
import com.example.alexander.library.di.apiModule
import com.example.alexander.library.di.executorsModule
import com.example.alexander.library.di.mainModule
import com.example.alexander.library.di.viewModelsModule
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

class BookApplication : Application(), KodeinAware {
    override val kodein: Kodein = Kodein {
        bind<Context>() with instance(this@BookApplication)

        bind<Kodein>() with singleton {
            instance<BookApplication>().kodein
        }

        import(mainModule())
        import(viewModelsModule())
        import(apiModule())
        import(executorsModule())


        bind<BookApplication>() with instance(this@BookApplication)
    }
}