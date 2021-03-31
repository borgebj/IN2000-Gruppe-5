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
    lateinit var submit: Button


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
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        assignId()
        addSpinnerAdapter()
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
        submit = findViewById(R.id.submit)
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

    fun parseJson(eoi : String) {
        var fullString = ""

        submit.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {

                // linken med spesifikk stasjon og tidspunkt (Kan og maa modifiseres !)
                val json = getData("/?station=${eoi}&reftime=${reftime}")

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
                    val idagListe = idag.split("T")
                    val idagDato = idagListe[0]
                    val idagKlokke = idagListe[1]

                    // henter og splitter tidspunkter fra api-tidspunktene
                    val apiList = tidspunkt.get("from").toString().split("T")
                    val apiDato = apiList[0]
                    val apiKlokke = apiList[1]

                    // sammenligner gitt tidspunkt og type
                    if (apiDato == idagDato && apiKlokke <= idagKlokke) {
                        when (type) {
                            "no2" -> fullString = "${"%.4f".format(no2.get("value"))}"
                            "pm10" -> fullString = "${"%.4f".format(pm10.get("value"))}"
                            "pm25" -> fullString = "${"%.4f".format(pm25.get("value"))}"
                            "o3" -> fullString = "${"%.4f".format(o3.get("value"))}"
                        }
                    }
                }
            }
        }
    }


    // metode for Coroutine -> legger til ALLE stasjoner
    suspend fun addStasjoner() {

        // gaar ut av coroutine for aa legge til
        withContext(Dispatchers.Main) {
            for (stasjon in stasjoner) {
                val lat: Double = stasjon.latitude
                val lon: Double = stasjon.longitude
                val location = LatLng(lat, lon)

                //TODO: Få dette til å fungere
                val text = parseJson(stasjon.eoi)
                println(text)

                mMap.addMarker(MarkerOptions()
                        .position(location)
                        .title(stasjon.name+" - "+text)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)) // color
                        .alpha(0.9F) // Opacity
                        .flat(true) // flattener-marker
                )
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        type = parent.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}