package com.example.gruppe5.testFiler

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.gruppe5.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class StasjonVerdier : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    lateinit var boks : TextView
    lateinit var input : EditText
    lateinit var submit : Button
    lateinit var spinner: Spinner
    val baseURL: String = "https://api.met.no/weatherapi/airqualityforecast/0.1"
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'") // tidsformat brukt i API og prosjekt

    // testvariabler ! Disse kan endres!
    var stasjon = "NO0011A"
    val reftime = "2021-03-28T12%3A00%3A00Z"
    var type = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stasjon_verdier)
        findViewById<RelativeLayout>(R.id.relative).setOnClickListener { closeKeyboard(findViewById(R.id.relative)) }


        // test-dato og naa-dato
        val klokkeslett = "2021-03-30T16:00:00Z"
        val idag = dateFormat.format(Calendar.getInstance().time)


        assignId()
        addSpinnerAdapter()

        // parser
        parseJson(reftime, idag)
    }

    // lukker "keyboard"-funksjon (etter knappetrykk)
    private fun closeKeyboard(view: View) {
        val hide = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        hide.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun assignId() {
        boks = findViewById(R.id.verdiBoks)
        input = findViewById(R.id.verdiInput)
        submit = findViewById(R.id.submit)
        spinner = findViewById(R.id.spinner)
    }
    fun addSpinnerAdapter() {
        ArrayAdapter.createFromResource(this, R.array.spinner, android.R.layout.simple_spinner_item).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = it }
        spinner.onItemSelectedListener = this
    }

    // henter JSON/XML via KHTTP -> til String
    fun getData(del: String): String {
        val full = "$baseURL$del"
        return khttp.get(full).text
    }

    fun parseJson(refTime: String, klokkeslett: String) {
        submit.setOnClickListener {
            closeKeyboard(it)
            if (input.text.toString() != "") stasjon = input.text.toString()
            CoroutineScope(Dispatchers.IO).launch {

                // linken med spesifikk stasjon og tidspunkt (Kan og maa modifiseres !)
                val json = getData("/?station=${stasjon}&reftime=${refTime}")

                val jsonobjekt = JSONObject(json)
                val data : JSONObject = jsonobjekt.getJSONObject("data")
                val tider : JSONArray = data.getJSONArray("time")

                // itererer gjennom tider-listen (time under data)
                for (i in 1 until tider.length()) {
                    val tidspunkt = tider.getJSONObject(i)
                    val variabler = tidspunkt.getJSONObject("variables")


                    // relevante variabler for luftkvalitet
                    val no2 = variabler.getJSONObject("AQI_no2")
                    val pm10 = variabler.getJSONObject("AQI_pm10")
                    val pm25 = variabler.getJSONObject("AQI_pm25")
                    val o3 = variabler.getJSONObject("AQI_o3")

                    // henter og splitter tidspunkt fra i dag
                    val idagListe = klokkeslett.split("T")
                    val idagDato = idagListe[0]
                    val idagKlokke = idagListe[1]

                    // henter og splitter tidspunkter fra api-tidspunktene
                    val apiList = tidspunkt.get("from").toString().split("T")
                    val apiDato = apiList[0]
                    val apiKlokke = apiList[1]

                    // sammenligner gitt tidspunkt og type
                    if (apiDato == idagDato && apiKlokke <= idagKlokke) {
                        withContext(Dispatchers.Main) {
                            when (type) {
                                "no2" -> boks.text = "${stasjon} \n ${tidspunkt.get("from")} \n no2: ${"%.4f".format(no2.get("value"))}"
                                "pm10" -> boks.text = "${stasjon} \n ${tidspunkt.get("from")} \n pm10: ${"%.4f".format(pm10.get("value"))}"
                                "pm25" -> boks.text = "${stasjon} \n ${tidspunkt.get("from")} \n pm25: ${"%.4f".format(pm25.get("value"))}"
                                "o3" -> boks.text = "${stasjon} \n ${tidspunkt.get("from")} \n o3: ${"%.4f".format(o3.get("value"))}"
                            }
                        }
                    }
                }
            }
        }
    }

    // endrer typen ved item-selected
    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        type = parent.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}

// Teststasjoner:
// Kirkeveien : NO0011A
// Sentrum : NO0113A
// Svanvik : NO0047R
