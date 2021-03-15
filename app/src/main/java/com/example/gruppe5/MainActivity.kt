package com.example.gruppe5

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    lateinit var videre: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        assignId()
        main()
    }

    fun assignId() {
        videre = findViewById(R.id.button1)
    }

    fun main() {
        println("--------------------")
        println("Committ her: Navn")
        println("Birgitte")

        println("Børge")

        println("Elias")

        println("Jonas er allergisk mot fisk")
        println("heeeelt ærlig")

        println("--------------------")

        println("Elias")
        println("Test borge elias")
        println("test2 borge")

        println("Rie test")
        println("Rie test2")


        // gaar til API_test-siden
        videre.setOnClickListener {
            val intent = Intent(this, API_test::class.java)
            startActivity(intent)
        }
    }
}