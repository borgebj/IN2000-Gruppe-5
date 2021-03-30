package com.example.gruppe5.testFiler

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.example.gruppe5.R
import androidx.appcompat.app.AppCompatActivity


class MainTestActivity : AppCompatActivity() {

    lateinit var en : Button
    lateinit var to : Button
    lateinit var tre : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_main)

        assignId()
        setOnClickers()
    }

    fun assignId() {
        en = findViewById(R.id.butEn)
        to = findViewById(R.id.butTo)
        tre = findViewById(R.id.butTre)
    }

    fun setOnClickers() {
        en.setOnClickListener {
            val verdier = Intent(this, StasjonVerdier::class.java)
            startActivity(verdier)
        }
        to.setOnClickListener {
            val recycler = Intent(this, API_test::class.java)
            startActivity(recycler)
        }
        tre.setOnClickListener {
            val map = Intent(this, MapTest::class.java)
            startActivity(map)
        }
    }
}
