package com.example.gruppe5.ui.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.gruppe5.R

class LocationFragment : Fragment() {

    private lateinit var viewModel : LocationViewModel

    lateinit var stasjonNavn : TextView
    lateinit var aqiLevel : TextView
    lateinit var aqiSentence : TextView
    lateinit var verdiNivaer : TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val root: View = inflater.inflate(R.layout.fragment_location, container, false)

        assignId(root)

        val navn = LocationFragmentArgs.fromBundle(requireArguments()).stationName
        val kortNavn = navn.substring(navn.indexOf("[") + 1, navn.indexOf("]"))
        stasjonNavn.text = kortNavn

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        viewModel.text.observe(viewLifecycleOwner, Observer {
            //stasjonNavn.text = it
        })
    }

    fun assignId(root: View) {
        stasjonNavn = root.findViewById(R.id.stationName_location)
        aqiLevel = root.findViewById(R.id.aqiLevel_location)
        aqiSentence = root.findViewById(R.id.aqiSentence_location)
        verdiNivaer = root.findViewById(R.id.verdiNivaer_location)
    }
}