package com.example.gruppe5

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView


class API_test : AppCompatActivity() {

    lateinit var text_en: TextView
    lateinit var text_to: TextView
    val baseURL: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_a_p_i_test)

        assignId()
        hentInfo()
    }

    fun assignId() {
        text_en = findViewById(R.id.textView)
        text_to = findViewById(R.id.textView2)
    }

    // henter JSON/XML via KHTTP -> til String
    fun getData(del: String): String {
        val full = "$baseURL$del"
        return khttp.get(full).text
    }

    fun hentInfo() {
        //TEST
    }
}