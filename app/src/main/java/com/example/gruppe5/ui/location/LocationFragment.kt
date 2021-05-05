package com.example.gruppe5.ui.location

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
import androidx.lifecycle.Observer
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

class LocationFragment : Fragment() {

    private lateinit var viewModel : LocationViewModel

    lateinit var stasjonNavn : TextView
    lateinit var aqiLevel : TextView
    lateinit var aqiSentence : TextView
    lateinit var verdiNivaer : TextView
    lateinit var aqiSmiley : ImageView
    lateinit var donutView : DonutProgressView

    lateinit var stasjon: Stasjon
    lateinit var barDataSet : BarDataSet
    lateinit var HorBarChart : HorizontalBarChart          //  NYESTE BAR CHART
    private var pm10Percentage : Float = 0.0f
    private var pm25Percentage : Float = 0.0f
    private var no2Percentage : Float = 0.0f
    private var o3percentage : Float = 0.0f

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val root: View = inflater.inflate(R.layout.fragment_location, container, false)
        val stasjon: Stasjon? = LocationFragmentArgs.fromBundle(requireArguments()).station

        assignId(root)
        setOnClickers(root)
        //setupBarchart(root)


        if (stasjon != null) { // fra Map
            this.stasjon = stasjon
            stasjonNavn.text = stasjon.name
            setAqiInformer(stasjon.verdier)
        }
        else { // fra Favorite
            val args = arguments
            if (args != null) {
                val myStasjon: Stasjon? = args.getParcelable("location") as Stasjon?
                this.stasjon = myStasjon!!
                setAqiInformer(myStasjon.verdier)
                stasjonNavn.text = myStasjon.name
            }
            else Log.d("bundle == null", "HER")
        }

