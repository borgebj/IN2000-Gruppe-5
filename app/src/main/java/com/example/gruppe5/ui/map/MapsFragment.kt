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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*


// SKAL INNEHOLDE UI/kode som endrer viewet

class MapsFragment : Fragment(), TextToSpeech.OnInitListener {

    // elementer
    lateinit var mMap: GoogleMap
    lateinit var root : View

    // viewmodel
    lateinit var viewModel : MapViewModel

    // maps stuff
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var GpsStatus = false
    var ttsStatus = true
    var locationManager: LocationManager? = null


    // info
    var type = "pm10"
    private var tts: TextToSpeech? = null


    private val callback = OnMapReadyCallback { Map ->
        mMap = Map
        tts = TextToSpeech(this.context, this) // oppretter TTS

        // starter med aa flytte kamera til Norge
        mMap.setPadding(0, 0, 0, 120)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(60.472024, 8.468946), 5.0f)) // flytter til Norge

        addMapFunctions()
        addOnClickers()
        viewModel.parseData()

        // henter livedata fra viewmodel
        viewModel.stations.observe(viewLifecycleOwner, Observer {
            addMarkers(it)
            val nearby: MutableList<Stasjon>? = getNearbyStations(it)
            val nearest: Stasjon? = getNearbyStation(it)

            Log.d("nearby stations", nearby.toString())
            Log.d("nearest station", nearest.toString())
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root: View = inflater.inflate(R.layout.fragment_maps, container, false)
        this.root = root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(MapViewModel::class.java) // legger til viewmodel
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    @SuppressLint("MissingPermission")
    fun getNearbyStation(stations: MutableList<Stasjon>) : Stasjon? {
        var nearest : Stasjon? = null
        var closest: Float = 100000.00F

        if (GpsStatus) {
            val me : Location
            fusedLocationClient.lastLocation.addOnSuccessListener {
                for (stasjon in stations) {

                    // oppretter Location-objekter
                    val myLocation = Location("")
                    myLocation.latitude = it.latitude
                    myLocation.longitude = it.longitude

                    val stationLocation = Location("")
                    stationLocation.latitude = stasjon.latitude
                    stationLocation.longitude = stasjon.longitude

                    // sammenligner avstand mellom "her" og markoer, og sjekker hvem er naermest
                    val distance = myLocation.distanceTo(stationLocation)
                    if (distance <= closest) {
                        closest = distance
                        nearest = stasjon
                    }
                }
            }; return nearest //TODO: Fungerer ikke pga async-task (?)
        } else return null
    }



    @SuppressLint("MissingPermission")
    fun getNearbyStations(stations: MutableList<Stasjon>) : MutableList<Stasjon>? {
        val nearby: MutableList<Stasjon>? = null

        if (GpsStatus) {
            fusedLocationClient.lastLocation.addOnSuccessListener {

                for (stasjon in stations) {
                    val myCoordinates = LatLng(it.latitude, it.longitude)
                    val stationCoordiantes = LatLng(stasjon.latitude, stasjon.longitude)

                    // henter stasjoner innen en 10km radius (ca, ish 11.1 km)
                    if (stationCoordiantes.latitude <= myCoordinates.latitude+0.1 && stationCoordiantes.latitude >= myCoordinates.latitude-0.1){
                        if (stationCoordiantes.longitude <= myCoordinates.longitude+0.1 && stationCoordiantes.longitude >= myCoordinates.longitude-0.1) {
                            nearby?.add(stasjon)
                        }
                    }
                }
            }; return nearby //TODO: Fungerer ikke pga async-task (?)
        } else return null
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

    // setter onClicker for infoWindow til hver markoer
    fun addOnClickers() {
        mMap.setOnInfoWindowClickListener {
            // tts-test
            val string = it.title.substring(it.title.indexOf("[") + 1, it.title.indexOf("]"))

            if (ttsStatus) tts!!.speak("Opening page $string", TextToSpeech.QUEUE_FLUSH, null, "")

            Toast.makeText(this.context, "Opening page ...", Toast.LENGTH_SHORT).show() // informerer bruker

            // venter i 2 sec for endret (postDelayed for aa vente)
            root.postDelayed({
                val action = MapsFragmentDirections.actionNavigationMapToNavigationLocation(it.title)
                root.findNavController().navigate(action)
            }, 1500)

            it.showInfoWindow()
        }
    }

    // legger til hver stasjon paa kartet
    fun addMarkers(stations: MutableList<Stasjon>) {
        for (station in stations) {
            val title = "[${station.name}] - ${station.verdier.get(type)} ug/m3"
            mMap.addMarker(
                MarkerOptions().position(LatLng(station.latitude, station.longitude))
                    .title(title)
            )
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
