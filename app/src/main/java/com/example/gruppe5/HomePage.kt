package com.example.gruppe5

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView


class HomePage : AppCompatActivity() {

    lateinit var navView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        assignId()

        // setter opp navbar
        navView.setupWithNavController(findNavController(R.id.nav_host_fragment))
    }

    fun assignId() {
        navView = findViewById(R.id.nav_view)
    }
}
