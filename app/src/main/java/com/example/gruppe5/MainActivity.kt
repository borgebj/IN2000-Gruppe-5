package com.example.gruppe5

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    lateinit var mapTest: Button
    lateinit var api_test: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        assignId()
        setOnClickers()
    }

    fun assignId() {
        api_test = findViewById(R.id.button1)
        mapTest = findViewById(R.id.button2)
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