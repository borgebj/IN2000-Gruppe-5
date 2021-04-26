package com.example.gruppe5.ui.location

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
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
import androidx.navigation.fragment.navArgs
import com.example.gruppe5.R
import com.example.gruppe5.testFiler.MainTestActivity
import com.example.gruppe5.Stasjon


class LocationFragment : Fragment() {

    private lateinit var viewModel : LocationViewModel

    lateinit var stasjonNavn : TextView
    lateinit var aqiLevel : TextView
    lateinit var aqiSentence : TextView
    lateinit var verdiNivaer : TextView

    lateinit var stasjon: Stasjon

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val root: View = inflater.inflate(R.layout.fragment_location, container, false)

        assignId(root)
        setOnClickers(root)

        val stasjon: Stasjon? = LocationFragmentArgs.fromBundle(requireArguments()).station
        if (stasjon != null) this.stasjon = stasjon

        //val kortNavn = navn.substring(navn.indexOf("[") + 1, navn.indexOf("]"))
        if (stasjon != null) {
            stasjonNavn.text = stasjon.name
            Log.d("mine verdier", stasjon.verdier.toString())
        }

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

    fun setOnClickers(root: View){
        val infoButton1 : ImageButton = root.findViewById(R.id.info1_location)
        infoButton1.setOnClickListener(){
            alertView(getString(R.string.str_info))
        }
        val infoButton2 : ImageButton = root.findViewById(R.id.info2_location)
        infoButton2.setOnClickListener(){
            alertValuesView(getString(R.string.str_info_values))
        }
    }

    //test for Location sin infoknapp om ulike nivåer. Gir en liste man kan velge i.
    private fun alertValuesView(message: String) {
        val dialog = AlertDialog.Builder(context)

        dialog.setTitle("AQI nivåer")
            .setIcon(R.drawable.ic_info)
            .setMessage(message)
            .setPositiveButton("Lukk") { dialoginterface, i -> }
            .setNeutralButton("les mer") { dialog, which -> openValueList() }
        dialog.show()
    }

    //kalles i metoden ovenfor. Åpner liste med ulike verdier, som man kan velge i for deretter å få en forklaring.
    private fun openValueList(){
        // setter opp alert builder
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Velg en type")

        val values = arrayOf("no2", "pm10", "pm25", "o3")
        builder.setItems(values) { dialog, which ->
            when (which) {
                0 -> {displayTypeFact("no2 er ikke bra for lungene")}           //disse utdypes senere
                1 -> {displayTypeFact("pm10 er ikke bra for lungene")}
                2 -> {displayTypeFact("pm25 er ikke bra for lungene")}
                3 -> {displayTypeFact("o3 er heller ikke bra for lungene")}
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    //for hver av typene i lista.
    private fun displayTypeFact(message: String){
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle(((message.split(" ".toRegex(), 2).toTypedArray())[0]))  //setter første ord som tittel
            .setIcon(R.drawable.ic_info)
            .setMessage(message)
            .setPositiveButton("Lukk") { dialoginterface, i -> }
            .setNeutralButton("Tilbake") { dialog, which -> openValueList() }
        dialog.show()
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

    fun setAqiInformer(root: View) {
        //skal finne hoyeste tallet bland aqiverdier, og sette det på homepage. farge og emoji skal endres etter verdien.
        var aqiValuesList = listOf(6, 250, 12, 36) //midlertidig aqi liste
        val highestIndex = aqiValuesList.maxOrNull() ?: 0
        val aqiLevel : TextView = root.findViewById(R.id.aqiLevel_location)
        val aqiSentence : TextView = root.findViewById(R.id.aqiSentence_location)
        val aqiSmiley : ImageView = root.findViewById(R.id.smiley_location)
        if (highestIndex < 50) { //bra verdi
            aqiLevel.setTextColor(Color.GREEN)
            aqiSentence.text = "AQI nivået er bra"
            aqiSmiley.setBackgroundResource(R.drawable.ic_smiley_lvl1)
        }
        else if (highestIndex > 50 && highestIndex < 100){
            aqiLevel.setTextColor(Color.YELLOW)
            aqiSentence.text = "AQI nivået er moderat"
            aqiSmiley.setBackgroundResource(R.drawable.ic_smiley_lvl2)
        }
        else if (highestIndex > 100 && highestIndex < 150){
            aqiLevel.setTextColor(Color.YELLOW) //endres til oransje
            aqiSentence.text = "AQI nivået er usunt for utsatte grupper"
            aqiSmiley.setBackgroundResource(R.drawable.ic_smiley_lvl3)
        }
        else if (highestIndex > 150 && highestIndex < 200){
            aqiLevel.setTextColor(Color.RED) //endres til oransje
            aqiSentence.text = "AQI nivået er usunt"
            aqiSmiley.setBackgroundResource(R.drawable.ic_smiley_lvl4)
        }
        else if (highestIndex > 200 && highestIndex < 300){
            aqiLevel.setTextColor(Color.RED) //endres til LILLA
            aqiSentence.text = "AQI nivået er veldig usunt"
            aqiSmiley.setBackgroundResource(R.drawable.ic_smiley_lvl5)
        }
        else if (highestIndex > 300){
            aqiLevel.setTextColor(Color.YELLOW) //endres til MAROON (?)
            aqiSentence.text = "AQI nivået er helseskadelig"
            aqiSmiley.setBackgroundResource(R.drawable.ic_smiley_lvl5)
        }
        aqiLevel.text = (highestIndex.toString() + " AQI")
    }
}