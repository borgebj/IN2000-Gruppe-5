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
<<<<<<< Updated upstream
        //TODO: Implementer fremtids-onclickers
=======
        //infoknapp
        val infoButton : ImageButton = root.findViewById(R.id.info_home)
        infoButton.setOnClickListener{
            alertView(getString(R.string.str_info), root, "open")
        }
    }

    private fun slideShow(command : String, dialog : AlertDialog.Builder){
        val animasjonsDialog : AlertDialog = dialog.create()
        if (command == "open") animasjonsDialog.window?.attributes?.windowAnimations = R.style.DialogThOpen //animasjon
        else if (command == "close") animasjonsDialog.window?.attributes?.windowAnimations = R.style.DialogThClose //animasjon
        else if (command == "next") animasjonsDialog.window?.attributes?.windowAnimations = R.style.DialogThNext //animasjon
        else if (command == "nextNext") animasjonsDialog.window?.attributes?.windowAnimations = R.style.DialogThNext //animasjon
        else if (command == "back") animasjonsDialog.window?.attributes?.windowAnimations = R.style.DialogThBack
        return (animasjonsDialog.show())
    }

    //viser dialog/pop up vindu. brukes for infoknapper
    private fun alertView(message: String, root : View, command : String) {
        val dialogB = AlertDialog.Builder(context)
        dialogB.setTitle("Hva er AQI?")
            .setIcon(R.drawable.ic_info)
            .setMessage(message)
            .setPositiveButton("Lukk") {
                    dialoginterface,i -> }
            .setNeutralButton("les mer") {
                    dialog, which ->
                root.findNavController().navigate(R.id.action_navigation_dialog_to_AboutAirQualityFragment)}
            .setNegativeButton("se funfact") {
                dialog, which ->
                displayFunfacts(root)
            }
        slideShow(command, dialogB)
    }

    //viser fram fun facts etter man har trykket på infoknappen og "les funfacts"
    private fun displayFunfacts(root: View) {
        val facts = listOf(
            "Barn er mest sårbare for luftforurensning - men vi er alle berørt",
            "Fem dager inn i 2017 ble de årlige grensene for luftforurensning i London brutt",
            "De globale kostnadene for luftforurensning er 225 milliarder dollar årlig, ifølge Verdensbanken",
            "De minste partiklene er de farligste",
            "I 2019 var det kun to brudd på grense- og målsettingsverdiene for lokal luftkvalitet i Norge, begge knyttet til utslipp fra industrivirksomhet",
            "Fint svevestøv (PM2,5) kommer hovedsakelig fra langtransportert luftforurensning og fra lokal vedfyring.",
            "Lokalt er utslipp fra vedfyring den viktigste kilden til fint svevestøv (PM2,5, mens utslipp fra eksos kan være viktig i områdene med de høyeste nivåene ",
            "Norges befolkning tapte over 15 000 friske leveår i 2016 (tall fra FHI) på grunn av lokal luftforurensning.",
            "Over 1100 mennesker døde for tidlig i Norge som følge av lokal luftforurensning i 2016 (tall fra FHI).",
            "7. september er den internasjonale dagen for ren luft.",
            "Høye nivåer av luftforurensning kan føre til alvorlige skader og/eller forverring av sykdommer på lunger og hjerte- og karsystemet.",
            "Nivåene av forurrensning i Norge er som i andre skandinaviske land, men lavere enn i Sør-Europa.",
            "Nivåene av luftforurensning i Norge har det siste tiåret vært forholdsvis stabile. For svevestøv har det vært en svak nedgang.",
            "Luftforurensning er på nasjonalt nivå den fjerde største trusselen mot menneskers helse, bak høyt blodtrykk, dårlig kosthold og røyking (WHO).",
            "Det er estimert at luftforurensning dreper syv millioner mennesker verden over hvert år (WHO).",
            "90% av verdens befolning puster luft som overstiger WHOs anbefalinger om eksponering for forurenset luft. U-land har den høyeste eksponeringen (WHO).",
            "En tredjedel av dødsfallene fra hjerneslag, lungekreft og hjertesykdom skyldes luftforurensning (WHO).",
            "Å oppfylle målene i Paris-avtalen for å bekjempe klimaendringene kan, internasjonalt innen 2050, redde omtrent en million liv i året gjennom reduksjoner i luftforurensning alene (WHO).",
            "Luften kan være forurenset, selv om den ser ren og klar ut. Svevestøv kan gjøre luften tåkete på avstand.",
            "Noen typer luftbåren forurensning kan trenge seg inn i kroppen gjennom huden.",
            "Planter kan filtrere og rense forurenset luft")

        var factIndex = (0 until facts.size).random()
        val newDialog = AlertDialog.Builder(context)
        newDialog.setTitle("Funfact om AQI")
        newDialog.setIcon(R.drawable.ic_funfact)
        newDialog.setMessage(facts.get(factIndex))
            .setPositiveButton("Lukk") {
                    dialoginterface, i ->}
            .setNeutralButton("Neste funfact") {
                dialog, which ->
                    factIndex = (0 until facts.size).random()
                    newDialog.setMessage(facts.get(factIndex))
                    slideShow("next", newDialog)
            }
            .setNegativeButton("tilbake") {
                dialog, which ->
                    alertView(getString(R.string.str_info), root, "back")}
        //newDialog.show()
        slideShow("nextNext", newDialog)
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
>>>>>>> Stashed changes
    }
}