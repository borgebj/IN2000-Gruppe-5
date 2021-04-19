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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.gruppe5.R
import com.example.gruppe5.Stasjon
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


// SKAL INNEHOLDE UI/kode som endrer viewet

class MapsFragment : Fragment() {

    // elementer
    lateinit var mMap: GoogleMap
    lateinit var root : View

    // viewmodel
    lateinit var viewModel : MapViewModel

    // info
    var locationManager: LocationManager? = null
    var GpsStatus = false
    var type = "pm10"


    private val callback = OnMapReadyCallback { Map ->
        mMap = Map

        // starter med aa flytte kamera til Norge
        mMap.setPadding(0, 0, 0, 120)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(60.472024, 8.468946), 5.0f)) // flytter til Norge

        addMapFunctions()
        addOnClickers()
        viewModel.parseData()

        Log.d("test", "en")
        viewModel.stations.observe(viewLifecycleOwner, Observer {
            Log.d("test to ->", it.toString())
            addMarkers(it)
        })
    }
  
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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

    fun addOnClickers() {
        mMap.setOnInfoWindowClickListener {
            Toast.makeText(this.context, "Opening page ...", Toast.LENGTH_SHORT).show() // informerer bruker
            it.title = "trykket"

            // venter i 2 sec for endret (postDelayed for aa vente)
            root.postDelayed({
                val action = MapsFragmentDirections.actionNavigationMapToNavigationLocation()
                root.findNavController().navigate(action)
            }, 1500)

            it.showInfoWindow()
        }
    }

    fun addMarkers(stations : MutableList<Stasjon>) {
        for (station in stations) {
            val title = "[${station.name}] - ${station.verdier.get(type)} ug/m3"
            mMap.addMarker(
                MarkerOptions().position(LatLng(station.latitude, station.longitude))
                    .title(title)
            )
        }
    }
}
