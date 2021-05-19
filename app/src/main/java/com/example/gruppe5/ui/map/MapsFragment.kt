package com.example.gruppe5.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.LocationManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.gruppe5.R
import com.example.gruppe5.Stasjon
import com.example.gruppe5.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import java.util.*


// SKAL INNEHOLDE UI/kode som endrer viewet

class MapsFragment : Fragment(), TextToSpeech.OnInitListener {

    // viewmodel
    private val viewModel: ViewModel by activityViewModels() // <- bruker EN felles viewmodel

    // elementer
    private lateinit var mMap: GoogleMap
    private lateinit var root: View
    private lateinit var switch: SwitchCompat

    // maps stuff
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationManager: LocationManager? = null
    private var gpsStatus = false

    private var svar: String? = null // svar fra search-fragment
    private var tts: TextToSpeech? = null
    private var ttsStatus = true

    // search
    private lateinit var adapter: ArrayAdapter<*>


    private val callback = OnMapReadyCallback { Map ->
        mMap = Map
        tts = TextToSpeech(this.context, this)

        // starter med aa flytte kamera til Norge
        mMap.setPadding(0, 0, 0, 120)
        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(60.472024, 8.468946),
                5.0f
            )
        ) // flytter til Norge

        // henter svar fra search-knappen
        svar = MapsFragmentArgs.fromBundle(requireArguments()).map

        addMapFunctions()
        addOnClickers()
        addSwitchFunction()
        addMarkers()
        createStationFromSearch()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root: View = inflater.inflate(R.layout.fragment_maps, container, false)
        this.root = root

        assignId(root)
        setHasOptionsMenu(true)

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    fun assignId(root: View) {
        switch = root.findViewById(R.id.heatmap_Switch)
        adapter = ArrayAdapter(
            root.context, android.R.layout.simple_list_item_1, resources.getStringArray(
                R.array.search_bar_strings
            )
        )
    }

    private fun checkGpsStatus() {
        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsStatus = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(root.context)
    }

    // legger til funksjoner fra google-maps
    @SuppressLint("MissingPermission")
    fun addMapFunctions() {
        checkGpsStatus()
        if (gpsStatus) {
            Toast.makeText(requireContext(), "Lokasjon på", Toast.LENGTH_SHORT).show()
            mMap.isMyLocationEnabled = true
        } else {
            mMap.isMyLocationEnabled = false
            mMap.uiSettings.isMyLocationButtonEnabled = false

        }
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true
    }

    // setter onClicker for infoWindow til hver markoer
    @SuppressLint("PotentialBehaviorOverride")
    fun addOnClickers() {

        // onclick til infovindu, aapner location-fragment
        mMap.setOnInfoWindowClickListener { marker ->
            val marker_title: String? = marker.title
            viewModel.stations.observe(viewLifecycleOwner) { list ->

                for (stasjon in list) {
                    val navn = marker_title
                    if (navn == stasjon.name) {
                        // tts-test
                        if (ttsStatus) {
                            tts!!.speak(
                                    "Opening page $navn",
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    ""
                            )
                        }
                        Toast.makeText(this.context, "Åpner side ...", Toast.LENGTH_SHORT)
                                .show() // informerer bruker

                        // venter i (ca) 2 sec for endret (postDelayed for aa vente)
                        try {
                            root.postDelayed({
                                val action =
                                        MapsFragmentDirections.actionNavigationMapToNavigationLocation(
                                                stasjon
                                        )
                                root.findNavController().navigate(action)
                            }, 1500)
                        } catch (e: Exception) { // denne kastes om man prøver å navigere til et annet fragment mens den venter
                            e.printStackTrace()
                        }

                        marker.showInfoWindow()
                    }
                }
            }
        }

        // onclick til markers - zoomer inn
        mMap.setOnMarkerClickListener {
            val latlng = LatLng(it.position.latitude, it.position.longitude)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 10F), 2000, null)
            it.showInfoWindow()
            return@setOnMarkerClickListener true
        }
    }

    // legger til funksjon for heatmap switch on/off
    private fun addSwitchFunction() {
        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                mMap.clear()
                root.postDelayed({
                    addHeatmap()
                }, 250)
            } else {
                mMap.clear()
                root.postDelayed({
                    addMarkers()
                }, 250)
            }
        }
    }

    // legger til heatmap overlay for google map
    private fun addHeatmap() {
        viewModel.stations.observe(viewLifecycleOwner, { stasjoner ->
            val weightedData: MutableList<WeightedLatLng> = mutableListOf()

            // lager LatLng og WeightedLatLng av hver stasjon for heatmap
            for (station in stasjoner) {
                val highest: Map.Entry<String, Double>? = station.verdier.maxByOrNull { it.value }
                val verdi = station.verdier[highest?.key]
                if (verdi != null) weightedData.add(
                        WeightedLatLng(
                                LatLng(
                                        station.latitude,
                                        station.longitude
                                ), verdi
                        )
                )
            }

            // lager selve heatmap og starter
            val mProvider = HeatmapTileProvider.Builder()
                    .radius(50)
                    .weightedData(weightedData)
                    .build()

            // selve overlayen
            mMap.addTileOverlay(TileOverlayOptions().tileProvider(mProvider))

            // endrer heatmap naar kartet endres - bevegelser / zoom
            mMap.setOnCameraIdleListener {
                val newZoom = mMap.cameraPosition.zoom.toInt()
                mProvider.setRadius((10 + newZoom * 2) * 4)

                if (newZoom in 10..20) mProvider.setMaxIntensity(500.0)
                if (newZoom in 9..9) mProvider.setMaxIntensity(1000.0)
                if (newZoom in 5..8) mProvider.setMaxIntensity(2000.0)
                if (newZoom in 0..4) mProvider.setMaxIntensity(4000.0)
            }
        })
    }

    // legger til hver stasjon paa kartet - observerer alle stasjoner > oppretter > legger till
    private fun addMarkers() {
        viewModel.stations.observe(viewLifecycleOwner, { stations ->
            for (station in stations) {
                createAndAddMarker(station)
            }
        })
    }

    private fun createAndAddMarker(station: Stasjon) {
        val highest: Map.Entry<String, Double>? = station.verdier.maxByOrNull { it.value }
        val markerOptions: MarkerOptions = MarkerOptions().position(
                LatLng(
                        station.latitude,
                        station.longitude
                )
        ).title(station.name)
        checkValues(highest, markerOptions)
        mMap.addMarker(markerOptions)
    }

    // sjekker hvilke forurensnings-type det er, deres nivaaer, og kaller hjelpemetode videre
    private fun checkValues(highest: Map.Entry<String, Double>?, marker: MarkerOptions) {

        // endrer farge/ikon paa kartet avhengig av markoerenes nivaa
        fun alterMarker(level: String, marker: MarkerOptions) {
            when (level) {
                "green" -> marker.icon(bitMapFromVector(R.drawable.ic_level_one))
                "orange" -> marker.icon(bitMapFromVector(R.drawable.ic_level_two))
                "red" -> marker.icon(bitMapFromVector(R.drawable.ic_level_three))
                "purple" -> marker.icon(bitMapFromVector(R.drawable.ic_level_four))
            }
        }

        if (highest != null)
            when (highest.key) {
                "no2" -> {
                    when {
                        highest.value <= 100.0 -> alterMarker("green", marker)
                        highest.value in 100.0..200.0 -> alterMarker("orange", marker)
                        highest.value in 200.0..400.0 -> alterMarker("red", marker)
                        highest.value >= 400.0 -> alterMarker("purple", marker)
                    }
                }
                "pm10" -> {
                    when {
                        highest.value <= 60.0 -> alterMarker("green", marker)
                        highest.value in 60.0..120.0 -> alterMarker("orange", marker)
                        highest.value in 120.0..400.0 -> alterMarker("red", marker)
                        highest.value >= 400.0 -> alterMarker("purple", marker)
                    }
                }
                "pm25" -> {
                    when {
                        highest.value <= 30.0 -> alterMarker("green", marker)
                        highest.value in 30.0..50.0 -> alterMarker("orange", marker)
                        highest.value in 50.0..150.0 -> alterMarker("red", marker)
                        highest.value >= 150.0 -> alterMarker("purple", marker)
                    }
                }
                "o3" -> {
                    when {
                        highest.value <= 100.0 -> alterMarker("green", marker)
                        highest.value in 100.0..180.0 -> alterMarker("orange", marker)
                        highest.value in 180.0..240.0 -> alterMarker("red", marker)
                        highest.value >= 240.0 -> alterMarker("purple", marker)
                    }
                }
            }
    }

    // oppretter en Bitmap fra en vector fil, for å skape Bitmaps / Icons for kartet sine markører
    private fun bitMapFromVector(vectorResID: Int): BitmapDescriptor {
        val vectorDrawable = this.context?.let { ContextCompat.getDrawable(it, vectorResID) }
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    // search-fragment navigering via søkefelt øverst på kartet
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.nav_search2 -> {
            val map = "map"
            val action = MapsFragmentDirections.actionNavigationMapToNavigationSearch(map)
            root.findNavController().navigate(action)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    // henter stasjon fra search og navigerer til markøren
    private fun createStationFromSearch() {
        viewModel.stations.observe(viewLifecycleOwner, { stations ->
            for (station in stations) {
                if (station.name == svar.toString()) {
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(station.latitude, station.longitude), 15F
                        ), 2000, null
                    )
                }
            }
        })
    }

    // Initializer for Text-to-Speech
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.US) // henter språk
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language not supported")
            }
        } else {
            Log.e("TTS", "Initialization failed")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_bar_on_map_menu, menu)

    }
}


// for koordinater (nøyaktighet)
/*decimal
places	degrees	distance
0  1.0	       111 km
1  0.1	       11.1 km
2  0.01	       1.11 km
3  0.001	   111 m
4  0.0001	   11.1 m
5  0.00001	   1.11 m
6  0.000001    0.111 m
7  0.0000001   1.11 cm
8  0.00000001  1.11 mm

- dvs at lon 59.90 er 11km fra lon 60.90
- 59.90 er 11.1km fra 59.80 */

/* quick-info
11.1 km = +/- 0.1
5/6 km = +/- 0.05
*/
