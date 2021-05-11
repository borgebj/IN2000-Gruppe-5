package com.example.gruppe5.ui.location

//bibloteker som hører til søylediagrammet og sirkel/donut diagrammet
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import app.futured.donut.DonutProgressView
import app.futured.donut.DonutSection
import com.example.gruppe5.R
import com.example.gruppe5.Stasjon
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.lang.NullPointerException
import com.example.gruppe5.ui.location.LocationFragmentArgs.Companion as LocationFragmentArgs1

class LocationFragment : Fragment() {

    private lateinit var viewModel : LocationViewModel
    private lateinit var stasjonNavn : TextView
    private lateinit var aqiLevel : TextView                         //aqi = air quality index, sier noe om luftkvaliteten
    private lateinit var aqiSentence : TextView
    private lateinit var verdiNivaer : TextView                      //overskrift for horisontalt søylediagram
    lateinit var aqiType : TextView                                  // textview som sier hvilken type som er høyest
    lateinit var numberMax : TextView
    private lateinit var aqiSmiley : ImageView
    private lateinit var donutView : DonutProgressView
    private lateinit var stasjon: Stasjon                            //Fragmentets stasjon
    private lateinit var barDataSet : BarDataSet
    private lateinit var HorBarChart : HorizontalBarChart
    private var pm10Percentage : Float = 0.0f                //viser helserisikoen til forurensningstypene i %
    private var pm25Percentage : Float = 0.0f
    private var no2Percentage : Float = 0.0f
    private var o3percentage : Float = 0.0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        val root: View = inflater.inflate(R.layout.fragment_location, container, false)
        val stasjon: Stasjon? = LocationFragmentArgs1.fromBundle(requireArguments()).station
        assignId(root)
        setOnClickers(root)

