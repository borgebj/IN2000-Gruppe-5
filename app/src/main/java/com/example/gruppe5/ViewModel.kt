package com.example.gruppe5

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import android.util.Log
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
        return Fuel.get(full)
            .header("User-Agent", "IN2000-Gruppe-5-App/1.0 (https://github.com/uio-in2000/IN2000-Gruppe-5)")
            .awaitString()
    }

    // henter data fra AirQuality (metrologisk institutt API)
    private fun parseData() {
        val baseURLMetro = "https://api.met.no/weatherapi/airqualityforecast/0.1" // AirQuality PI url

        // [indre metode] henter alle stasjoner
        suspend fun getStations() : MutableList<Stasjon> = Gson().fromJson(
                getData(
                        baseURLMetro,
                        "/stations"
                ), Array<Stasjon>::class.java
        ).toMutableList()

        // henter og tildeler verdier til alle stasjoner
        suspend fun getValues(stasjoner: MutableList<Stasjon>) = coroutineScope {
            stasjoner.map { station ->
                async {
                    if (station.eoi.isNullOrEmpty()) return@async
                    try {
                        val valueJson = getData(baseURLMetro, "/?station=${station.eoi}")
                        val objekt = JSONObject(valueJson)
                        val data = objekt.optJSONObject("data") ?: return@async
                        val timeList = data.optJSONArray("time") ?: return@async

                        var bestMatch: HashMap<String, Double>? = null
                        var minDiff = Int.MAX_VALUE
                        val currentHour = try { today[1].split(":")[0].toInt() } catch (e: Exception) { -1 }

                        for (i in 0 until timeList.length()) {
                            val timeObject = timeList.getJSONObject(i)
                            val from = timeObject.optString("from") ?: continue
                            val times = from.split("T")

                            if (times.isNotEmpty() && times[0] == today[0]) {
                                val variables = timeObject.optJSONObject("variables") ?: continue
                                val map = HashMap<String, Double>()

                                fun getVal(key: String): Double {
                                    return try {
                                        val v = variables.getJSONObject(key).getDouble("value")
                                        String.format(Locale.ENGLISH, "%.2f", v).toDouble()
                                    } catch (e: Exception) { 0.0 }
                                }

                                map["no2"] = getVal("no2_concentration")
                                map["pm10"] = getVal("pm10_concentration")
                                map["pm25"] = getVal("pm25_concentration")
                                map["o3"] = getVal("o3_concentration")

                                val forecastHour = try { times[1].split(":")[0].toInt() } catch (e: Exception) { -2 }
                                val diff = Math.abs(forecastHour - currentHour)
                                if (diff < minDiff) {
                                    minDiff = diff
                                    bestMatch = map
                                }
                                if (diff == 0) break
                            }
                        }
                        if (bestMatch != null) station.verdier = bestMatch
                    } catch (e: Exception) {
                        Log.e("ViewModel", "Error fetching data for station ${station.name}", e)
                    }
                }
            }.awaitAll()
        }

        // starter coroutine som kjorer funksjonene og parser
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val stasjoner = getStations()
                // Post early to improve perceived speed
                stations.postValue(stasjoner)
                getValues(stasjoner)
                // Post again once all values are fetched
                stations.postValue(stasjoner)
            } catch (e: Exception) {
                Log.e("ViewModel", "Error parsing air quality data: ${e.message}", e)
            }
        }
    }

    // setter default-state til stasjon (i Oslo) med høyeste verdi
    private fun setDefaultState(stations: MutableList<Stasjon>) {
        var currentHighestStation: Stasjon? = if (stations.isNotEmpty()) stations.random() else null
        var currentHighestValue = -1.0

        for (stasjon in stations) {
            val isInOslo = stasjon.kommune?.name == "Oslo" || stasjon.path?.contains("Oslo", ignoreCase = true) == true
            if (isInOslo) {
                val highest = stasjon.verdier?.maxByOrNull { it.value }
                if (highest != null) {
                    if (highest.value > currentHighestValue) {
                        currentHighestValue = highest.value
                        currentHighestStation = stasjon
                    }
                }
            }
        }
        if (currentHighestStation != null) {
            nearestStation.postValue(currentHighestStation)
        }
    }

    //region [nearby stations]
    @SuppressLint("MissingPermission")
    fun findNearestStation(fusedLocationClient: FusedLocationProviderClient, stations: MutableList<Stasjon>, GpsStatus: Boolean) {
        var nearest : Stasjon? = null
        var closest = Float.MAX_VALUE

        // selve requesten
        val mLocationRequest = LocationRequest.create().setInterval(60000).setFastestInterval(5000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        val locationCallback = object : LocationCallback() {}
        fusedLocationClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.getMainLooper())

        if (GpsStatus) {
            usingDefault = false
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location == null) {
                    usingDefault = true
                    setDefaultState(stations)
                    return@addOnSuccessListener
                }

                for (stasjon in stations) {
                    val lat = stasjon.latitude ?: continue
                    val lng = stasjon.longitude ?: continue
                    // oppretter Location-objekter
                    val stationLocation = Location("")
                    stationLocation.latitude = lat
                    stationLocation.longitude = lng

                    // sammenligner avstand mellom "her" og markoer, og sjekker hvem er naermest
                    val distance = location.distanceTo(stationLocation)
                    if (distance < closest) {
                        closest = distance
                        nearest = stasjon
                    }
                }

                if (nearest != null) {
                    nearestStation.postValue(nearest)
                } else {
                    usingDefault = true
                    setDefaultState(stations)
                }
            }.addOnFailureListener {
                usingDefault = true
                setDefaultState(stations)
            }
        }
        // om bruk av GPS ikke er tillatt, bruk defaultstate (høyeste i Oslo)
        else {
            usingDefault = true
            setDefaultState(stations)
        }
    }
    //endregion
}