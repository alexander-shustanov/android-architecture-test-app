/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.alexander.library.network

import android.os.Handler
import android.os.HandlerThread
import androidx.annotation.AnyThread
import androidx.annotation.GuardedBy
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

class PagingRequestHelper(private val retryExecutor: Executor, private val requestQueueHandler: Handler) {
    private val lock = Any()
    @GuardedBy("lock")
    private val mRequestQueues =
        arrayOf(RequestQueue(RequestType.INITIAL), RequestQueue(RequestType.BEFORE), RequestQueue(RequestType.AFTER))
    private val listeners = CopyOnWriteArrayList<Listener>()

    private val handler = kotlin.run {
        HandlerThread("requestQueueHandler")
    }

    @AnyThread
    fun addListener(listener: Listener): Boolean {
        return listeners.add(listener)
    }


    fun removeListener(listener: Listener): Boolean {
        return listeners.remove(listener)
    }

    @AnyThread
    fun runIfNotRunning(type: RequestType, request: Request): Boolean {
        val hasListeners = !listeners.isEmpty()
        var report: StatusReport? = null
        synchronized(lock) {
            val queue = mRequestQueues[type.ordinal]
            if (queue.running != null) {
                return false
            }
            queue.running = request
            queue.status = Status.RUNNING
            queue.failed = null
            queue.lastError = null
            if (hasListeners) {
                report = prepareStatusReportLocked()
            }
        }
        if (report != null) {
            dispatchReport(report!!)
        }
        val wrapper = RequestWrapper(request, this, type)
        requestQueueHandler.post(wrapper)
        return true
    }

    @GuardedBy("lock")
    private fun prepareStatusReportLocked(): StatusReport {
        val errors = arrayOf(mRequestQueues[0].lastError, mRequestQueues[1].lastError, mRequestQueues[2].lastError)
        return StatusReport(
            getStatusForLocked(RequestType.INITIAL),
            getStatusForLocked(RequestType.BEFORE),
            getStatusForLocked(RequestType.AFTER),
            errors
        )
    }

    @GuardedBy("lock")
    private fun getStatusForLocked(type: RequestType): Status {
        return mRequestQueues[type.ordinal].status
    }

    @AnyThread
    @VisibleForTesting
    internal fun recordResult(wrapper: RequestWrapper, throwable: Throwable?) {
        var report: StatusReport? = null
        val success = throwable == null
        val hasListeners = !listeners.isEmpty()
        synchronized(lock) {
            val queue = mRequestQueues[wrapper.mType.ordinal]
            queue.running = null
            queue.lastError = throwable
            if (success) {
                queue.failed = null
                queue.status = Status.SUCCESS
            } else {
                queue.failed = wrapper
                queue.status = Status.FAILED
            }
            if (hasListeners) {
                report = prepareStatusReportLocked()
            }
        }
        if (report != null) {
            dispatchReport(report!!)
        }
    }

    private fun dispatchReport(report: StatusReport) {
        for (listener in listeners) {
            listener.onStatusChange(report)
        }
    }

    fun retryAllFailed(): Boolean {
        val toBeRetried = arrayOfNulls<RequestWrapper>(RequestType.values().size)
        var retried = false
        synchronized(lock) {
            for (i in 0 until RequestType.values().size) {
                toBeRetried[i] = mRequestQueues[i].failed
                mRequestQueues[i].failed = null
            }
        }
        for (failed in toBeRetried) {
            if (failed != null) {
                failed.retry(retryExecutor)
                retried = true
            }
        }
        return retried
    }

    internal class RequestWrapper(
        val mRequest: Request, val mHelper: PagingRequestHelper,
        val mType: RequestType
    ) : Runnable {
        override fun run() {
            mRequest(Callback(this, mHelper))
        }

        fun retry(service: Executor) {
            service.execute { mHelper.runIfNotRunning(mType, mRequest) }
        }
    }

    class Callback internal constructor(
        private val mWrapper: RequestWrapper,
        private val mHelper: PagingRequestHelper
    ) {
        private val mCalled = AtomicBoolean()

        fun recordSuccess() {
            if (mCalled.compareAndSet(false, true)) {
                mHelper.recordResult(mWrapper, null)
            } else {
                throw IllegalStateException("already called recordSuccess or recordFailure")
            }
        }

        fun recordFailure(throwable: Throwable) {

            if (throwable == null) {
                throw IllegalArgumentException("You must provide a throwable describing" + " the error to record the failure")
            }
            if (mCalled.compareAndSet(false, true)) {
                mHelper.recordResult(mWrapper, throwable)
            } else {
                throw IllegalStateException(
                    "already called recordSuccess or recordFailure"
                )
            }
        }
    }

    class StatusReport internal constructor(
        val initial: Status,
        val before: Status,
        val after: Status,
        private val mErrors: Array<Throwable?>
    ) {

        fun hasRunning(): Boolean {
            return (initial == Status.RUNNING
                    || before == Status.RUNNING
                    || after == Status.RUNNING)
        }

        fun hasError(): Boolean {
            return (initial == Status.FAILED
                    || before == Status.FAILED
                    || after == Status.FAILED)
        }

        fun getErrorFor(type: RequestType): Throwable? {
            return mErrors[type.ordinal]
        }

        override fun toString(): String {
            return ("StatusReport{"
                    + "initial=" + initial
                    + ", before=" + before
                    + ", after=" + after
                    + ", mErrors=" + Arrays.toString(mErrors)
                    + '}'.toString())
        }

        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false
            val that = o as StatusReport?
            if (initial != that!!.initial) return false
            if (before != that.before) return false
            return if (after != that.after) false else Arrays.equals(mErrors, that.mErrors)
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
        }

        override fun hashCode(): Int {
            var result = initial.hashCode()
            result = 31 * result + before.hashCode()
            result = 31 * result + after.hashCode()
            result = 31 * result + Arrays.hashCode(mErrors)
            return result
        }
    }


    interface Listener {
        fun onStatusChange(report: StatusReport)
    }

    enum class Status {
        RUNNING,
        SUCCESS,
        FAILED
    }


    enum class RequestType {
        INITIAL,
        BEFORE,
        AFTER
    }

    internal inner class RequestQueue(val requestType: RequestType) {
        var failed: RequestWrapper? = null
        var running: Request? = null
        var lastError: Throwable? = null
        var status = Status.SUCCESS
    }


    private fun getErrorMessage(report: PagingRequestHelper.StatusReport): String {
        return PagingRequestHelper.RequestType.values().mapNotNull {
            report.getErrorFor(it)?.message
        }.first()
    }

    fun createStatusLiveData(): LiveData<NetworkState> {
        val liveData = MutableLiveData<NetworkState>()
        addListener(object : Listener {
            override fun onStatusChange(report: StatusReport) {
                when {
                    report.hasRunning() -> liveData.postValue(NetworkState.LOADING)
                    report.hasError() -> liveData.postValue(
                        NetworkState.error(getErrorMessage(report))
                    )
                    else -> liveData.postValue(NetworkState.LOADED)
                }
            }
        })
        return liveData
    }


}

typealias Request = (PagingRequestHelper.Callback) -> Unit
