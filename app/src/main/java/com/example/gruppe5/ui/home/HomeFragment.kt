package com.example.gruppe5.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.gruppe5.R
import com.example.gruppe5.testFiler.MainTestActivity


class HomeFragment : Fragment() {

    // globale variabler
    private lateinit var homeModel: HomeViewModel
    lateinit var textView: TextView
    lateinit var testFilKnapp: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root: View = inflater.inflate(R.layout.fragment_home, container, false)

        assignId(root)
        setOnClickers(root)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        homeModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        homeModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
    }

    fun assignId(root: View) {
        textView = root.findViewById(R.id.text_home)
        testFilKnapp = root.findViewById(R.id.testFiler_knapp)
    }

    // setter onClickers for kart og API_test
    fun setOnClickers(root: View){

        // gaar til map-tester
        testFilKnapp.setOnClickListener {
            val map = Intent(root.context, MainTestActivity::class.java)
            startActivity(map)
        }
    }

}