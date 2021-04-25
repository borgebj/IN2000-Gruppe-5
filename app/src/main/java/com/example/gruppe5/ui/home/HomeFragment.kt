package com.example.gruppe5.ui.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Color.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import app.futured.donut.DonutProgressView
import app.futured.donut.DonutSection
import com.example.gruppe5.R


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
        setAqiInformer(root)

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
        //infoknapp
        val infoButton : ImageButton = root.findViewById(R.id.info_home)
        infoButton.setOnClickListener{
            alertView(getString(R.string.str_info), root)
        }

        //Test for Location, da slipper jeg å accesse Locations gjennom mappet hele tiden
        val locButton : ImageButton = root.findViewById(R.id.iconLocation_home)
        locButton.setOnClickListener{
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
    private fun alertView(message: String, root : View) {
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle("Hva er AQI?")
            .setIcon(R.drawable.ic_info)
            .setMessage(message)
            .setPositiveButton("Lukk") { dialoginterface, i -> }
            .setNeutralButton("les mer") { dialog, which -> openInfo(root)}
            .setNegativeButton("se funfact") { dialog, which -> displayFunfacts(root)}
        dialog.show()
    }

    //sender brukern til AboutAirQualityFragment når det trykkes "les mer"
    private fun openInfo(root : View){
        root.findNavController().navigate(R.id.action_navigation_dialog_to_AboutAirQualityFragment)
    }

    //viser fram fun facts etter man har trykket på infoknappen og "les funfacts"
    private fun displayFunfacts(root: View) {
        val facts = listOf(
            "Luftforurensning er en av Storbritannias (og verdens) største drapsmenn",
            "Barn er mest sårbare for luftforurensning - men vi er alle berørt",
            "Et barn født i dag puster kanskje ikke ren luft før de er 8 år",
            "Luftforurensning forårsaker opptil 36 000 tidlige dødsfall i året i Storbritannia",
            "Fem dager inn i 2017 ble de årlige grensene for luftforurensning i London brutt",
            "De globale kostnadene for luftforurensning er 225 milliarder dollar årlig, ifølge Verdensbanken",
            "De minste partiklene er de farligste",
            "Planter kan filtrere forurensning")

        var factIndex = (0 until facts.size).random()
        val newDialog = AlertDialog.Builder(context)
        newDialog.setTitle("Funfacts om AQI")
        newDialog.setIcon(R.drawable.ic_funfact)
        newDialog.setMessage(facts.get(factIndex))
            .setPositiveButton("Lukk", { dialoginterface, i -> })
            .setNeutralButton("Neste funfact") { dialog, which ->
                    factIndex = (0 until facts.size).random()
                newDialog.setMessage(facts.get(factIndex))
                newDialog.show()
            }
            .setNegativeButton("tilbake") { dialog, which -> alertView(getString(R.string.str_info), root)}
        newDialog.show()
    }

    //endrer dataen på hjemskjermen etter faktiske verdinivåer
@SuppressLint("ResourceAsColor")
    fun setAqiInformer(root: View) {
        //skal finne hoyeste tallet bland aqiverdier, og sette det på homepage. farge og emoji skal endres etter verdien.
        var aqiValuesList = listOf(6, 250, 12, 36) //midlertidig aqi liste
        val highestIndex = aqiValuesList.maxOrNull() ?: 0
        val aqiLevel : TextView = root.findViewById(R.id.aqiLvlHome)
        val aqiSentence : TextView = root.findViewById(R.id.aqiSentence_home)
        val aqiSmiley : ImageView = root.findViewById(R.id.smiley_home)
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
}