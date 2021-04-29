package com.example.gruppe5.ui.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
<<<<<<< Updated upstream
import androidx.navigation.fragment.navArgs
=======
import androidx.navigation.findNavController
>>>>>>> Stashed changes
import com.example.gruppe5.R

class LocationFragment : Fragment() {

    private lateinit var viewModel : LocationViewModel

    lateinit var stasjonNavn : TextView
    lateinit var aqiLevel : TextView
    lateinit var aqiSentence : TextView
    lateinit var verdiNivaer : TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val root: View = inflater.inflate(R.layout.fragment_location, container, false)

        assignId(root)

        val navn = LocationFragmentArgs.fromBundle(requireArguments()).stationName
        val kortNavn = navn.substring(navn.indexOf("[") + 1, navn.indexOf("]"))
        stasjonNavn.text = kortNavn

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
<<<<<<< Updated upstream
=======
        barchart = root.findViewById(R.id.chart)
    }

    fun setupBarchart(root: View) {
        val data = BarData(getDataSet())
        barchart.data = data
        barchart.animateXY(2000, 2000)
        barchart.invalidate()
    }

    fun getDataSet() : BarDataSet {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(4f, 0f))
        entries.add(BarEntry(8f, 1f))
        entries.add(BarEntry(16f, 2f))
        val dataset = BarDataSet(entries, "hi")
        return dataset
    }

    fun setOnClickers(root: View){
        val infoButton1 : ImageButton = root.findViewById(R.id.info1_location)
        infoButton1.setOnClickListener(){
            alertView(getString(R.string.str_info), root, "open")
        }
        val infoButton2 : ImageButton = root.findViewById(R.id.info2_location)
        infoButton2.setOnClickListener(){
            alertValuesView(getString(R.string.str_info_values), "open")
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

    private fun alertValuesView(message: String, command : String) {
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle("AQI nivåer")
            .setIcon(R.drawable.ic_info)
            .setMessage(message)
            .setPositiveButton("Lukk") { dialoginterface, i -> } //legge til animasjon senere
            .setNeutralButton("les mer") { dialog, which -> openValueList("next") }
        //dialog.show()
        slideShow(command, dialog)
    }

    //kalles i metoden ovenfor. Åpner liste med ulike verdier, som man kan velge i for deretter å få en forklaring. åpnes når det trykkes "les mer"
    private fun openValueList(command : String){
        // setter opp alert builder
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Velg en type")
            .setNeutralButton("Tilbake") { dialog, which -> alertValuesView(getString(R.string.str_info_values), "back")}

        //ToDO - endre til stasjon.verdi
        val values = arrayOf("NO2", "PM10", "PM2,5", "O3")
        builder.setItems(values) { dialog, which ->
            when (which) {
                0 -> {displayTypeFact("NO2 kan være helseskadelig for alle mennesker, men barn, eldre og folk med luftveis- og hjertekar problemer er spesielt sårbare. \nNitrogendioksid (NO2) er en helseskadelig gass, og hovedkilden er trafikkerte veier. Helseeffekter er svekket lungeinfeksjon, og forsterkelse av astma. Langvarig eksponering kan bidra til utvikling av luftveissykdommer som astma.\nKalde vinterdager med lite vind, er dager som oftest blir vi utsatt for de høyeste konsentrasjonene om vinteren på kalde dager med lite vind, og spesielt på trafikkerte veier og i tunneler. Oslo og Bergen har hatt de høyeste verdiene." )}           //https://www.fhi.no/nyheter/2020/nitrogendioksid-forverrer-helsa-ved-lave-nivaer/
                1 -> {displayTypeFact("PM10 er betegnelse på partikler med diameter under 10 mikrometer (1/1000 000 meter), og omtales i dagligtalen som svevestøv. Partiklene kan stamme fra blant annet industriutslipp og biltrafikk. Verdier over 35 mikrogram regnes som uakseptabelt ifølge vedtatte norske luftkvalitetskriterier. Ifølge Verdens helseorganisasjon (WHO) vil en tredagers periode med 50 mikrogram PM10 per kubikkmeter resultere i 1000 nye astmaanfall og fire dødsfall i en by med 1 million innbyggere. I England er det beregnet at PM10-partikler forårsaker 2000 til 10 000 dødsfall per år. Omlag 86 % av PM10 kommer fra vei- og gatetrafikk. I USA skyldes 64 000 dødsfall årlig virkninger på hjerte/lunge av svevestøv. Partiklene inneholder substanser som man vet er kreftfremkallende i andre sammenhenger.")}
                2 -> {displayTypeFact("pm2,5 er betegnelse på partikler med diameter under 2,5 mikrometer, og omtales som fint svevestøv. Partiklene stammer hovedsakelig fra industriutslipp og biltrafikk. Siden de er så små og lette, har fine partikler en tendens til å holde seg lenger i luften enn tyngre partikler. Dette øker sjansene for at mennesker og dyr inhalerer partiklene. Barn, eldre, og de som lider av lunge- og / eller hjertesykdom er spesielt sårbare, og bør ta spesielle forholdsregler når PM2.5 verdien krysser usunne nivåer.")}
                3 -> {displayTypeFact("o3 (Ozon) er en reaktiv gass som finnes både nær bakken og høyere opp i atmosfæren. Høye konsentrasjoner av bakkenært ozon i Norge skyldes hovedsakelig langtransportert ozon fra Europa. Ozon frigjøres ikke fra en primær kilde, men dannes via en rekke komplekse reaksjoner i luften. Konsentrasjonen av ozon er noe høyere utenfor byene enn i byene. Ozonkonsentrasjonen i Norge har episodevis nådd nivåer opp mot 160 μg/m3. Studier har vist at astmatiske barn kan få luftveissymptomer ved akutt eksponering for ozon fra 100 til 120 μg/m3. Ozon kan gi betennelse og føre til skader i luftveiene, samt svekke luftveisfunksjon og øke luftveisplager. Befolkningsstudier har vist sammenhenger mellom ozoneksponering og økt dødelighet av luftveis-, hjerte- og karsykdom, samt økt sykelighet for mennesker med luftveissykdommer.")} //https://www.fhi.no/nettpub/luftkvalitet/temakapitler/ozon/
            }
        }
        slideShow(command, builder)
    }

    //for hver av typene i lista.
    private fun displayTypeFact(message: String){
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle(((message.split(" ".toRegex(), 2).toTypedArray())[0]))  //setter første ord som tittel
            .setIcon(R.drawable.ic_info)
            .setMessage(message)
            .setPositiveButton("Lukk") { dialoginterface, i -> }
            .setNeutralButton("Tilbake") { dialog, which ->
                openValueList("back")}
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
>>>>>>> Stashed changes
    }
}