        //henter stasjon fra map fragmentet, og setter den til instansvariabelen.
        if (stasjon != null) {
            this.stasjon = stasjon
            setAqiInformer(stasjon.verdier)
            stasjonNavn.text = stasjon.name
        }
        //henter stasjon fra favorite fragmentet, og setter den til instansvariabelen.
        else {
            val args = arguments
            if (args != null) {
                try {
                    val myStasjon: Stasjon? = args.getParcelable("location") as Stasjon?
                    this.stasjon = myStasjon!!
                    setAqiInformer(myStasjon.verdier)
                    stasjonNavn.text = myStasjon.name
                } catch (e: NullPointerException) {e.printStackTrace()}
            }
            else Log.d("bundle == null", "HER")
        }
        setChart() // oppretter søylediagrammet
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
    }

    fun assignId(root: View) {
        stasjonNavn = root.findViewById(R.id.stationName_location)
        aqiLevel = root.findViewById(R.id.aqiLevel_location)
        aqiSentence = root.findViewById(R.id.aqiSentence_location)
        verdiNivaer = root.findViewById(R.id.verdiNivaer_location)
        aqiType = root.findViewById(R.id.aqiTypeLocation)
        numberMax = root.findViewById(R.id.donutNmbrsLocationMax)
        aqiSmiley = root.findViewById(R.id.smiley_location)
        donutView = root.findViewById(R.id.donut_view_location)
        HorBarChart = root.findViewById(R.id.hor_bar_chart)
    }

    //setter oppa aksene og andre nødvendige detaljer for det horisontale bar chartet
    private fun setChart() {
        val description = Description()                 //lager description som skal gjøre diagrammet enklere å lese.
        description.text = "Verdiene vises i %"
        description.textSize = 15f
        HorBarChart.description = description
        HorBarChart.legend.isEnabled = false            //Default er true
        HorBarChart.setPinchZoom(false)                 //ønsker ikke at chartet skal være interaktivt.
        HorBarChart.setDrawValueAboveBar(true)          //gjør at søylenes verdi havner etter, og ikke på søyla. Ved lave verdier (under 10) ville dette ført til at teksten kræsjet med labelsene på Y aksen.

        //setter aksen på venstre side med labels: pm25, pm10 osv..
        val xAxis = HorBarChart.xAxis
        xAxis.setDrawGridLines(false)                   //fjerner rutenettet bak
        xAxis.position = XAxis.XAxisPosition.BOTTOM   //setter labelsene på venstre side av søylene
        xAxis.labelCount = 4                            //setter antallet til 4, siden vi har 4 verdier
        xAxis.isEnabled = true
        xAxis.textSize = 15f
        
        //setter minimum og maximum lengde for verdiene parene representerer. Siden verdier skal vises i prosent, går det til 100
        val yLeft = HorBarChart.axisLeft
        yLeft.axisMaximum = 100f
        yLeft.axisMinimum = 0f
        yLeft.isEnabled = true                          //Var på false før, ser ikke forskjell
        yLeft.labelCount = 5                            //5 deler, så hver "bolk" er 20%

        //Legger til labels som legges til på den vertikale aksen til venstre
        val values = arrayOf("PM10", "PM2.5", "NO2", "O3")
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return values[value.toInt()]
            }
        }

        val yRight = HorBarChart.axisRight
        yRight.isEnabled = false                        //hindrer at tekst kommer på nederste aksen
        setGraphData()                                  //egen metode. setter plasseringen, og annen formattering
        HorBarChart.animateY(2000)         //animasjon for når søylene "bygges"
    }

    //setter deler av designet og selve dataen.
    private fun setGraphData() {
        setValues()
        HorBarChart.setDrawBarShadow(true)              //setter skygge som viser hvor langt søylen kan gå.//egen metode,
        barDataSet.setDrawValues(true)                  //setter søyleverdien i tekst
        barDataSet.valueTextSize = 13f                  //endrer tekststørrelsen til teksten oppå søylene
        barDataSet.barBorderColor = R.color.black       //setter rammefarge på søylene
        barDataSet.barShadowColor = Color.argb(40, 150, 150, 150)   //gråfarge
        val data = BarData(barDataSet)                  //oppretter BarData objekt med bardatasettet som parameter.
        data.barWidth = 0.5f                            //setter bredden på søylene  OBS: for å øke mellomrommet mellom søylene, sett verdien til barwidth til <1f
        HorBarChart.data = data                         //avslutningsvis: sett dataen og refresh grafen
        HorBarChart.invalidate()
    }

    //metoden returnerer et dangerLevel fra 1-4(4=farligst), ved å regne ut. setter samtidig hver enkelte globale prosentvariabel.
    private fun calculateDangerLevel(liveValue: Double?, polType: String) : Int{
        //initaliserer verdier som brukes til å kalkulere prosent, og hvor farlig prosenten er for den gitte typen.
        var topValue = 0                         //verdien viser max eksponering av ug/m3 i timesmiddel før det blir svært alvorlig. Kan hete maxvalue, men blir misvisende siden verdien kan overstige nivået
        val percentage : Float                   //prosenten for anbefalt eksponering
        var dangerLimit1 = 0                     //hvor mange prosent som må til for å være "lite farlig", "moderat", "alvorlig" og "svært alvorlig".
        var dangerLimit2 = 0
        val dangerLimit3  = 100                  //farenivå "alvorlig" vil alltid være 100%. Farenivået over vil alltid være over 100%. Det er altså kun prosentene for lite farlig og moderat som er relevant her, siden det er de som vil variere.
        var dangerLevel = 0                      //settes til et tall fra 1 - 4, som forteller hvor farlig nivået er (4 er farligst). Returneres

        //setter verdiene som er initalisert, til sin type. Hver type har forskjellige prosentmessige grenseverdier.
        //setter dangerLevelet, ved å bruke variablene som er deklarert til å regne ut
        when (polType) {
            "pm10" -> { topValue = 400; dangerLimit1 = 15; dangerLimit2 = 30; }
            "pm25" -> { topValue = 150; dangerLimit1 = 20; dangerLimit2 = 33 }
            "no2" -> { topValue = 400; dangerLimit1 = 25; dangerLimit2 = 50 }
            "o3" -> { topValue = 240; dangerLimit1 = 41; dangerLimit2 = 75 }
            //prosenten regnes ut med verdiene som settes ovenfor, og settes for hver av typene.
        }

        //prosenten regnes ut med verdiene som settes ovenfor, og settes for hver av typene.
        if (liveValue != null) {
            percentage = (liveValue.toFloat() / topValue.toFloat()) * 100f
            when (polType) {
                "pm10" -> pm10Percentage = percentage
                "pm25" -> pm25Percentage = percentage
                "no2" -> no2Percentage = percentage
                "o3" -> o3percentage = percentage
            }

            //setter dangerLevelet, ved å bruke variablene som er deklarert til å regne ut
            if (percentage < dangerLimit1) dangerLevel = 1
            else if (percentage > dangerLimit1 && percentage < dangerLimit2) dangerLevel = 2
            else if (percentage > dangerLimit2 && percentage < dangerLimit3) dangerLevel = 3
            else if (percentage > dangerLimit3) dangerLevel = 4
        }
        return dangerLevel
    }

    //metoden tar inn dangerLevelet fra 1-4, og returnerer fargen, som settes i setValues.
    private fun setColor(dangerLevel : Int): Int{
        var color = R.color.black
        when (dangerLevel) {
            1 -> { color = R.color.green }
            2 -> { color = R.color.yellow }
            3 -> { color = R.color.red}
            4 -> { color = R.color.purple_700}
        }
        return color
    }

    //metoden setter opp søylenes nivå, og farger.
    private fun setValues(){
        //egen metode kalles, slik at prosenten og dangertlevelet for hver type settes. Dangerlevelet lagres i variabelen, som sendes med som parameter når fargene settes i bunnen av denne metoden.
        val pm10lvl = calculateDangerLevel(stasjon.verdier["pm10"], "pm10")
        val pm25lvl = calculateDangerLevel(stasjon.verdier["pm25"], "pm25")
        val no2lvl = calculateDangerLevel(stasjon.verdier["no2"], "no2")
        val o3lvl = calculateDangerLevel(stasjon.verdier["o3"], "o3")

        //her settes verdiene fra apiet i søylene.
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, pm10Percentage))
        entries.add(BarEntry(1f, pm25Percentage))
        entries.add(BarEntry(2f, no2Percentage))
        entries.add(BarEntry(3f, o3percentage))

        //initialiserer en instans av barDataSet, for å kunne vise dataen i barchartet
        barDataSet = BarDataSet(entries, "Bar Data Set")

        //setter fargene med variablene som lages med metodekall
        barDataSet.setColors(
            ContextCompat.getColor(HorBarChart.context, setColor(pm10lvl)),    //pm10lvl
            ContextCompat.getColor(HorBarChart.context, setColor(pm25lvl)),    //pm25lvl
            ContextCompat.getColor(HorBarChart.context, setColor(no2lvl)),     //no2lvl
            ContextCompat.getColor(HorBarChart.context, setColor(o3lvl))      //o3lvl
        )
    }

    //setter onclicklisteners for infoknappene. åpner hver sine dialoger, og sender med tekst fra Strings, kommando for animasjon og root
    private fun setOnClickers(root: View){
        val infoButton1 : ImageButton = root.findViewById(R.id.info1_location)
        infoButton1.setOnClickListener {
            alertView(getString(R.string.str_info), root, "open")
        }
        val infoButton2 : ImageButton = root.findViewById(R.id.info2_location)
        infoButton2.setOnClickListener{
            alertValuesView(getString(R.string.str_info_values), "open", root)
        }
    }

    //brukes for animerte overganger i dialog/pop up boksene.
    private fun slideShow(command : String, dialog : AlertDialog.Builder){
        val animasjonsDialog : AlertDialog = dialog.create()
        when (command) {
            "open" -> animasjonsDialog.window?.attributes?.windowAnimations = R.style.DialogThOpen
            "close" -> animasjonsDialog.window?.attributes?.windowAnimations = R.style.DialogThClose
            "next" -> animasjonsDialog.window?.attributes?.windowAnimations = R.style.DialogThNext
            "nextNext" -> animasjonsDialog.window?.attributes?.windowAnimations = R.style.DialogThNext
            "back" -> animasjonsDialog.window?.attributes?.windowAnimations = R.style.DialogThBack
        }
        return (animasjonsDialog.show())
    }

    //dialog/pop-up vinduet til nederste infoknapp om de ulike nivåene.
    private fun alertValuesView(message: String, command : String, root : View) {
        val dialog = AlertDialog.Builder(context)                                                       //setter opp alert builder
        dialog.setTitle("Hvordan måles luftkvalitet?")
            .setIcon(R.drawable.ic_info)
            .setMessage(message)
            .setPositiveButton("Lukk") { _, _ -> }                                              //lukker vinduet. _ brukes for å hoppe over variabler som ikke brukes.
            .setNeutralButton("les mer") { _, _ -> openValueList("next", root) }     //kaller metoden nedenfor.
            .setNegativeButton("Se risikogrenser") {_, _ -> displayTable(root) }
        slideShow(command, dialog)
    }

    private fun displayTable(root : View){
        val image = ImageView(context)
        image.setImageResource(R.drawable.health_risk_table)
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            .setView(image)
            .setNeutralButton("Tilbake") { _, _ -> alertValuesView(getString(R.string.str_info_values),"back", root)}
            .setPositiveButton("Lukk") {_, _ -> }
        //builder.create()
        slideShow("next", builder)
    }


    //kalles i metoden ovenfor. Åpner liste med ulike verdier, som man kan velge i for deretter å få en forklaring. åpnes når det trykkes "les mer".
    private fun openValueList(command : String, root : View){
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle("Velg en type")
            .setNeutralButton("Tilbake") { _, _ -> alertValuesView(getString(R.string.str_info_values), "back", root)}  //tilbake-knapp
        //verdiene settes inn i lista i dialogen. ved trykk på de ulike, kalles egen metode: "DisplayTypeFact". Denne åpner et nytt dialogvindu, med fakta for den valgte typen.
        val values = stasjon.verdier.keys.toTypedArray()
        dialog.setItems(values) { _, which ->
            when (which) {
                0 -> {displayTypeFact("O3 (Ozon) er en reaktiv gass som finnes både nær bakken og høyere opp i atmosfæren. Høye konsentrasjoner av bakkenært ozon i Norge skyldes hovedsakelig langtransportert ozon fra Europa. Ozon frigjøres ikke fra en primær kilde, men dannes via en rekke komplekse reaksjoner i luften. Konsentrasjonen av ozon er noe høyere utenfor byene enn i byene. Ozonkonsentrasjonen i Norge har episodevis nådd nivåer opp mot 160 μg/m3. Studier har vist at astmatiske barn kan få luftveissymptomer ved akutt eksponering for ozon fra 100 til 120 μg/m3. Ozon kan gi betennelse og føre til skader i luftveiene, samt svekke lungefunksjon og øke luftveisplager. Befolkningsstudier har vist sammenhenger mellom ozoneksponering og økt dødelighet av luftveis-, hjerte- og karsykdom, samt økt sykelighet for mennesker med luftveissykdommer.", root)} //https://www.fhi.no/nettpub/luftkvalitet/temakapitler/ozon/
                1 -> {displayTypeFact("PM2.5 er betegnelse på partikler med diameter under 2.5 mikrometer, og omtales som fint svevestøv. Partiklene stammer hovedsakelig fra industriutslipp og biltrafikk. Siden de er så små og lette, har fine partikler en tendens til å holde seg lenger i luften enn tyngre partikler. Dette øker sjansene for at mennesker og dyr inhalerer partiklene. Barn, eldre, og de som lider av lunge- og / eller hjertesykdom er spesielt sårbare, og bør ta spesielle forholdsregler når PM2.5 verdien krysser usunne nivåer.", root)}
                2 -> {displayTypeFact("NO2 kan være helseskadelig for alle mennesker, men barn, eldre og folk med luftveis- og hjertekar problemer er spesielt sårbare. \nNitrogendioksid (NO2) er en helseskadelig gass, og hovedkilden er trafikkerte veier. Helseeffekter er svekket lungeinfeksjon, og forsterkelse av astma. Langvarig eksponering kan bidra til utvikling av luftveissykdommer som astma.\nKalde vinterdager med lite vind, særlig langs hovedveier, er dager som ofte har høy konsentrasjon. Oslo og Bergen har hatt de høyeste verdiene.", root )}           //https://www.fhi.no/nyheter/2020/nitrogendioksid-forverrer-helsa-ved-lave-nivaer/
                3 -> {displayTypeFact("PM10 er betegnelse på partikler med diameter under 10 mikrometer (1/1000000 meter), og omtales i dagligtalen som svevestøv. Partiklene kan stamme fra blant annet industriutslipp og biltrafikk. Verdier over 35 mikrogram regnes som uakseptabelt ifølge vedtatte norske luftkvalitetskriterier. Ifølge Verdens helseorganisasjon (WHO) vil en tredagers periode med 50 mikrogram PM10 per kubikkmeter resultere i 1000 nye astmaanfall og fire dødsfall i en by med 1 million innbyggere. I England er det beregnet at PM10-partikler forårsaker 2000 til 10 000 dødsfall per år. Omlag 86 % av PM10 kommer fra vei- og gatetrafikk. I USA skyldes 64 000 dødsfall årlig virkninger på hjerte/lunge av svevestøv. Partiklene inneholder substanser som man vet er kreftfremkallende i andre sammenhenger.", root)}
            }
        }
        slideShow(command, dialog)
    }

    //for hver av typene i lista.
    private fun displayTypeFact(message: String, root : View){
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle(((message.split(" ".toRegex(), 2).toTypedArray())[0]))  //setter første ord som tittel. alle stringsene om typene, har typen som første ord.
            .setIcon(R.drawable.ic_info)
            .setMessage(message)
            .setPositiveButton("Lukk") { _, _ -> }
            .setNeutralButton("Tilbake") { _, _ -> openValueList("back", root)}
        slideShow("next", dialog)
    }

    //viser dialog/pop up vindu. brukes for infoknappen øverst.
    private fun alertView(message: String, root : View, command : String) {
        val dialogB = AlertDialog.Builder(context) //setter opp Builder objekt.
        dialogB.setTitle("Om luftkvalitet")
            .setIcon(R.drawable.ic_info)
            .setMessage(message)
            .setPositiveButton("Lukk") { _,_ -> }
            .setNeutralButton("les mer") { _, _ -> root.findNavController().navigate(R.id.action_navigation_dialog_to_AboutAirQualityFragment)} //åpner infosiden under settings.
            .setNegativeButton("se funfact") { _, _ -> displayFunfacts(root) }  //kaller metode for å se funfacts. åpner en egen dialog der man kan bla gjennom funfacts
        slideShow(command, dialogB)
    }

    //viser fram fun facts etter man har trykket på infoknappen og "les funfacts"
    private fun displayFunfacts(root: View) {
        val facts = listOf(
            "Barn er mest sårbare for luftforurensning - men vi er alle berørt.",
            "Fem dager inn i 2017 ble de årlige grensene for luftforurensning i London brutt.",
            "De globale kostnadene for luftforurensning er 225 milliarder dollar årlig, ifølge Verdensbanken.",
            "De minste partiklene er de farligste.",
            "I 2019 var det kun to brudd på grense- og målsettingsverdiene for lokal luftkvalitet i Norge, begge knyttet til utslipp fra industrivirksomhet.",
            "Fint svevestøv (PM2,5) kommer hovedsakelig fra langtransportert luftforurensning og fra lokal vedfyring.",
            "Lokalt er utslipp fra vedfyring den viktigste kilden til fint svevestøv (PM2,5) mens utslipp fra eksos kan være viktig i områdene med de høyeste nivåene.",
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

        var factIndex = (facts.indices).random()       //gjør at man får tilfeldig rekkefølge på faktaen hver gang.
        val newDialog = AlertDialog.Builder(context)
        newDialog.setTitle("Funfact om AQI")
        newDialog.setIcon(R.drawable.ic_funfact)
        newDialog.setMessage(facts[factIndex])
            .setPositiveButton("Lukk") { _, _ ->}
            .setNeutralButton("Neste funfact") { _, _ ->
                factIndex = (facts.indices).random()
                newDialog.setMessage(facts[factIndex])
                slideShow("next", newDialog) }
            .setNegativeButton("tilbake") { _, _ -> alertView(getString(R.string.str_info), root, "back")}
        slideShow("nextNext", newDialog)
    }

    //setter views etter nivåene fra aqiet
    private fun setAqiInformer(map: Map<String, Double>) {
        val highest : Map.Entry<String, Double>? = map.maxByOrNull { it.value } //finner den høyeste verdien blant verdiene.
        var donutColor = "#808080"

        @SuppressLint("SetTextI18n") //ignorerer advarsel på strings
        fun changeVisuals(level : String, type : String)   {  //metoden kalles i createDonut(). endrer views som signaliserer luftkvalitetsnivået etter det faktiske nivået.
            aqiType.text = "[$type]"
            when(level) {
                "green" -> {
                    aqiLevel.setTextColor(Color.parseColor("#3F9F41"))
                    aqiSentence.text = getString(R.string.luftnivaa_bra)
                    aqiSmiley.setBackgroundResource(R.drawable.ic_smiley_lvl1)
                    donutColor = "#3F9F41"
                } "orange" -> {
                aqiLevel.setTextColor(Color.parseColor("#FFCB00"))
                aqiSentence.text = getString(R.string.luftnivaa_moderat)
                aqiSmiley.setBackgroundResource(R.drawable.ic_smiley_lvl2)
                donutColor = "#FFCB00"
            } "red" -> {
                aqiLevel.setTextColor(Color.parseColor("#C13500"))
                aqiSentence.text = getString(R.string.luftnivaa_utsatte)
                aqiSmiley.setBackgroundResource(R.drawable.ic_smiley_lvl3)
                donutColor = "#C13500"
            } "purple" -> {
                aqiLevel.setTextColor(Color.parseColor("#4900AC")) //endres til oransje
                aqiSentence.text = getString(R.string.luftnivaa_usunt)
                aqiSmiley.setBackgroundResource(R.drawable.ic_smiley_lvl4)
                donutColor = "#4900AC"
            }
            }
        }

        // donutview-seksjonen for nivaaet
        fun createDonut() {
            val donutSection = highest?.value?.let { DonutSection("pollution level", Color.parseColor(donutColor), it.toFloat()) }
            if (donutSection != null) donutView.submitData(listOf(donutSection))
        }

        //sjekker verdinivåene, og setter views deretter
        if (highest != null)
            when (highest.key) {
                "no2" -> {
                    numberMax.text = "\n400"
                    donutView.cap = 400F
                    if (highest.value <= 100.0) changeVisuals("green", "Nitrogenoksid")
                    else if (highest.value in 100.0..200.0) changeVisuals("orange", "Nitrogenoksid")
                    else if (highest.value in 200.0..400.0) changeVisuals("red", "Nitrogenoksid")
                    else if (highest.value >= 400.0) changeVisuals("purple", "Nitrogenoksid")
                }
                "pm10" -> {
                    numberMax.text = "\n400"
                    donutView.cap = 400F
                    if (highest.value <= 60.0) changeVisuals("green", "Svevestøv (pm10)")
                    else if (highest.value in 60.0..120.0) changeVisuals("orange", "Svevestøv (pm10)")
                    else if (highest.value in 120.0..400.0) changeVisuals("red", "Svevestøv (pm10)")
                    else if (highest.value >= 400.0) changeVisuals("purple", "Svevestøv (pm10)")
                }
                "pm25" -> {
                    numberMax.text = "\n150"
                    donutView.cap = 150F
                    if (highest.value <= 30.0) changeVisuals("green", "Svevestøv (pm2.5)")
                    else if (highest.value in 30.0..50.0) changeVisuals("orange", "Svevestøv (pm2.5)")
                    else if (highest.value in 50.0..150.0) changeVisuals("red", "Svevestøv (pm2.5)")
                    else if (highest.value >= 150.0) changeVisuals("purple", "Svevestøv (pm2.5)")
                }
                "o3" -> {
                    numberMax.text = "\n240"
                    donutView.cap = 240F
                    if (highest.value <= 100.0) changeVisuals("green", "Ozon")
                    else if (highest.value in 100.0..180.0) changeVisuals("orange", "Ozon")
                    else if (highest.value in 180.0..240.0) changeVisuals("red", "Ozon")
                    else if (highest.value >= 240.0) changeVisuals("purple", "Ozon")
                }
            }
        // endrer tekst midt i donuten og lager donuten
        aqiLevel.text = (highest?.value?.toInt().toString() + " μg/m3")
        createDonut()
    }
}
