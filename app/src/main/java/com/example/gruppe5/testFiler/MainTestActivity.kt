package com.example.gruppe5.testFiler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.gruppe5.R

class MainTestActivity : AppCompatActivity() {

    lateinit var mapTest: Button
    lateinit var apiTest: Button
    lateinit var fragmentTest: Button
    lateinit var xxx: Button
    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_main)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarMain).title ="Main side"  // endrer tekst paa toolbar

        assignId()
        setOnClickers()

    }

    fun assignId() {
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
