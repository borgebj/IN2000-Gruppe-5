package com.example.gruppe5.ui.favorites

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gruppe5.R
import com.example.gruppe5.Stasjon
import com.example.gruppe5.StasjonAdapter
import com.example.gruppe5.ui.map.ViewModel


@Suppress("UsePropertyAccessSyntax")
class FavoritesFragment : Fragment() {

    // globale variabler
    private lateinit var viewModel: ViewModel

    lateinit var addBut: ImageButton
    lateinit var resetB : Button
    lateinit var fav_recycler: RecyclerView
    lateinit var fav_adapter: StasjonAdapter

    lateinit var root: View

    var fav_stations: MutableList<Stasjon> = mutableListOf()
    lateinit var pref : SharedPreferences// = requireContext().getSharedPreferences("my_pref", MODE_PRIVATE)
    lateinit var editor : SharedPreferences.Editor// = pref.edit()
    var antKeys = 0 // antall lagrede favorittstasjoner - maks5

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("onCreate()", "KALT")
        Log.d("andKeys i onCreate", antKeys.toString())
        toastMsg("Loading ..")
    }*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root: View = inflater.inflate(R.layout.fragment_favorites, container, false)
        this.root = root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ViewModel::class.java)
        assignId(root)
        addAdapter()

        Log.d("antKeys i onViewCreate", antKeys.toString())
        /*viewModel.fav_stations.observe(viewLifecycleOwner,{
            fav_adapter = StasjonAdapter(it.toMutableList())
            fav_recycler.adapter = fav_adapter
        })*/

        setResetBut(root)
        setSearchFrag(root)
        setInfoBut()

        //toastMsg("Loading ..")
        viewModel.stations.observe(viewLifecycleOwner, Observer {
            Log.d("I OBSERVE", "KALT")

            setFavStations(it)
            getDataTilbake(it)
        })
        checkDeleteElem()
    }

    fun assignId(root: View) {

        Log.d("assignID", "KALT")

        addBut = root.findViewById(R.id.add_but)
        resetB = root.findViewById(R.id.reset_but)
        fav_recycler = root.findViewById(R.id.favorites_recycler)
        fav_recycler.layoutManager = LinearLayoutManager(root.context, RecyclerView.VERTICAL, false)

        pref = requireContext().getSharedPreferences("pre", MODE_PRIVATE)
        editor = pref.edit()
        antKeys = pref.all.size

    }

    private fun onClick(root: View) {
        if (root.id === R.id.cardView) {
            // TODO
        }
    }

    fun addAdapter() {
        fav_adapter = StasjonAdapter(fav_stations)
        fav_recycler.adapter = fav_adapter
    }

    @SuppressLint("ApplySharedPref")
    fun setResetBut(root: View){

        resetB.setOnClickListener{

            editor.clear().commit()
            fav_stations = mutableListOf()
            //viewModel.resetFavStations() // TODO
            fav_adapter.notifyDataSetChanged()
            refresh(root)
        }
    }

    fun refresh(root: View){

        root.findNavController().navigate(
            FavoritesFragmentDirections.actionNavigationFavoritesSelf()
        )
    }

    fun setSearchFrag(root: View){

        Log.d("setSearchFrag", "KALT")

        addBut.setOnClickListener {
            tilSearch(root)  // navigere til SearchFragment
            if (antKeys == 5) toastMsg("List is full! Reset favorite stations to add new favorite.")
        }
    }

    fun tilSearch(root: View){

        root.findNavController().navigate(FavoritesFragmentDirections.actionNavigationFavoritesToNavigationSearch2())
    }

    fun setFavStations(stasjoner: MutableList<Stasjon>){

        val keys: Map<String, *> = pref.getAll()
        for ((key, value) in keys) {
            Log.d("map values", key + ": " + value.toString())
            addToFavStations(value as String, stasjoner)
        }
    }

    fun addToFavStations(station: String, stasjoner: MutableList<Stasjon>){

        Log.d("addToFavStations", "KALT med ${station}")

        for (st in stasjoner){
            if (st.name.equals(station, ignoreCase = true)){ // finner match

                //if (!viewModel.inFavStations(station)){
                if (!inFavStations(station)){ // ikke satt til CardView ennaa
                    //viewModel.addStatToFav(st)
                    fav_stations.add(st)
                    fav_adapter.notifyDataSetChanged()
                    Log.d("ADDED TO FAV", st.name)

                    if (!inPref(station)) { // ikke satt til pref ennaa = ny favorittby
                        setElem(station, station)
                        antKeys++
                    }
                } else //toastMsg("${station} is already in fav_stasjoner.")
                break
            }
        }
    }

    private fun inPref(station: String) : Boolean {

        val keys: Map<String, *> = pref.getAll()
        for ((key, value) in keys) {
            Log.d("map values", key + ": " + value.toString())

            if (station == value) return true
        }
        return false
    }

    private fun inFavStations(station: String) : Boolean {

        for (st in fav_stations){
            if (st.name == station) return true
        }
        return false
    }


    @SuppressLint("CommitPrefEdits")
    fun setElem(station: String, key: String){
        editor.putString(key, station)
        editor.commit()
    }


    fun getDataTilbake(stasjoner: MutableList<Stasjon>){

        val station: String? = FavoritesFragmentArgs.fromBundle(requireArguments()).favoriteStation //args.favoriteStation
        Log.d("getdatatilbake()", "${station}")
        if (antKeys != 5) { // kan lagre MAKS FEM favorittstasjoner -- antall elementer som kan legges til kan endres
            if (station != null && !inFavStations(station)) addToFavStations(station, stasjoner)
            else if (inFavStations(station.toString())) toastMsg("${station} is already in your favorite list") //TODO: Denne refreshes hver gang man går fram og tilbake fra søkefeltet til favorittene !
            else Log.d("STATION(Favorite)", "IS NULL")
        }
    }

    fun checkDeleteElem(){
        Log.d("checkDeleteElem()", "KALT")

        val args = arguments // station as Stasjon som skal slettes fra pref
        if (args != null) {
            val slettes: Stasjon? = args?.getParcelable("station") as Stasjon?

            val keys: Map<String, *> = pref.getAll()
            for ((key, value) in keys) {
                Log.d("map values", key + ": " + value.toString())
                if (slettes?.name == value) {
                    editor.remove(key)
                    editor.commit()
                    antKeys--
                    fav_stations.remove(slettes)
                    fav_adapter.notifyDataSetChanged()
                    break
                }
            }
        }
        else Log.d("bundle for slettes", "NULL")
    }

    fun toastMsg(msg: String){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun setInfoBut() {
        val infoButton: ImageButton = root.findViewById(R.id.info_favorites)
        infoButton.setOnClickListener {
            alertView(getString(R.string.str_info_favorites), root, "open")
        }
    }

    //infoknapp
    private fun alertView(message: String, root: View, command: String) {
        val dialogB = AlertDialog.Builder(context)
        dialogB.setTitle("Hvordan fungerer det?")
            .setIcon(R.drawable.ic_info_green)
            .setMessage(message)
            .setPositiveButton("Lukk") { dialoginterface, i ->}
        slideShow(command, dialogB)
    }

    private fun slideShow(command: String, dialog: AlertDialog.Builder){
        val animasjonsDialog : AlertDialog = dialog.create()
        if (command == "open") animasjonsDialog.window?.attributes?.windowAnimations = R.style.DialogThOpen //animasjon
        else if (command == "close") animasjonsDialog.window?.attributes?.windowAnimations = R.style.DialogThClose //animasjon
        return (animasjonsDialog.show())
    }

    /*fun getElem(key: Int) : String {

    val station : String? = pref.getString(key.toString(), null)
    Log.d("getElem() station", station.toString())

    return station.toString()
}*/

}