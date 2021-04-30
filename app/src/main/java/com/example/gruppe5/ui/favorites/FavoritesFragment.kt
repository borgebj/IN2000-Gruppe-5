package com.example.gruppe5.ui.favorites

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
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
import com.example.gruppe5.ui.map.MapViewModel

class FavoritesFragment : Fragment() {

    // globale variabler
    private lateinit var viewModel: MapViewModel
    //lateinit var textView: TextView
    lateinit var addBut: ImageButton
    lateinit var resetB : Button
    lateinit var fav_recycler: RecyclerView
    lateinit var fav_adapter: StasjonAdapter

    lateinit var root: View

    private val path: String = "https://api.met.no/weatherapi/airqualityforecast/0.1/stations"

    var fav_stations: MutableList<Stasjon> = mutableListOf()
    lateinit var pref : SharedPreferences// = requireContext().getSharedPreferences("my_pref", MODE_PRIVATE)
    lateinit var editor : SharedPreferences.Editor// = pref.edit()
    var antKeys = 0 // antall lagrede favorittstasjoner

    override fun onCreate(savedInstanceState: Bundle?) { // dette blir kalt kun når noe besøker favoritefragment vba navigation
        super.onCreate(savedInstanceState)

        Log.d("onCreate()", "KALT")
        Log.d("andKeys",antKeys.toString())
        toastMsg("Loading ..")
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("onCreateView", "KALT")
        val root: View = inflater.inflate(R.layout.fragment_favorites, container, false)
        this.root = root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(MapViewModel::class.java)
        assignId(root)
        addAdapter()

        Log.d("antKeys", antKeys.toString())
        /*viewModel.fav_stations.observe(viewLifecycleOwner,{
            fav_adapter = StasjonAdapter(it.toMutableList())
            fav_recycler.adapter = fav_adapter
        })*/

        setResetBut(root)
        setSearchFrag(root)

        //toastMsg("Loading ..")
        viewModel.stations.observe(viewLifecycleOwner, Observer {
            Log.d("I OBSERVE", "KALT")

            setFavStations(it)
            getDataTilbake(it)
        })
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

    fun addAdapter() {
        fav_adapter = StasjonAdapter(fav_stations)
        fav_recycler.adapter = fav_adapter
    }

    fun setFavStations(stasjoner: MutableList<Stasjon>){

        Log.d("antKey", antKeys.toString())
        if (antKeys != 0){ // fav_statioins er ikke tom
            for (i in 1 .. antKeys){
                val st : String = getElem(i)
                Log.d("setFavStations()", st)

                if (st != null ) addToFavStations(st, stasjoner)
                else break
            }
        }
    }

    fun addToFavStations(station: String, stasjoner: MutableList<Stasjon>){

        Log.d("addToFavStations", "KALT med ${station}")

        for (st in stasjoner){
            if (st.name.equals(station, ignoreCase = true)){ // finner match

                Log.d("FANT MATCH I addToFav", st.name)

                //if (!viewModel.inFavStations(station)){
                if (!inFavStations(station)){ // ikke satt til CardView ennaa
                    //viewModel.addStatToFav(st)
                    fav_stations.add(st) // TODO
                    fav_adapter.notifyDataSetChanged()
                    Log.d("ADDED TO FAV", st.name)

                    if (!inPref(station)) setElem(station, ++antKeys) // ikke satt til pref ennaa = ny favorittby
                    //else toastMsg("${station} is already in pref.")
                } else //toastMsg("${station} is already in fav_stasjoner.")
                break
            }
        }
    }


    fun getElem(key: Int) : String {

        val station : String? = pref.getString(key.toString(), "")
        Log.d("getElem() station", station.toString())

        return station.toString()
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

    private fun inPref(station: String) : Boolean {

        val favs : Map<String, String> = pref.all as Map<String, String>
        for (s in favs){
            if (s.value == station) return true
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
    fun setElem(station: String, key: Int){
        editor.putString(key.toString(), station)
        editor.commit()
    }

    fun setSearchFrag(root: View){

        Log.d("setSearchFrag", "KALT")

        addBut.setOnClickListener {
            tilSearch(root)  // navigere til SearchFragment
            if (antKeys == 5) toastMsg("List is full! Reset favorite stations to add new favorite.")
        }
    }

    fun tilSearch(root: View){

        root.findNavController().navigate(
            FavoritesFragmentDirections.actionNavigationFavoritesToNavigationSearch2()
        )
    }

    fun getDataTilbake(stasjoner: MutableList<Stasjon>){

        val station: String? = FavoritesFragmentArgs.fromBundle(requireArguments()).favoriteStation //args.favoriteStation

        Log.d("getdatatilbake()", "${station}")

        if (antKeys != 5) { // kan lagre MAKS TRE favorittstasjoner -- antall elementer som kan legges til kan endres
            if (station != null && !inFavStations(station)) addToFavStations(station, stasjoner)
            //else if (inFavStations(station.toString())) toastMsg("${station} is already in fav_stasjoner!!!!")
            else Log.d("STATION", "IS NULL")
        }
    }

    fun toastMsg(msg: String){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

}