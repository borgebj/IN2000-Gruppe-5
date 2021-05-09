package com.example.gruppe5.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.gruppe5.R
import com.example.gruppe5.Stasjon
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import java.util.*


// SKAL INNEHOLDE UI/kode som endrer viewet

class MapsFragment : Fragment() {

    // elementer
    lateinit var mMap: GoogleMap
    lateinit var root : View
    lateinit var switch : SwitchCompat

    // viewmodel
    lateinit var viewModel : ViewModel

    // maps stuff
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var locationManager: LocationManager? = null

    // status
    var GpsStatus = false
    var svar : String? = null // svar fra search-fragment

    // search
    lateinit var adapter : ArrayAdapter<*>


    private val callback = OnMapReadyCallback { Map ->
        mMap = Map

        // starter med aa flytte kamera til Norge
        mMap.setPadding(0, 0, 0, 120)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(60.472024, 8.468946), 5.0f)) // flytter til Norge

        // henter svar fra search-knappen
        svar = MapsFragmentArgs.fromBundle(requireArguments()).map

        addMapFunctions()
        addOnClickers()
        addSwitchFunction()
        if (svar == null) addMarkers()
        else createStationFromSearch(svar!!)
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
        viewModel = ViewModelProvider(this).get(ViewModel::class.java) // legger til viewmodel
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


    fun CheckGpsStatus() {
        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        GpsStatus = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(root.context)
    }

    // legger til funksjoner fra google-maps
    @SuppressLint("MissingPermission")
    fun addMapFunctions() {
        CheckGpsStatus()
        if (GpsStatus) {
            Toast.makeText(requireContext(), "GPS ENABLED", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "GPS NOT ENABLED", Toast.LENGTH_SHORT).show()
        }
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true

    }

    private fun createAndAddMarker(station : Stasjon) {
        val highest: Map.Entry<String, Double>? = station.verdier.maxByOrNull { it.value }
        val marker: MarkerOptions = MarkerOptions().position(LatLng(station.latitude, station.longitude)).title("[${station.name}]")
        checkValues(highest,marker)
        mMap.addMarker(marker)
    }

    // sjekker hvilke forurensnings-type det er, deres nivaaer, og kaller hjelpemetode videre
    fun checkValues(highest: Map.Entry<String, Double>?, marker: MarkerOptions) {

        // endrer farge/ikon paa kartet avhengig av markoerenes nivaa
        fun alterMarker(level: String, marker: MarkerOptions) {
            when(level) {
                "green" -> marker.icon(bitMapFromVector(R.drawable.ic_level_one))
                "orange" -> marker.icon(bitMapFromVector(R.drawable.ic_level_two))
                "red" -> marker.icon(bitMapFromVector(R.drawable.ic_level_three))
                "purple" -> marker.icon(bitMapFromVector(R.drawable.ic_level_four))
            }
        }

        if (highest != null)
            when (highest.key) {
                "no2" -> {
                    if (highest.value <= 100.0) alterMarker("green", marker)
                    else if (highest.value in 100.0..200.0) alterMarker("orange", marker)
                    else if (highest.value in 200.0..400.0) alterMarker("red", marker)
                    else if (highest.value >= 400.0) alterMarker("purple", marker)
                }
                "pm10" -> {
                    if (highest.value <= 60.0) alterMarker("green", marker)
                    else if (highest.value in 60.0..120.0) alterMarker("orange", marker)
                    else if (highest.value in 120.0..400.0) alterMarker("red", marker)
                    else if (highest.value >= 400.0) alterMarker("purple", marker)
                }
                "pm25" -> {
                    if (highest.value <= 30.0) alterMarker("green", marker)
                    else if (highest.value in 30.0..50.0) alterMarker("orange", marker)
                    else if (highest.value in 50.0..150.0) alterMarker("red", marker)
                    else if (highest.value >= 150.0) alterMarker("purple", marker)
                }
                "o3" -> {
                    if (highest.value <= 100.0) alterMarker("green", marker)
                    else if (highest.value in 100.0..180.0) alterMarker("orange", marker)
                    else if (highest.value in 180.0..240.0) alterMarker("red", marker)
                    else if (highest.value >= 240.0) alterMarker("purple", marker)
                }
            }
    }

    // legger til hver stasjon paa kartet - observerer alle stasjoner > oppretter > legger till
    fun addMarkers() {
        viewModel.stations.observe(viewLifecycleOwner, { stations ->
            for (station in stations) {
                createAndAddMarker(station)
            }
        })
    }

    // legger til heatmap overlay for google map
    private fun addHeatmap() {
        viewModel.stations.observe(viewLifecycleOwner, { list ->
            val weightedData: MutableList<WeightedLatLng> = mutableListOf()

            // lager LatLng og WeightedLatLng av hver stasjon for heatmap
            for (station in list) {
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

    // setter onClicker for infoWindow til hver markoer
    @SuppressLint("PotentialBehaviorOverride")
    fun addOnClickers() {

        // onclick til infovindu, aapner location-fragment
        mMap.setOnInfoWindowClickListener { marker ->
            val marker_title: String? = marker.title
            viewModel.stations.observe(viewLifecycleOwner) { list ->

                for (stasjon in list) {
                    val navn = marker_title?.substring(marker_title.indexOf("[") + 1, marker_title.indexOf("]"))

                    if (navn == stasjon.name) {

                        Toast.makeText(this.context, "Opening page ...", Toast.LENGTH_SHORT)
                            .show() // informerer bruker

                        // venter i (ca) 2 sec for endret (postDelayed for aa vente)
                        root.postDelayed({
                            val action =
                                MapsFragmentDirections.actionNavigationMapToNavigationLocation(
                                    stasjon
                                )
                            root.findNavController().navigate(action)
                        }, 1500)

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
    fun addSwitchFunction() {
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

    // oppretter en Bitmap fra en vector fil, for å skape Bitmaps / Icons for kartet sine markører
    private fun bitMapFromVector(vectorResID:Int):BitmapDescriptor {
        val vectorDrawable= this.context?.let { ContextCompat.getDrawable(it,vectorResID) }
        vectorDrawable!!.setBounds(0,0, vectorDrawable.intrinsicWidth,vectorDrawable.intrinsicHeight)
        val bitmap=Bitmap.createBitmap(vectorDrawable.intrinsicWidth,vectorDrawable.intrinsicHeight,Bitmap.Config.ARGB_8888)
        val canvas=Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    // search-fragment navigering via søkefelt øverst på kartet
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.nav_search2 -> {
            val map = "map"
            val action = MapsFragmentDirections.actionNavigationMapToNavigationSearch(map)
            root.findNavController().navigate(action)
            Log.d("test", "en")
            true
        }
        else -> {
            Log.d("test",  "to")
            super.onOptionsItemSelected(item)
        }
    }

    // henter stasjon fra search og navigerer til markøren
    private fun createStationFromSearch(svar : String) {
        mMap.clear()
        viewModel.stations.observe(viewLifecycleOwner, { stations ->
            for (station in stations) {
                if (station.name == svar.toString()) {
                    createAndAddMarker(station)
                }
            }
        })
    }

    //TODO legg til reset knapp ved siden av search

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
