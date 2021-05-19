package com.example.gruppe5

import android.annotation.SuppressLint
import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
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

    var usingDefault: Boolean = false

    val nearestStation: MutableLiveData<Stasjon> by lazy { MutableLiveData<Stasjon>() }

    val stations: MutableLiveData<MutableList<Stasjon>> by lazy { MutableLiveData<MutableList<Stasjon>>() }

    @SuppressLint("SimpleDateFormat")
    val today = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(Calendar.getInstance().time).split(
        "T"
    ) // dagens dato og tid splittet i to


    // henter JSON/XML via KHTTP -> til String
    private suspend fun getData(base: String, del: String): String {
        val full = "$base$del"
        return Fuel.get(full).awaitString()
    }

    // henter data fra AirQuality (metrologisk institutt API)
    private fun parseData() {
        val baseURLMetro = "https://in2000-apiproxy.ifi.uio.no/weatherapi/airqualityforecast/0.1" // AirQuality PI url

        // [indre metode] henter alle stasjoner
        suspend fun getStations() : MutableList<Stasjon> = Gson().fromJson(
            getData(
                baseURLMetro,
                "/stations"
            ), Array<Stasjon>::class.java
        ).toMutableList()

        // henter og tildeler verdier til alle stasjoner
        suspend fun getValues(stations: MutableList<Stasjon>) {
            for (station in stations) {
                val valueJson = getData(baseURLMetro, "/?station=${station.eoi}")
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
                    val timeIsValid: Boolean =
                        times[1] >= slit[0] && times[1] <= (slit[0].toInt() + 1).toString()

                    // sammenligner dato og tidspunkt for aa hente verdier for NAA
                    if (times[0] == today[0] && timeIsValid) {
                        val map = HashMap<String, Double>()
                        map["no2"] = String.format(
                            "%.2f", variables.getJSONObject("no2_concentration").get(
                                "value"
                            )
                        ).toDouble()
                        map["pm10"] = String.format(
                            "%.2f", variables.getJSONObject("pm10_concentration").get(
                                "value"
                            )
                        ).toDouble()
                        map["pm25"] = String.format(
                            "%.2f", variables.getJSONObject("pm25_concentration").get(
                                "value"
                            )
                        ).toDouble()
                        map["o3"] = String.format(
                            "%.2f", variables.getJSONObject("o3_concentration").get(
                                "value"
                            )
                        ).toDouble()
                        station.verdier = map
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
    private fun setDefaultState(stations: MutableList<Stasjon>) {
        var currentHighestStation: Stasjon? = stations.random()
        var currentHighestValue = 0.0

        for (stasjon in stations) {
            if (stasjon.kommune.name == "Oslo") {
                val highest = stasjon.verdier.maxByOrNull { it.value }
                if (highest != null)
                    if (highest.value > currentHighestValue) {
                        currentHighestValue = highest.value
                        currentHighestStation = stasjon
                    }
            }
        }
        nearestStation.postValue(currentHighestStation)
    }

    //region [nearby stations]
    @SuppressLint("MissingPermission")
    fun findNearestStation(
        fusedLocationClient: FusedLocationProviderClient,
        stations: MutableList<Stasjon>,
        GpsStatus: Boolean
    ) {
        var nearest : Stasjon? = null
        var closest = 100000.00F


        if (GpsStatus) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                for (stasjon in stations) {
                    if (location == null) {
                        usingDefault = false
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
                    }; nearestStation.postValue(nearest)
                }
            }
        }
        else {
            usingDefault = true
            setDefaultState(stations) }
    }

    //endregion
}