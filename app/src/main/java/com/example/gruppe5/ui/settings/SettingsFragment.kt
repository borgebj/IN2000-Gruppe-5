package com.example.gruppe5.ui.settings

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.gruppe5.R
import com.example.gruppe5.ui.favorites.FavoritesFragment


class SettingsFragment : Fragment() {

    private lateinit var settingsModel: SettingsViewModel

    private lateinit var lokasjonKnapp: Button
    private lateinit var informasjon: TextView
    private lateinit var luftkvalitetKnapp: Button
    private lateinit var appKnapp: Button
    private lateinit var dataKnapp: Button


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root: View = inflater.inflate(R.layout.fragment_settings, container, false)

        assignId(root)
        setCondition(root)
        setLocation(root)
        setAboutAirquality(root)
        setAboutApp(root)
        setAboutData(root)

        return root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        settingsModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        settingsModel.text.observe(viewLifecycleOwner, {
        })
    }

    fun assignId(root: View) {
        lokasjonKnapp = root.findViewById(R.id.location_button)
        informasjon = root.findViewById(R.id.informasjon)
        luftkvalitetKnapp = root.findViewById(R.id.about_airquality)
        appKnapp = root.findViewById(R.id.about_app)
        dataKnapp = root.findViewById(R.id.about_data)
    }


    private fun setLocation(root: View) {
        lokasjonKnapp.setOnClickListener{
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            setCondition(root)
        }
    }


    private fun setCondition(root: View) {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (location) {
            lokasjonKnapp.text = "På"
        } else {
            lokasjonKnapp.text = "Av"
        }
    }

    private fun refresh(root: View) {
        root.findNavController().navigate(R.id.navigation_settings)
    }

        /* FORSØK PÅ Å FJERNE POSISJONSKNAPPEN PÅ KARTET:

        val mMap : GoogleMap = root.findViewById(R.id.map)
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (location) {
            lokasjonKnapp.setText("På")
            mMap.uiSettings.isMyLocationButtonEnabled = true
            if (ActivityCompat.checkSelfPermission(
                    root.context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    root.context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            mMap.isMyLocationEnabled = true
        } else {
            lokasjonKnapp.setText("Av")
            mMap.uiSettings.isMyLocationButtonEnabled = false
            mMap.isMyLocationEnabled = false
        }
        */


    private fun setAboutAirquality(root: View) {
        luftkvalitetKnapp.setOnClickListener {
            root.findNavController().navigate(R.id.action_navigation_settings_to_aboutAirqualityFragment)
        }
    }

    private fun setAboutApp(root: View) {
        appKnapp.setOnClickListener {
            root.findNavController().navigate(R.id.action_navigation_settings_to_aboutAppFragment)
        }
    }

    private fun setAboutData(root: View) {
        dataKnapp.setOnClickListener {
            root.findNavController().navigate(R.id.action_navigation_settings_to_aboutDataFragment)
        }
    }
}
