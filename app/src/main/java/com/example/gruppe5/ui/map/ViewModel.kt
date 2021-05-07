package com.example.gruppe5.ui.map

import android.annotation.SuppressLint
import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gruppe5.Stasjon
import com.google.android.gms.location.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

// skal inneholde logikk

class ViewModel : ViewModel() {
    init {
        parseData()
    }

    val nearest_station: MutableLiveData<Stasjon> by lazy { MutableLiveData<Stasjon>() }

    val stations: MutableLiveData<MutableList<Stasjon>> by lazy { MutableLiveData<MutableList<Stasjon>>() }

    val today = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(Calendar.getInstance().time).split("T") // dagens dato og tid splittet i to


    // henter JSON/XML via KHTTP -> til String
    fun getData(base: String, del: String): String {
        val full = "$base$del"
        return khttp.get(full).text
    }

    // henter data fra AirQuality (metrologisk institutt API)
    fun parseData() {
        val baseURLMetro: String = "https://api.met.no/weatherapi/airqualityforecast/0.1" // AirQuality PI url

        //TODO bruk disse ! (i funfacts?)
        var highestValueInNorway : Double = 0.0
        var lowestValueInNorway : Double = 500.0

        // henter alle stasjoner
        fun getStations() : MutableList<Stasjon> = Gson().fromJson(getData(baseURLMetro,"/stations"), Array<Stasjon>::class.java).toMutableList()

        // henter og tildeler verdier til alle stasjoner
        fun getValues(stations : MutableList<Stasjon>) {
            for (station in stations) {
                val valueJson = getData(baseURLMetro,"/?station=${station.eoi}")
                val objekt = JSONObject(valueJson)

                // main-data
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

        }
    }

    // setter default-state til stasjon (i Oslo) med høyeste verdi
    fun setDefaultState(stations: MutableList<Stasjon>) {
        var current_highest_station: Stasjon? = stations.random()
        var current_highest_value = 0.0

        for (stasjon in stations) {
            if (stasjon.kommune.name == "Oslo") {
                val highest = stasjon.verdier.maxBy { it.value }
                if (highest != null)
                    if (highest.value > current_highest_value) {
                        current_highest_value = highest.value
                        current_highest_station = stasjon
                    }
            }
        };
        nearest_station.postValue(current_highest_station)
    }

    //region [nearby stations]
    @SuppressLint("MissingPermission")
    fun findNearestStation(fusedLocationClient: FusedLocationProviderClient, stations: MutableList<Stasjon>, GpsStatus: Boolean) {
        var nearest : Stasjon? = null
        var closest: Float = 100000.00F


        if (GpsStatus) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                for (stasjon in stations) {
                    if (location == null) {
                        setDefaultState(stations); break
                    }
                    else {
                        // oppretter Location-objekter
                        val myLocation = Location("")
                        myLocation.latitude = location.latitude
                        myLocation.longitude = location.longitude

                        val stationLocation = Location("")
                        stationLocation.latitude = stasjon.latitude
                        stationLocation.longitude = stasjon.longitude

                        // sammenligner avstand mellom "her" og markoer, og sjekker hvem er naermest
                        val distance = myLocation.distanceTo(stationLocation)
                        if (distance <= closest) {
                            closest = distance
                            nearest = stasjon
                        }
                    }; nearest_station.postValue(nearest)
                }
            }
        }
        else setDefaultState(stations)
    }

    //endregion
}