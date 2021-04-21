package com.example.gruppe5.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import app.futured.donut.DonutProgressView
import app.futured.donut.DonutSection
import com.example.gruppe5.R
import com.example.gruppe5.testFiler.MainTestActivity


class HomeFragment : Fragment() {

    // globale variabler
    private lateinit var homeModel: HomeViewModel
    lateinit var donutView: DonutProgressView
    lateinit var textView: TextView
    lateinit var testFilKnapp: Button

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
        testFilKnapp = root.findViewById(R.id.testFiler_knapp)
        donutView = root.findViewById(R.id.donut_view)
    }

    // setter onClickers for kart og API_test
    fun setOnClickers(root: View){

//         gaar til map-tester
        testFilKnapp.setOnClickListener {
            val map = Intent(root.context, MainTestActivity::class.java)
            startActivity(map)
        }
        //infoknapp
        val infoButton : ImageButton = root.findViewById(R.id.info_home)
        infoButton.setOnClickListener(){
            alertView(getString(R.string.str_info))
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