package com.example.gruppe5.testFiler

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gruppe5.R
import com.example.gruppe5.Stasjon
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class API_test : AppCompatActivity() {

    // elementer
    lateinit var recycler: RecyclerView
    lateinit var adapter: StasjonAdapter
    lateinit var hentStasjoner: Button
    lateinit var hentAnnet: Button
    lateinit var info: TextView

    val gson = Gson()
    var stasjoner: MutableList<Stasjon> = mutableListOf()
    val baseURL: String = "https://api.met.no/weatherapi/airqualityforecast/0.1"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_a_p_i_test)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarApi).title = "API_test"

        assignId()
        addAdapter()
        setOnClickers()
    }

    // assigner ID'er
    fun assignId() {
        recycler = findViewById(R.id.recycler)
        hentStasjoner = findViewById(R.id.button2)
        hentAnnet = findViewById(R.id.button3)
        info = findViewById(R.id.api_info)
        recycler.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

    // legger til adapter til RecyclerView
    fun addAdapter() {
        adapter = StasjonAdapter(stasjoner)
        recycler.adapter = adapter
    }

    // setter onClickListeners til knappene
    fun setOnClickers() {

        // viser RecyclerView med stasjoner
        hentStasjoner.setOnClickListener {
            hentOgVisRecycler()
        }
        hentAnnet.setOnClickListener {
            println(it)
        }
    }

    // henter JSON/XML via KHTTP -> til String
    fun getData(del: String): String {
        val full = "$baseURL$del"
        return khttp.get(full).text
    }


    // metode som parser fra start-data fra JSON, dermed viser RecyclerView
    fun hentOgVisRecycler() {
        //region (coroutine-1) starter en coroutine for aa parse
        CoroutineScope(Dispatchers.IO).launch {
            val rawJSON = getData("/stations")

            val collectionType = object : TypeToken<Collection<Stasjon?>?>() {}.type
            val fake_liste: ArrayList<Stasjon> = gson.fromJson(rawJSON, collectionType)

            for (stasjon in fake_liste) {
                stasjoner.add(stasjon)
            }
            withContext(Dispatchers.Main) {
                info.visibility = TextView.GONE
                adapter.notifyDataSetChanged()
            }

        } //endregion
    }
}

/*
//1 - IDK-ass, brukte stackoverflow
val listPersonType = object : TypeToken<List<Stasjon>>() {}.type
val station: List<Stasjon> = gson.fromJson(rawJSON, listPersonType)
Log.d("1", station.toString())

// 2 - bruke Array-typen til aa hente en Array med stasjoner - litt usikker om riktig eller galt
val list: Array<Stasjon> = gson.fromJson(rawJSON, Array<Stasjon>::class.java)
Log.d("2", list.toString())

// 3 - bruke JSONObject og JSONArray
val root = JSONArray(rawJSON)
Log.d("3", root.toString())*/
