package com.example.gruppe5

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    lateinit var mapTest: Button
    lateinit var api_test: Button
    lateinit var navView: BottomNavigationView
    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        assignId()
        setOnClickers()

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
        setupActionBarWithNavController(navController, appBarConfiguration)

        navView.setupWithNavController(navController)
    }

    fun assignId() {
        navView = findViewById(R.id.nav_view)
        api_test = findViewById(R.id.button1)
        mapTest = findViewById(R.id.button2)
        textView = findViewById(R.id.mainText_en)
    }

    // setter onClickers for kart og API_test
    fun setOnClickers(){
        // gaar til mapTest-siden
        mapTest.setOnClickListener {
            val map = Intent(this, MapTest::class.java)
            startActivity(map)
        }

        // gaar til API_test-siden
        api_test.setOnClickListener {
            val api = Intent(this, API_test::class.java)
            startActivity(api)
        }
    }
}
