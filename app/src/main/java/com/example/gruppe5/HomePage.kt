package com.example.gruppe5

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.gruppe5.testFiler.MainTestActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


class HomePage : AppCompatActivity() {

    lateinit var navView: BottomNavigationView
    lateinit var testFilKnapp: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        assignId()
        setOnClickers()

        // setter opp navbar
        navView.setupWithNavController(findNavController(R.id.nav_host_fragment))
    }

    fun assignId() {
        navView = findViewById(R.id.nav_view)
        testFilKnapp = findViewById(R.id.testFiler_knapp)
    }
    // setter onClickers for kart og API_test
    fun setOnClickers(){

        // gaar til map-tester
        testFilKnapp.setOnClickListener {
            val map = Intent(this, MainTestActivity::class.java)
            startActivity(map)
        }
    }
}
