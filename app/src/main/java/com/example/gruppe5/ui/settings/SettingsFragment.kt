package com.example.gruppe5.ui.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.gruppe5.R
import com.example.gruppe5.testFiler.MainTestActivity
import com.example.gruppe5.ui.favorites.FavoritesFragmentDirections


class SettingsFragment : Fragment() {


    private lateinit var settingsModel: SettingsViewModel
    private lateinit var innstillinger: TextView

    private lateinit var lokasjon: Button

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
        setLokasjon()
        setOmLuftkvalitet(root)

        return root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        settingsModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        settingsModel.text.observe(viewLifecycleOwner, {
        })
    }

    fun assignId(root: View) {
        innstillinger = root.findViewById(R.id.innstillinger)
        lokasjon = root.findViewById(R.id.location_button)
        informasjon = root.findViewById(R.id.informasjon)
        luftkvalitetKnapp = root.findViewById(R.id.button1)
        appKnapp = root.findViewById(R.id.button2)
        dataKnapp = root.findViewById(R.id.button3)

    }

    fun setLokasjon() {
        lokasjon.setOnClickListener{

        }
    }

    fun setOmLuftkvalitet(root: View) {
        luftkvalitetKnapp.setOnClickListener() {
            //root.findNavController().navigate(R.id.navigation_luftkvalitet)
        }
    }
}
