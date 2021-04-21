package com.example.gruppe5.ui.location

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.gruppe5.R
import com.example.gruppe5.testFiler.MainTestActivity

class LocationFragment : Fragment() {

    private lateinit var viewModel : LocationViewModel

    lateinit var stasjonNavn : TextView
    lateinit var aqiLevel : TextView
    lateinit var aqiSentence : TextView
    lateinit var verdiNivaer : TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val root: View = inflater.inflate(R.layout.fragment_location, container, false)

        assignId(root)
        setOnClickers(root)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        viewModel.text.observe(viewLifecycleOwner, Observer {
            stasjonNavn.text = it
        })
    }

    fun assignId(root: View) {
        stasjonNavn = root.findViewById(R.id.stationName_location)
        aqiLevel = root.findViewById(R.id.aqiLevel_location)
        aqiSentence = root.findViewById(R.id.aqiSentence_location)
        verdiNivaer = root.findViewById(R.id.verdiNivaer_location)
    }

    fun setOnClickers(root: View){
        val infoButton1 : ImageButton = root.findViewById(R.id.info1_location)
        infoButton1.setOnClickListener(){
            alertView(getString(R.string.str_info))
        }
        val infoButton2 : ImageButton = root.findViewById(R.id.info2_location)
        infoButton2.setOnClickListener(){
            alertView(getString(R.string.str_info_values))
        }
    }

    //viser dialog/pop up vindu. brukes for infoknapper
    private fun alertView(message: String) {
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle("Hva er AQI?")
            .setIcon(R.drawable.ic_info)
            .setMessage(message)
            .setPositiveButton("Lukk",
                { dialoginterface, i -> }).show()
    }
}