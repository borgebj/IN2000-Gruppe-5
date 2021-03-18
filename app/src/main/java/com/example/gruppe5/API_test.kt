package com.example.gruppe5

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class API_test : AppCompatActivity() {

    lateinit var recycler: RecyclerView
    lateinit var adapter: StasjonAdapter
    lateinit var button: Button
    var stasjoner: MutableList<Stasjon> = mutableListOf()
    val baseURL: String = "https://api.met.no/weatherapi/airqualityforecast/0.1"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_a_p_i_test)

        assignId()
        addAdapter()

        // naar knappen klikkes
        button.setOnClickListener {
            hentInfo()
        }
    }

    // assigner ID'er
    fun assignId() {
        recycler = findViewById(R.id.recycler)
        button = findViewById(R.id.button2)
        recycler.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

    // legger til adapter til RecyclerView
    fun addAdapter() {
        adapter = StasjonAdapter(stasjoner)
        recycler.adapter = adapter
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

            try {
                val test = JSONArray(response)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            //val listPersonType = object : TypeToken<List<Stasjon>>() {}.type
            //val station: List<Stasjon> = gson.fromJson(response, listPersonType)
            //val list: Array<Stasjon> = gson.fromJson(response, Array<Stasjon>::class.java)

        } //endregion
    }
}