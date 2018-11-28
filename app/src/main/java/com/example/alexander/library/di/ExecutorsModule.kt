package com.example.alexander.library.di

import android.os.Handler
import android.os.HandlerThread
import com.example.alexander.library.IO
import com.example.alexander.library.NETWORK
import com.example.alexander.library.REQUEST_QUEUE
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

fun executorsModule() = Kodein.Module("executors") {
    bind<ExecutorService>(IO) with singleton {
        Executors.newSingleThreadExecutor()
    }

    bind<ExecutorService>(NETWORK) with singleton {
        Executors.newSingleThreadExecutor()
    }

    bind<Handler>(REQUEST_QUEUE) with singleton {
        val handlerThread = HandlerThread(REQUEST_QUEUE).apply {
            start()
        }
        Handler(handlerThread.looper)
    }

}