package com.example.gruppe5.ui.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import app.futured.donut.DonutProgressView
import app.futured.donut.DonutSection
import com.example.gruppe5.R
import com.example.gruppe5.testFiler.MainTestActivity
import org.w3c.dom.Text
import java.util.*


class HomeFragment : Fragment(){

    // globale variabler
    private lateinit var homeModel: HomeViewModel
    lateinit var donutView: DonutProgressView
    lateinit var textView: TextView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root: View = inflater.inflate(R.layout.fragment_home, container, false)
        val section1 = DonutSection(
            name = "normal_pollution",
            color = Color.parseColor("#FF46E33B"),
            amount = 60f
        )
        val section2 = DonutSection(
            name = "warning_pollution",
            color = Color.parseColor("#FFDDE33B"),
            amount = 20f
        )
        val section3 = DonutSection(
            name = "dangerous_pollution",
            color = Color.parseColor("#FFE33B3B"),
            amount = 10f
        )
        assignId(root)
        setOnClickers(root)

        donutView.cap = 100f
        donutView.submitData(listOf(section1,section2,section3))


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
        donutView = root.findViewById(R.id.donut_view)
    }

    // setter onClickers for kart og API_test
    fun setOnClickers(root: View){
        //TODO: Implementer fremtids-onclickers
    }
}