        setSkillGraph(root) // oppretter søylediagrammet
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
        aqiSmiley = root.findViewById(R.id.smiley_location)
        donutView = root.findViewById(R.id.donut_view_location)
        //barchart = root.findViewById(R.id.ski)
    }

    //setter oppa aksene og andre nødvendige detaljer for det horisontale bar chartet
    fun setSkillGraph(root : View) {
        HorBarChart = root.findViewById(R.id.hor_bar_chart)
        HorBarChart.setDrawBarShadow(false)
        val description = Description()
        description.text = "Verdiene vises i %"
        description.textSize = 15f
        HorBarChart.description = description
        HorBarChart.legend.setEnabled(false)
        HorBarChart.setPinchZoom(false)
        HorBarChart.setDrawValueAboveBar(false)

        //Display the axis on the left (contains the labels 1*, 2* and so on)
        //viser aksen på venstre side med labels: pm25, pm10 osv..
        val xAxis = HorBarChart.getXAxis()
        xAxis.setDrawGridLines(false)
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM)
        xAxis.setEnabled(true)
        xAxis.setDrawAxisLine(false)
        xAxis.textSize = 15f
        val yLeft = HorBarChart.axisLeft

        //setter minimum og maximum lengde for verdiene parene representerer. Siden verdier skal vises i prosent, går det til 100
        yLeft.axisMaximum = 100f
        yLeft.axisMinimum = 0f
        yLeft.isEnabled = true      //Var på false før, ser ikke forskjell
        xAxis.setLabelCount(4)      //setter antallet til 4, siden vi har 4 verdier

        //Legger til labels som legges til på den vertikale aksen
        val values = arrayOf("PM10", "PM2.5", "NO2", "O3")
        xAxis.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String? {
                return values.get(value.toInt())
            }
        })

        val yRight = HorBarChart.axisRight
        yRight.setDrawAxisLine(true)
        yRight.setDrawGridLines(false)
        yRight.isEnabled = false
        setGraphData()                                  //setter plasseringen, og annen formattering
        HorBarChart.animateY(2000)    //animasjon
    }

    //metoden returnerer et dangerLevel fra 1-4(4=farligst), ved å regne ut. setter samtidig hver enkelte prosent.
    private fun calculateDangerLevel(liveValue: Double?, polType: String) : Int{
        //initaliserer verdier som brukes til å kalkulere prosent, og hvor farlig prosenten er for den gitte typen.
        var topValue = 0                         //verdien viser max eksponering av ug/m3 i timesmiddel før det blir svært alvorlig. Kan hete maxvalue, men blir misvisende siden verdien kan overstige nivået
        var percentage : Float                   //prosenten for anbefalt eksponering
        var dangerLimit1 = 0                     //hvor mange prosent som må til for å være "lite farlig", "moderat", "alvorlig" og "svært alvorlig".
        var dangerLimit2 = 0
        val dangerLimit3  = 100                 //farenivå "alvorlig" vil alltid være 100%. Farenivået over vil alltid være over 100%. Det er altså kun prosentene for lite farlig og moderat som er relevant her.
        var dangerLevel = 0                     //settes til et tall fra 1 - 4, som forteller hvor farlig nivået er (4 er farligst)

        //setter verdiene som er initalisert, til sin type. Hver type har forskjellige prosentmessige grenseverdier.
        if (polType == "pm10"){ topValue = 400; dangerLimit1 = 15; dangerLimit2 = 30; }
        else if (polType == "pm25"){ topValue = 150; dangerLimit1 = 20; dangerLimit2 = 33 }
        else if (polType == "no2"){ topValue = 400; dangerLimit1 = 25; dangerLimit2 = 50 }
        else if (polType == "o3"){ topValue = 240; dangerLimit1 = 41; dangerLimit2 = 75 }

        //prosenten regnes ut med verdiene som settes ovenfor, og settes for hver av typene.
        if (liveValue != null) {
            percentage = (liveValue.toFloat() / topValue.toFloat()) * 100f
            if (polType == "pm10") pm10Percentage = percentage
            else if (polType == "pm25") pm25Percentage = percentage
            else if (polType == "no2") no2Percentage = percentage
            else if (polType == "o3") o3percentage = percentage


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
        if (dangerLevel == 1){ color = R.color.green }
        else if (dangerLevel == 2){ color = R.color.yellow }
        else if (dangerLevel == 3){ color = R.color.red}           //endres til rød etter merge
        else if (dangerLevel == 4){ color = R.color.purple_700}
        return color
    }

    //metoden setter opp ting
    private fun setValues(){
        //her settes verdiene fra APIet i søylene.
        val pm10lvl = calculateDangerLevel(stasjon.verdier["pm10"], "pm10")
        val pm25lvl = calculateDangerLevel(stasjon.verdier["pm25"], "pm25")
        val no2lvl = calculateDangerLevel(stasjon.verdier["no2"], "no2")
        val o3lvl = calculateDangerLevel(stasjon.verdier["o3"], "o3")

        //liste over innganger/startplassring       - OBS, disse kan endres med apinivået
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, pm10Percentage))
        entries.add(BarEntry(1f, pm25Percentage))
        entries.add(BarEntry(2f, no2Percentage))
        entries.add(BarEntry(3f, o3percentage))

        //initialiserer en instans av barDataSet, for å kunne vise dataen i barchartet
        barDataSet = BarDataSet(entries, "Bar Data Set")

        //setter fargene
        barDataSet.setColors(
            ContextCompat.getColor(HorBarChart.context, setColor(pm10lvl)),    //pm10lvl
            ContextCompat.getColor(HorBarChart.context, setColor(pm25lvl)),    //pm25lvl
            ContextCompat.getColor(HorBarChart.context, setColor(no2lvl)),     //no2lvl
            ContextCompat.getColor(HorBarChart.context, setColor(o3lvl)),      //o3lvl
        )
    }

    private fun setGraphData() {
        setValues()
        barDataSet.setDrawValues(true)              //tekst oppå søylene som viser antall prosent
        barDataSet.valueTextSize = 13f              //endrer tekststørrelsen til teksten oppå søylene
        barDataSet.barBorderColor = R.color.black
        HorBarChart.setDrawBarShadow(true)          //setter skygger
        barDataSet.barShadowColor = Color.argb(40, 150, 150, 150)
        val data = BarData(barDataSet)
        data.barWidth = 0.6f                        //setter breddet på søylene  OBS: for å øke mellomrommet mellom søylene, set verdien til barwidt til <1f
        HorBarChart.data = data                     //avslutningsvis: sett dataen og refresh grafen
        HorBarChart.invalidate()
    }

    fun setOnClickers(root: View){
        val infoButton1 : ImageButton = root.findViewById(R.id.info1_location)
        infoButton1.setOnClickListener(){
            alertView(getString(R.string.str_info), root, "open")
        }
        val infoButton2 : ImageButton = root.findViewById(R.id.info2_location)
        infoButton2.setOnClickListener(){
            alertValuesView(getString(R.string.str_info_values), "open", root)
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

    private fun alertValuesView(message: String, command : String, root : View) {
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle("AQI nivåer")
            .setIcon(R.drawable.ic_info)
            .setMessage(message)
            .setPositiveButton("Lukk") { dialoginterface, i -> } //legge til animasjon senere
            .setNeutralButton("les mer") { dialog, which -> openValueList("next", root) }
        //dialog.show()
        slideShow(command, dialog)
    }

    //kalles i metoden ovenfor. Åpner liste med ulike verdier, som man kan velge i for deretter å få en forklaring. åpnes når det trykkes "les mer"
    private fun openValueList(command : String, root : View){
        // setter opp alert builder
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Velg en type")
            .setNeutralButton("Tilbake") { dialog, which -> alertValuesView(getString(R.string.str_info_values), "back", root)}

        //ToDO - endre til stasjon.verawidi
        //val values = arrayOf("NO2", "PM10", "PM2,5", "O3")
        val values = stasjon.verdier.keys.toTypedArray() //TODO: fra Børge: Idk hva du mente så sjekk om dette fungerer :)
        builder.setItems(values) { dialog, which ->
            when (which) {
                0 -> {displayTypeFact("NO2 kan være helseskadelig for alle mennesker, men barn, eldre og folk med luftveis- og hjertekar problemer er spesielt sårbare. \nNitrogendioksid (NO2) er en helseskadelig gass, og hovedkilden er trafikkerte veier. Helseeffekter er svekket lungeinfeksjon, og forsterkelse av astma. Langvarig eksponering kan bidra til utvikling av luftveissykdommer som astma.\nKalde vinterdager med lite vind, er dager som oftest blir vi utsatt for de høyeste konsentrasjonene om vinteren på kalde dager med lite vind, og spesielt på trafikkerte veier og i tunneler. Oslo og Bergen har hatt de høyeste verdiene.", root )}           //https://www.fhi.no/nyheter/2020/nitrogendioksid-forverrer-helsa-ved-lave-nivaer/
                1 -> {displayTypeFact("PM10 er betegnelse på partikler med diameter under 10 mikrometer (1/1000 000 meter), og omtales i dagligtalen som svevestøv. Partiklene kan stamme fra blant annet industriutslipp og biltrafikk. Verdier over 35 mikrogram regnes som uakseptabelt ifølge vedtatte norske luftkvalitetskriterier. Ifølge Verdens helseorganisasjon (WHO) vil en tredagers periode med 50 mikrogram PM10 per kubikkmeter resultere i 1000 nye astmaanfall og fire dødsfall i en by med 1 million innbyggere. I England er det beregnet at PM10-partikler forårsaker 2000 til 10 000 dødsfall per år. Omlag 86 % av PM10 kommer fra vei- og gatetrafikk. I USA skyldes 64 000 dødsfall årlig virkninger på hjerte/lunge av svevestøv. Partiklene inneholder substanser som man vet er kreftfremkallende i andre sammenhenger.", root)}
                2 -> {displayTypeFact("pm2,5 er betegnelse på partikler med diameter under 2,5 mikrometer, og omtales som fint svevestøv. Partiklene stammer hovedsakelig fra industriutslipp og biltrafikk. Siden de er så små og lette, har fine partikler en tendens til å holde seg lenger i luften enn tyngre partikler. Dette øker sjansene for at mennesker og dyr inhalerer partiklene. Barn, eldre, og de som lider av lunge- og / eller hjertesykdom er spesielt sårbare, og bør ta spesielle forholdsregler når PM2.5 verdien krysser usunne nivåer.", root)}
                3 -> {displayTypeFact("o3 (Ozon) er en reaktiv gass som finnes både nær bakken og høyere opp i atmosfæren. Høye konsentrasjoner av bakkenært ozon i Norge skyldes hovedsakelig langtransportert ozon fra Europa. Ozon frigjøres ikke fra en primær kilde, men dannes via en rekke komplekse reaksjoner i luften. Konsentrasjonen av ozon er noe høyere utenfor byene enn i byene. Ozonkonsentrasjonen i Norge har episodevis nådd nivåer opp mot 160 μg/m3. Studier har vist at astmatiske barn kan få luftveissymptomer ved akutt eksponering for ozon fra 100 til 120 μg/m3. Ozon kan gi betennelse og føre til skader i luftveiene, samt svekke luftveisfunksjon og øke luftveisplager. Befolkningsstudier har vist sammenhenger mellom ozoneksponering og økt dødelighet av luftveis-, hjerte- og karsykdom, samt økt sykelighet for mennesker med luftveissykdommer.", root)} //https://www.fhi.no/nettpub/luftkvalitet/temakapitler/ozon/
            }
        }
        slideShow(command, builder)
        //alertView(getString(R.string.str_info))
        val infoButton2 : ImageButton = root.findViewById(R.id.info2_location)
        infoButton2.setOnClickListener(){
            alertValuesView(getString(R.string.str_info_values), root)
        }
    }

    //test for Location sin infoknapp om ulike nivåer. Gir en liste man kan velge i.
    private fun alertValuesView(message: String, root : View) {
        val dialog = AlertDialog.Builder(context)

        dialog.setTitle("AQI nivåer")
            .setIcon(R.drawable.ic_info)
            .setMessage(message)
            .setPositiveButton("Lukk") { dialoginterface, i -> }
            .setNeutralButton("les mer") { dialog, which -> openValueList(root) }
        dialog.show()
    }

    //kalles i metoden ovenfor. Åpner liste med ulike verdier, som man kan velge i for deretter å få en forklaring.
    private fun openValueList(root : View){
        // setter opp alert builder
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Velg en type")

        // TODO endre til stasjon.verdi
        val values = arrayOf("no2", "pm10", "pm25", "o3")
        builder.setItems(values) { dialog, which ->
            when (which) {
                0 -> { displayTypeFact("no2 er ikke bra for lungene", root) }
                1 -> { displayTypeFact("pm10 er ikke bra for lungene", root) }
                2 -> { displayTypeFact("pm25 er ikke bra for lungene", root) }
                3 -> { displayTypeFact("o3 er heller ikke bra for lungene", root) }
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    //for hver av typene i lista.
    private fun displayTypeFact(message: String, root : View){
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle(((message.split(" ".toRegex(), 2).toTypedArray())[0]))  //setter første ord som tittel
            .setIcon(R.drawable.ic_info)
            .setMessage(message)
            .setPositiveButton("Lukk") { dialoginterface, i -> }
            .setNeutralButton("Tilbake") { dialog, which ->
                openValueList("back", root)}
        //dialog.show()
        slideShow("next", dialog)
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
        //.setNeutralButton("Tilbake") { dialog, which -> openValueList() }
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

    fun setAqiInformer(map: Map<String, Double>) {
        val highest : Map.Entry<String, Double>? = map.maxBy { it.value }
        var donut_color : String = "#808080"


        fun changeVisuals(level : String)   {
            when(level) {
                "green" -> {
                    aqiLevel.setTextColor(Color.parseColor("#3F9F41"))
                    aqiSentence.text = "Luftnivået er bra"
                    aqiSmiley.setBackgroundResource(R.drawable.ic_smiley_lvl1)
                    donut_color = "#3F9F41"
                } "orange" -> {
                aqiLevel.setTextColor(Color.parseColor("#FFCB00"))
                aqiSentence.text = "Luftnivået er moderat"
                aqiSmiley.setBackgroundResource(R.drawable.ic_smiley_lvl2)
                donut_color = "#FFCB00"
            } "red" -> {
                aqiLevel.setTextColor(Color.parseColor("#C13500"))
                aqiSentence.text = "Luftnivået nivået er usunt for utsatte grupper"
                aqiSmiley.setBackgroundResource(R.drawable.ic_smiley_lvl3)
                donut_color = "#C13500"
            } "purple" -> {
                aqiLevel.setTextColor(Color.parseColor("#4900AC")) //endres til oransje
                aqiSentence.text = "Luftnivået nivået er usunt"
                aqiSmiley.setBackgroundResource(R.drawable.ic_smiley_lvl4)
                donut_color = "#4900AC"
            }
            }
        }

        // donutview-seksjonen for nivaaet
        fun createDonut() {
            val donut_section = highest?.value?.let { DonutSection("pollution level", Color.parseColor(donut_color), it.toFloat()) }
            donutView.cap = 500f
            if (donut_section != null) donutView.submitData(listOf(donut_section))
        }

        if (highest != null)
            when (highest.key) {
                "no2" -> {
                    if (highest.value <= 100.0) changeVisuals("green")
                    else if (highest.value in 100.0..200.0) changeVisuals("orange")
                    else if (highest.value in 200.0..400.0) changeVisuals("red")
                    else if (highest.value >= 400.0) changeVisuals("purple")
                }
                "pm10" -> {
                    if (highest.value <= 60.0) changeVisuals("green")
                    else if (highest.value in 60.0..120.0) changeVisuals("orange")
                    else if (highest.value in 120.0..400.0) changeVisuals("red")
                    else if (highest.value >= 400.0) changeVisuals("purple")
                }
                "pm25" -> {
                    if (highest.value <= 30.0) changeVisuals("green")
                    else if (highest.value in 30.0..50.0) changeVisuals("orange")
                    else if (highest.value in 50.0..150.0) changeVisuals("red")
                    else if (highest.value >= 150.0) changeVisuals("purple")
                }
                "o3" -> {
                    if (highest.value <= 100.0) changeVisuals("green")
                    else if (highest.value in 100.0..180.0) changeVisuals("orange")
                    else if (highest.value in 180.0..240.0) changeVisuals("red")
                    else if (highest.value >= 240.0) changeVisuals("purple")
                }
            }
        // endrer tekst midt i donut og lager donut
        aqiLevel.text = (highest?.value?.toInt().toString() + " ug/m3")
        createDonut()
    }
}