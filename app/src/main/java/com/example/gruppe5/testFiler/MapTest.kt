package com.example.gruppe5.testFiler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ToggleButton
import com.example.gruppe5.R
import com.example.gruppe5.Stasjon
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*

class MapTest : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    lateinit var visMarker: ToggleButton
    lateinit var resetCamera: Button


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

        assignId()
        getInfo()
        setOnClickers()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(60.472024, 8.468946), 5.0f)) // flytter til Norge

        // legger til allerede-lagde funksjoner fra google-maps
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true

        // HOWTO: Legge til markoer
        // 1. val sydney = LatLng(-34.0, 151.0)
        // 2. mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        // brukes for aa sette fokus:
        // 3. mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    fun assignId() {
        visMarker = findViewById(R.id.addButton)
        resetCamera = findViewById(R.id.resetButton)
    }

    fun setOnClickers() {

        // onClicker for markører - viser knapper og resetter kamera
        visMarker.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                CoroutineScope(Dispatchers.Main).launch {
                    addStasjoner()
                }
            } else mMap.clear()
        }
        resetCamera.setOnClickListener {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(60.472024, 8.468946), 5.0f)) // flytter til Norge
        }
    }

    // metode som parser fra start-data fra JSON
    fun getInfo() {

        //region (coroutine-1) starter en coroutine for aa parse
        CoroutineScope(Dispatchers.IO).launch {
            val rawJSON = getData("/stations")

            val collectionType = object : TypeToken<Collection<Stasjon?>?>() {}.type
            val stasjonArray: ArrayList<Stasjon> = gson.fromJson(rawJSON, collectionType)

            for (stasjon in stasjonArray) {
                stasjoner.add(stasjon)
            }
        } //endregion
    }

    // henter JSON/XML via KHTTP -> til String
    fun getData(del: String): String {
        val full = "$baseURL$del"
        return khttp.get(full).text
    }



    // metode for Coroutine -> legger til ALLE stasjoner
    suspend fun addStasjoner() {

        // gaar ut av coroutine for aa legge til
        withContext(Dispatchers.Main) {
            for (stasjon in stasjoner) {
                val lat: Double = stasjon.latitude
                val lon: Double = stasjon.longitude
                val location = LatLng(lat, lon)
                mMap.addMarker(MarkerOptions()
                        .position(location)
                        .title(stasjon.name)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)) // color
                        .alpha(0.9F) // Opacity
                        .flat(true) // flattener-marker
                )
            }
        }
    }
}