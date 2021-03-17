package com.example.gruppe5

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class API_test : AppCompatActivity() {

    lateinit var listView: ListView
    lateinit var button: Button
    var stasjoner: MutableList<Stasjon> = mutableListOf()
    val baseURL: String = "https://api.met.no/weatherapi/airqualityforecast/0.1"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_a_p_i_test)

        assignId()

        button.setOnClickListener {
            hentInfo()
        }
    }

    fun assignId() {
        listView = findViewById(R.id.listView)
        button = findViewById(R.id.button2)
    }

    // henter JSON/XML via KHTTP -> til String
    fun getData(del: String): String {
        val full = "$baseURL$del"
        return khttp.get(full).text
    }


    // metode som parser fra start-data fra JSON
    fun hentInfo() {
        //region (coroutine-1) starter en coroutine for aa parse
        CoroutineScope(Dispatchers.IO).launch {
            val response = getData("/stations")
            val gson = Gson()

            val listPersonType = object : TypeToken<List<Stasjon>>() {}.type
            var station: List<Stasjon> = gson.fromJson(response, listPersonType)

            for (hver in station) {
                Log.d("Stasjon:", hver.toString())
            }
        } //endregion
    }
}