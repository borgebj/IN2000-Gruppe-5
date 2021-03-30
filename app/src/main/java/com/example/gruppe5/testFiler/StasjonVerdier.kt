package com.example.gruppe5.testFiler

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.gruppe5.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class StasjonVerdier : AppCompatActivity() {

    lateinit var boks : TextView
    lateinit var input : EditText
    lateinit var submit : Button
    val baseURL: String = "https://api.met.no/weatherapi/airqualityforecast/0.1"



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stasjon_verdier)

        val string = "2018-04-10T04:00:00.000Z"
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val date: LocalDate = LocalDate.parse(string, formatter)
        println(date)


        val kirkeveien = "NO0011A"
        val reftime = "2021-03-28T12%3A00%3A00Z"
        val klokkeslett = "2021-03-30T16:00:00Z"
        val type = "o3"

        assignId()
        parseJson(kirkeveien, reftime, klokkeslett, type)
    }


    fun assignId() {
        boks = findViewById(R.id.verdiBoks)
        input = findViewById(R.id.verdiInput)
        submit = findViewById(R.id.submit)
    }

    // henter JSON/XML via KHTTP -> til String
    fun getData(del: String): String {
        val full = "$baseURL$del"
        return khttp.get(full).text
    }

    fun parseJson(stasjon: String, refTime: String, klokkeslett: String, type: String) {
        submit.setOnClickListener {

            var fullString = ""

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

                    // sammenligner gitt tidspunkt og type
                    if (tidspunkt.get("from") == klokkeslett) {
                        when(type) {
                            "no2" -> fullString = "${tidspunkt.get("from")} - $no2"
                            "pm10" -> fullString = "${tidspunkt.get("from")} - $pm10"
                            "pm25" -> fullString = "${tidspunkt.get("from")} - $pm25"
                            "o3" -> fullString = "${tidspunkt.get("from")} - $o3"
                        }
                    }
                }

                // endrer teksten i boksen
                withContext(Dispatchers.Main) {
                    boks.text = fullString
                }
            }
        }
    }
}

// Teststasjoner:
// Kirkeveien : NO0011A
// Sentrum : NO0113A
// Svanvik : NO0047R
