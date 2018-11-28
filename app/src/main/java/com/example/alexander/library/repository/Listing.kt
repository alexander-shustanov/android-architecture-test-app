package com.example.alexander.library.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.example.alexander.library.network.NetworkState

data class Listing<T>(
    val pagedList: LiveData<PagedList<T>>,
    val networkState: LiveData<NetworkState>,
    val refreshState: LiveData<NetworkState>,
    val refresh: () -> Unit,
    val retry: () -> Unit
)