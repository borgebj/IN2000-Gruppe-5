package com.example.gruppe5.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gruppe5.Stasjon
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

// skal inneholde logikk

class MapViewModel() : ViewModel() {

    val stations: MutableLiveData<MutableList<Stasjon>> by lazy {
        MutableLiveData<MutableList<Stasjon>>()
    }

    val baseURL: String = "https://api.met.no/weatherapi/airqualityforecast/0.1" // API url
    val today = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(Calendar.getInstance().time).split("T") // dagens dato og tid splittet i to


    //region OVERFØR TIL VIEWMODEL !
    // henter JSON/XML via KHTTP -> til String
    fun getData(del: String): String {
        val full = "$baseURL$del"
        return khttp.get(full).text
    }

    fun parseData() {

        //TODO: Fjern eller spar - variabel som holder høyeste+lavest AQI-nivå i Norge
        var highestValueInNorway : Double = 0.0
        var lowestValueInNorway : Double = 500.0

        // henter alle stasjoner
        fun getStations() : MutableList<Stasjon> = Gson().fromJson(getData("/stations"), Array<Stasjon>::class.java).toMutableList()

        // henter og tildeler verdier til alle stasjoner
        fun getValues(stations : MutableList<Stasjon>) {
            for (station in stations) {
                val valueJson = getData("/?station=${station.eoi}")

                val objekt = JSONObject(valueJson)

                // main-data
                val meta = objekt.getJSONObject("meta")
                val data = objekt.getJSONObject("data")


                // gaar gjennom listen med tidspunkter
                val timeList = data.getJSONArray("time")
                for (i in 0 until timeList.length()) {
                    val timeObject = timeList.getJSONObject(i)

                    val times = timeObject.get("from").toString().split("T")
                    val variables = timeObject.getJSONObject("variables")

                    // for aa sikre at tidspunktet sammenlignet er innenfor denne og neste time
                    val slit = today[1].split(":")
                    val timeIsValid : Boolean = times[1] >= slit[0] && times[1] <= (slit[0].toInt()+1).toString()

                    // sammenligner dato og tidspunkt for aa hente verdier for NAA
                    if (times[0] == today[0] && timeIsValid) {
                        val map = HashMap<String, Double>()
                        map["no2"] = String.format("%.2f", variables.getJSONObject("no2_concentration").get("value")).toDouble()
                        map["pm10"] = String.format("%.2f", variables.getJSONObject("pm10_concentration").get("value")).toDouble()
                        map["pm25"] = String.format("%.2f", variables.getJSONObject("pm25_concentration").get("value")).toDouble()
                        map["o3"] = String.format("%.2f", variables.getJSONObject("o3_concentration").get("value")).toDouble()
                        station.verdier = map

                        // skaffer hoyeste og lavest
                        for (verdi in map) {
                            if (verdi.value > highestValueInNorway) highestValueInNorway = verdi.value
                            if (verdi.value < lowestValueInNorway) lowestValueInNorway = verdi.value
                        }
                    }
                }
            }
        }

        // starter coroutine som kjorer funksjonene og parser
        CoroutineScope(Dispatchers.IO).launch {
            val stasjoner = getStations()
            getValues(stasjoner)
            stations.postValue(stasjoner)
            //stations.value = stasjoner
        }
    }

    //endregion
}