package com.example.gruppe5.ui.home

import android.R.array
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Color.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import app.futured.donut.DonutProgressView
import app.futured.donut.DonutSection
import com.example.gruppe5.R
import java.util.*
import kotlin.math.max


class HomeFragment : Fragment(){

    // globale variabler
    private lateinit var homeModel: HomeViewModel
    lateinit var donutView: DonutProgressView
    lateinit var textView: TextView
    lateinit var aqiLevel : TextView
    lateinit var aqiSentence : TextView
    lateinit var aqiSmiley : ImageView


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
        setAqiInformer(root)
        test2(root)

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
        aqiLevel = root.findViewById(R.id.aqiLvlHome)
        aqiSentence = root.findViewById(R.id.aqiSentence_home)
        aqiSmiley = root.findViewById(R.id.smiley_home)
    }

    // setter onClickers for kart og API_test
    fun setOnClickers(root: View){
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

    //TODO bruk av LiveData fra ViewModel for aa hente stasjoner, og lokasjon for aa hente naermeste stasjon (begge disse blir gjort i MapFragment ;)
    @SuppressLint("ResourceAsColor")
    fun setAqiInformer(root: View) {
        //skal finne hoyeste tallet bland aqiverdier, og sette det på homepage. farge og emoji skal endres etter verdien.
        var aqiValuesList = listOf(6, 250, 12, 36) //midlertidig aqi liste
        val highestIndex = aqiValuesList.maxOrNull() ?: 0

        if (highestIndex < 50) { //bra verdi
            aqiLevel.setTextColor(GREEN)
            aqiSentence.text = "AQI nivået er bra"
            aqiSmiley.setBackgroundResource(R.drawable.ic_smiley_lvl1)
        }
        else if (highestIndex > 50 && highestIndex < 100){
            aqiLevel.setTextColor(YELLOW)
            aqiSentence.text = "AQI nivået er moderat"
            aqiSmiley.setBackgroundResource(R.drawable.ic_smiley_lvl2)
        }
        else if (highestIndex > 100 && highestIndex < 150){
            aqiLevel.setTextColor(YELLOW) //endres til oransje
            aqiSentence.text = "AQI nivået er usunt for utsatte grupper"
            aqiSmiley.setBackgroundResource(R.drawable.ic_smiley_lvl3)
        }
        else if (highestIndex > 150 && highestIndex < 200){
            aqiLevel.setTextColor(RED) //endres til oransje
            aqiSentence.text = "AQI nivået er usunt"
            aqiSmiley.setBackgroundResource(R.drawable.ic_smiley_lvl4)
        }
        else if (highestIndex > 200 && highestIndex < 300){
            aqiLevel.setTextColor(RED) //endres til LILLA
            aqiSentence.text = "AQI nivået er veldig usunt"
            aqiSmiley.setBackgroundResource(R.drawable.ic_smiley_lvl5)
        }
        else if (highestIndex > 300){
            aqiLevel.setTextColor(YELLOW) //endres til MAROON (?)
            aqiSentence.text = "AQI nivået er helseskadelig"
            aqiSmiley.setBackgroundResource(R.drawable.ic_smiley_lvl5)
        }
        aqiLevel.text = (highestIndex.toString() + " AQI")
    }

    @SuppressLint("ResourceAsColor")
    fun test2(root: View) {
        val map = mapOf<String, Double>("no2" to 6.00, "pm10" to 250.00, "pm25" to 12.00, "o3" to 36.00)
        val highest : Map.Entry<String, Double>? = map.maxBy { it.value }

        if (highest != null)
        when (highest.key) {
            "no2" -> {
                if (highest.value <= 100.0) {

                } else if (highest.value in 100.0..200.0) {

                } else if (highest.value in 200.0..400.0) {

                } else if (highest.value >= 400.0) { }
            }
            "pm10" -> {
                if (highest.value <= 60.0) {

                } else if (highest.value in 60.0..120.0) {

                } else if (highest.value in 120.0..400.0) {

                } else if (highest.value >= 400.0) { }
            }
            "pm25" -> {
                if (highest.value <= 30.0) {

                } else if (highest.value in 30.0..50.0) {

                } else if (highest.value in 50.0..150.0) {

                } else if (highest.value >= 150.0) { }
            }
            "o3" -> {
                if (highest.value <= 100.0) {

                } else if (highest.value in 100.0..180.0) {

                } else if (highest.value in 180.0..240.0) {

                } else if (highest.value >= 240.0) { }
            }
        }
    }
}