package com.example.gruppe5.ui.map
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.gruppe5.R
import com.example.gruppe5.Stasjon
import com.example.gruppe5.ui.home.HomeViewModel
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

class MapsFragment : Fragment(), TextToSpeech.OnInitListener {

    // elementer
    lateinit var mMap: GoogleMap
    lateinit var overlay : TileOverlay
    lateinit var root : View
    lateinit var switch : SwitchCompat

    // viewmodel
    lateinit var viewModel : ViewModel

    // maps stuff
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var locationManager: LocationManager? = null

    // status
    var GpsStatus = false
    var ttsStatus = true

    // info
    var type = "o3"
    private var tts: TextToSpeech? = null //TODO fjern?


    private val callback = OnMapReadyCallback { Map ->
        mMap = Map
        tts = TextToSpeech(this.context, this) //TODO fjern?

        // starter med aa flytte kamera til Norge
        mMap.setPadding(0, 0, 0, 120)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(60.472024, 8.468946), 5.0f)) // flytter til Norge

        assignId(root)
        addMapFunctions()
        addMarkers()
        addOnClickers()
        addSwitchFunction()

        //TODO fjern! - dette er bare en fremvisning av hvordan hente Nearest og Nearby
        viewModel.stations.observe(viewLifecycleOwner, Observer { stasjoner ->
            viewModel.findNearestStation(fusedLocationClient, stasjoner, GpsStatus)
            viewModel.findNearbyStations(fusedLocationClient, stasjoner, GpsStatus)
            viewModel.nearest_station.observe(viewLifecycleOwner, Observer { nearest ->
                Log.d("Nearest", nearest.toString())
            })
            viewModel.nearby_stations.observe(viewLifecycleOwner, Observer { nearby ->
                Log.d("nearby", nearby.toString())
            })
        })


        //region [midlertidig] TODO: fjern?
        viewModel.stations.observe(viewLifecycleOwner, Observer { aq ->
            viewModel.niluStations.observe(viewLifecycleOwner, Observer { nilu ->
                var antLike = 0
                var antNilu = 0
                var antAq = 0
                val like: MutableList<Stasjon> = mutableListOf()
                val ulike: MutableList<Stasjon> = mutableListOf()

                for (y in nilu) antNilu++
                for (x in aq) antAq++

                for (x in aq) {
                    for (y in nilu) {
                        if (y.eoi == x.eoi) {
                            like.add(x)
                            antLike++
                        }
                        else ulike.add(y)
                    }
                }
                Log.d("Antall like stasjoner", antLike.toString())
                Log.d("Antall NILU", antNilu.toString())
                Log.d("Antall Aq", antAq.toString())
                Log.d("like", like.toString())
                Log.d("ulike", ulike.toString())
            })
        })
        //endregion
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root: View = inflater.inflate(R.layout.fragment_maps, container, false)
        this.root = root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ViewModel::class.java) // legger til viewmodel
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    fun assignId(root : View) {
        switch = root.findViewById(R.id.heatmap_Switch)
    }


    open fun CheckGpsStatus() {
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

    // legger til hver stasjon paa kartet
    fun addMarkers() {
        viewModel.stations.observe(viewLifecycleOwner, Observer { stations ->
            for (station in stations) {
                val highest : Map.Entry<String, Double>? = station.verdier.maxBy { it.value }
                val title = "[${station.name}] - ${highest?.value} ug/m3 [${highest?.key}]"
                val marker : MarkerOptions = MarkerOptions().position(LatLng(station.latitude, station.longitude)).title(title)
                mMap.addMarker(marker)
            }
        })

    }

    fun addHeatmap() {
        viewModel.stations.observe(viewLifecycleOwner, Observer { list ->
            val weightedData: MutableList<WeightedLatLng> = mutableListOf()

            // lager LatLng og WeightedLatLng av hver stasjon for heatmap
            for (station in list) {
                val highest : Map.Entry<String, Double>? = station.verdier.maxBy { it.value }
                val verdi = station.verdier[highest?.key]
                if (verdi != null) weightedData.add(WeightedLatLng(LatLng(station.latitude, station.longitude), verdi))
            }

            // lager selve heatmap og starter
            val mProvider = HeatmapTileProvider.Builder()
                .radius(50)
                .weightedData(weightedData)
                .build()

            val overlay = mMap.addTileOverlay(TileOverlayOptions().tileProvider(mProvider))

            // endrer heatmap naar kartet endres - bevegelser / zoom
            mMap.setOnCameraIdleListener {
                val newZoom = mMap.cameraPosition.zoom.toInt()
                mProvider.setRadius((10 + newZoom * 2)*4)

                if (newZoom in 10..20) { mProvider.setMaxIntensity(500.0)}
                if (newZoom in 9..9) { mProvider.setMaxIntensity(1000.0)}
                if (newZoom in 5..8) { mProvider.setMaxIntensity(2000.0)}
                if (newZoom in 0..4) { mProvider.setMaxIntensity(4000.0)}
            }
        })
    }

    // setter onClicker for infoWindow til hver markoer
    @SuppressLint("PotentialBehaviorOverride")
    fun addOnClickers() {
        mMap.setOnInfoWindowClickListener { marker ->
            viewModel.stations.observe(viewLifecycleOwner) { list ->

                for (stasjon in list) {
                    val navn = marker.title.substring(marker.title.indexOf("[") + 1, marker.title.indexOf("]"))

                    if (navn == stasjon.name) {
                        // tts-test
                        if (ttsStatus) tts!!.speak("Opening page $navn", TextToSpeech.QUEUE_FLUSH, null, "")

                        Toast.makeText(this.context, "Opening page ...", Toast.LENGTH_SHORT).show() // informerer bruker

                        // venter i (ca) 2 sec for endret (postDelayed for aa vente)
                        root.postDelayed({
                            val action =
                                MapsFragmentDirections.actionNavigationMapToNavigationLocation(stasjon)
                            root.findNavController().navigate(action)
                        }, 1500)

                        marker.showInfoWindow()
                    }
                }
            }
        }
    }

    fun addSwitchFunction() {
        switch.setOnCheckedChangeListener { buttonView, isChecked ->
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

    // KUN FOR TEST ATM !
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.US) // henter språk
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language not supported")
            }
        } else { Log.e("TTS", "Initialization failed") }
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
