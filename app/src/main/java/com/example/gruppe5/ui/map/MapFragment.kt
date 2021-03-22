package com.example.gruppe5.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.gruppe5.R

class MapFragment : Fragment() {

    // globale variabler
    private lateinit var mapModel: MapViewModel
    lateinit var textView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root: View = inflater.inflate(R.layout.fragment_map, container, false)

        assignId(root)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mapModel = ViewModelProvider(this).get(MapViewModel::class.java)
        mapModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
    }

    fun assignId(root: View) {
        textView = root.findViewById(R.id.text_map)
    }


}