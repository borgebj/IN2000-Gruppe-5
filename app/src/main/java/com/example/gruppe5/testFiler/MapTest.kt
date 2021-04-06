package com.example.gruppe5.testFiler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.gruppe5.R
import com.example.gruppe5.Stasjon
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MapTest : AppCompatActivity(), OnMapReadyCallback, AdapterView.OnItemSelectedListener {

    private lateinit var mMap: GoogleMap
    lateinit var visMarker: ToggleButton
    lateinit var resetCamera: Button


    // liste med stasjoner og Gson
    var stasjoner: MutableList<Stasjon> = mutableListOf()
    var gson = Gson()
    lateinit var spinner: Spinner
    val baseURL: String = "https://api.met.no/weatherapi/airqualityforecast/0.1"
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'") // tidsformat brukt i API og prosjekt

    // testvariabler ! Disse kan endres!
    val reftime = "2021-03-28T12%3A00%3A00Z"
    val idag = dateFormat.format(Calendar.getInstance().time)
    var type = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_test)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        assignId()
        addSpinnerAdapter()
        getInfo()
        setOnClickers()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(60.472024, 8.468946), 5.0f)) // flytter til Norge

        // legger til funksjoner fra google-maps
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true
    }

    fun addSpinnerAdapter() {
        ArrayAdapter.createFromResource(this, R.array.spinner, android.R.layout.simple_spinner_item).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = it }
        spinner.onItemSelectedListener = this
    }

    fun assignId() {
        visMarker = findViewById(R.id.addButton)
        resetCamera = findViewById(R.id.resetButton)
        spinner = findViewById(R.id.spinner)
    }

    fun setOnClickers() {

        // onClicker for markører - viser knapper og resetter kamera
        visMarker.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) addStasjoner()
            else mMap.clear()
        }
        resetCamera.setOnClickListener {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(60.472024, 8.468946), 5.0f)) // flytter til Norge
        }
    }

    // metode som parser fra start-data fra JSON
    fun getInfo() {

        //region (coroutine-1) starter en coroutine for aa parse
        CoroutineScope(Dispatchers.IO).launch {
            val stasjonJSON = getData("/stations")

            // oppretter en array med stasjon-objekter fra JSON
            val collectionType = object : TypeToken<Collection<Stasjon?>?>() {}.type
            val stasjonArray: ArrayList<Stasjon> = gson.fromJson(stasjonJSON, collectionType)

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
    fun addStasjoner() {
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

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        type = parent.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}