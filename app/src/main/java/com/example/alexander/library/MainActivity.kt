package com.example.alexander.library

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.Navigation

class MainActivity : FragmentActivity(), NavHost {
    override fun getNavController(): NavController = Navigation.findNavController(this, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
