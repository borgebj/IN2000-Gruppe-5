package com.example.gruppe5

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.gruppe5.testFiler.MainTestActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    lateinit var tester: Button
    lateinit var navView: BottomNavigationView
    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        assignId()
        setOnClickers()

        // setter opp navbar
        navView.setupWithNavController(findNavController(R.id.nav_host_fragment))
    }

    fun assignId() {
        navView = findViewById(R.id.nav_view)
        textView = findViewById(R.id.mainText_en)
        tester = findViewById(R.id.testFiler_knapp)
    }

    // setter onClickers for kart og API_test
    fun setOnClickers(){

        // gaar til test-filene
        tester.setOnClickListener {
            val intent = Intent(this, MainTestActivity::class.java)
            startActivity(intent)
        }
    }
}
