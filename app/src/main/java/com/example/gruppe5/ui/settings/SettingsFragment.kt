package com.example.gruppe5.ui.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.gruppe5.R


class SettingsFragment : Fragment() {

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
        setLocation()
        setAboutAirquality(root)
        setAboutApp(root)
        setAboutData(root)

        return root
    }

    fun assignId(root: View) {
        lokasjonKnapp = root.findViewById(R.id.location_button)
        informasjon = root.findViewById(R.id.informasjon)
        luftkvalitetKnapp = root.findViewById(R.id.about_airquality)
        appKnapp = root.findViewById(R.id.about_app)
        dataKnapp = root.findViewById(R.id.about_data)
    }


    private fun setLocation() {
        lokasjonKnapp.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    private fun setAboutAirquality(root: View) {
        luftkvalitetKnapp.setOnClickListener {
            root.findNavController()
                .navigate(R.id.action_navigation_settings_to_aboutAirqualityFragment)
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
