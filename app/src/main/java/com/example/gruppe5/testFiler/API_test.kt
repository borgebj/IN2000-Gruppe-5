package com.example.gruppe5.testFiler

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gruppe5.R
import com.example.gruppe5.Stasjon
import com.example.gruppe5.StasjonAdapter
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
            info.text = "WIP - Ikke lagt til enda"
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