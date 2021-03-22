package com.example.gruppe5.testFiler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toolbar
import androidx.appcompat.app.ActionBar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.gruppe5.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainTestActivity : AppCompatActivity() {

    lateinit var mapTest: Button
    lateinit var apiTest: Button
    lateinit var fragmentTest: Button
    lateinit var xxx: Button
    lateinit var navView: BottomNavigationView
    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_main)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarMain).title ="Main side"  // endrer tekst paa toolbar

        assignId()
        setOnClickers()

        // setter opp navbar
        navView.setupWithNavController(findNavController(R.id.nav_host_fragment))
    }

    fun assignId() {
        navView = findViewById(R.id.nav_view)
        textView = findViewById(R.id.mainText_en)
        apiTest = findViewById(R.id.api_test_knapp)
        mapTest = findViewById(R.id.mapTest_knapp)
        fragmentTest = findViewById(R.id.fragment_knapp)
        xxx = findViewById(R.id.xxx_knapp)
    }

    // setter onClickers for kart og API_test
    fun setOnClickers(){

        // gaar til map-tester
        mapTest.setOnClickListener {
            val map = Intent(this, MapTest::class.java)
            startActivity(map)
        }

        // gaar til API_tester
        apiTest.setOnClickListener {
            val api = Intent(this, API_test::class.java)
            startActivity(api)
        }

        // aapner fragment-tester
        fragmentTest.setOnClickListener {
            val fragment = Intent(this, FragmentTest::class.java)
            startActivity(fragment)
        }
    }
}
