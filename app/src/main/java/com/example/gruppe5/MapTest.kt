package com.example.gruppe5

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapTest : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    // liste med stasjoner og Gson
    var stasjoner: MutableList<Stasjon> = mutableListOf()
    var gson = Gson()
    var baseURL: String = "https://api.met.no/weatherapi/airqualityforecast/0.1"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_test)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getInfo()

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)

        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    // henter JSON/XML via KHTTP -> til String
    fun getData(del: String): String {
        val full = "$baseURL$del"
        return khttp.get(full).text
    }

    // metode for Coroutine -> legger til ALLE stasjoner
    suspend fun addMaps() {

        // gaar ut av coroutine for aa legge til
        withContext(Dispatchers.Main) {
            for (stasjon in stasjoner) {
                val lat = stasjon.latitude
                val lon = stasjon.longitude
                val location = LatLng(lat, lon)
                mMap.addMarker(MarkerOptions().position(location).title(stasjon.name))
            }
        }
    }

    // metode som parser fra start-data fra JSON
    fun getInfo() {

        //region (coroutine-1) starter en coroutine for aa parse
        CoroutineScope(Dispatchers.IO).launch {
            val rawJSON = getData("/stations")

            val collectionType = object : TypeToken<Collection<Stasjon?>?>() {}.type
            val fake_liste: ArrayList<Stasjon> = gson.fromJson(rawJSON, collectionType)

            for (stasjon in fake_liste) {
                stasjoner.add(stasjon)
            }
            addMaps()
        } //endregion
    }
}