package com.example.alexander.library.di

import com.example.alexander.library.BuildConfig
import com.example.alexander.library.network.BookApi
import com.example.alexander.library.network.ConverterFactory
import com.google.gson.Gson
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun apiModule() = Kodein.Module("apiModule") {

    bind<Gson>() with instance(Gson())

    bind<ConverterFactory>("entityBodyConverter") with singleton {
        ConverterFactory(instance())
    }

    bind<Retrofit>() with singleton {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_ENDPOINT)
//            .addConverterFactory(instance("entityBodyConverter"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    bind<BookApi>() with singleton {
        instance<Retrofit>().create(BookApi::class.java)
    }
}