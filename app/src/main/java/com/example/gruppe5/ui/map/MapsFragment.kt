package com.example.gruppe5.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gruppe5.R
import com.example.gruppe5.Stasjon
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
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class MapsFragment : Fragment() {

    // elementer
    lateinit var mMap: GoogleMap
    var locationManager: LocationManager? = null
    var GpsStatus = false
    val baseURL: String = "https://api.met.no/weatherapi/airqualityforecast/0.1" // API url
    var type = "o3"

    val stations = mutableListOf<Stasjon>()
    val today = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(Calendar.getInstance().time).split(
        "T"
    ) // dagens dato og tid splittet i to



    private val callback = OnMapReadyCallback { Map ->
        mMap = Map

        // starter med aa flytte kamera til Norge
        mMap.setPadding(0, 0, 0, 120)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(60.472024, 8.468946), 5.0f)) // flytter til Norge

        addMapFunctions()
        parseData()

        mMap.addMarker(MarkerOptions().position(LatLng(59.911491, 10.757933)).title("Oslo"))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root: View = inflater.inflate(R.layout.fragment_maps, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }


    // legger til funksjoner fra google-maps

    open fun CheckGpsStatus() {
        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        GpsStatus = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
    @SuppressLint("MissingPermission")
    fun addMapFunctions() {
//        CheckGpsStatus()
//        if (GpsStatus == true) {
//            Toast.makeText(requireContext(), "GPS ENABLED", Toast.LENGTH_SHORT).show()
//
//        } else {
//            Toast.makeText(requireContext(), "GPS NOT ENABLED", Toast.LENGTH_SHORT).show()
//        }
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true

    }






    // henter JSON/XML via KHTTP -> til String
    fun getData(del: String): String {
        val full = "$baseURL$del"
        return khttp.get(full).text
    }

    fun parseData() {

        //TODO: Fjern eller spar - variabel som holder høyeste AQI-nivå i Norge
        var highestValueInNorway : Double = 0.0

        // henter alle stasjoner og henter alle verdier
        fun getStations() {
            val stationJson = getData("/stations")

            // oppretter en array med stasjon-objekter fra JSON
            val collectionType = object : TypeToken<Collection<Stasjon?>?>() {}.type
            val stasjonArray: ArrayList<Stasjon> = Gson().fromJson(stationJson, collectionType)

            for (stasjon in stasjonArray) {
                stations.add(stasjon)
            }
        }
        fun getValues() {
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

                    // sammenligner dato og tidspunkt for aa hente verdier for NAA
                    if (times[0] == today[0] && times[1] >= today[1]) {
                        val map = HashMap<String, Double>()
                        map["no2"] = variables.getJSONObject("no2_concentration").get("value").toString().toDouble()
                        map["pm10"] = variables.getJSONObject("pm10_concentration").get("value").toString().toDouble()
                        map["pm25"] = variables.getJSONObject("pm25_concentration").get("value").toString().toDouble()
                        map["o3"] = variables.getJSONObject("o3_concentration").get("value").toString().toDouble()
                        station.verdier = map

                        // skaffer hoyeste
                        for (verdi in map)
                            if (verdi.value > highestValueInNorway) highestValueInNorway = verdi.value
                    }
                }
            }
            Log.d("Highest", highestValueInNorway.toString()) // test
        }
        suspend fun addMarkers() {
            for (station in stations) {
                withContext(Dispatchers.Main) {
                    val title = "[${station.name}] - ${station.verdier.get(type)}"
                    mMap.addMarker(
                        MarkerOptions().position(
                            LatLng(
                                station.latitude,
                                station.longitude
                            )
                        ).title(title)
                    )

                    //TODO: Fjern denne, + fiks markører som ikke viser tittel
                    Log.d(station.name, station.verdier.get(type).toString())
                }
            }
        }

        // starter coroutine som parser all dataen
        CoroutineScope(Dispatchers.IO).launch {
            getStations()
            getValues()
            addMarkers()

            Log.d("Hoyeste i Norge", highestValueInNorway.toString())
        }
    }
}