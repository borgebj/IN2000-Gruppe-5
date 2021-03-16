package com.example.gruppe5

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class API_test : AppCompatActivity() {

    lateinit var listView: ListView
    lateinit var button: Button
    val gson = Gson()
    var stasjoner: MutableList<Stasjon> = mutableListOf()
    val baseURL: String = "https://api.met.no/weatherapi/airqualityforecast/0.1"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_a_p_i_test)

        assignId()
        hentInfo()
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
            val response = getData("/studier/emner/matnat/ifi/IN2000/v21/obligatoriske-oppgaver/alpakkaland/alpacaparties.json")

            //TODO: Fiks det her idk ass
            val json: List<Stasjon> = gson.fromJson(response, listOf<Stasjon>())
            // legger til listene med AlpacaParty inn i den globale listen
            for (parti in json) {
                println(parti)
            }
        } //endregion
    }
}