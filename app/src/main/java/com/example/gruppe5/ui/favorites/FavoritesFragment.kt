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
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gruppe5.R
import com.example.gruppe5.Stasjon
import com.example.gruppe5.StasjonAdapter
import com.example.gruppe5.ui.map.MapViewModel
import com.example.gruppe5.ui.search.SearchFragmentDirections
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoritesFragment : Fragment() {

    // globale variabler
    private lateinit var viewModel: MapViewModel
    lateinit var textView: TextView
    lateinit var addBut: ImageButton
    lateinit var resetB : Button
    lateinit var fav_recycler: RecyclerView
    lateinit var fav_adapter: StasjonAdapter // gjenbruker StasjonAdapter fra testFiler

    lateinit var root: View

    private val path: String = "https://api.met.no/weatherapi/airqualityforecast/0.1/stations"

    var fav_stations: MutableList<Stasjon> = mutableListOf()
    lateinit var pref : SharedPreferences// = requireContext().getSharedPreferences("my_pref", MODE_PRIVATE)
    lateinit var editor : SharedPreferences.Editor// = pref.edit()
    var antKeys = 0 // antall lagrede favorittstasjoner

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
        viewModel = ViewModelProvider(this).get(MapViewModel::class.java)
        assignId(root)
        addAdapter()
        setResetBut(root)
        setSearchFrag(root)

        viewModel.stations.observe(viewLifecycleOwner, Observer {
            getDataTilbake(it)
            setFavStations(it)

        })
    }

    fun assignId(root: View) {

        textView = root.findViewById(R.id.text_favorites)
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

    fun setFavStations(stasjoner : MutableList<Stasjon>){

        if (antKeys != 0){ // fav_statioins er ikke tom
            for (i in 1 .. antKeys){
                val st : String = getElem(i)
                Log.d("setFavStations()", st)

                if (st != null /*&& !sjekkDup(st)*/) addToFavStations(st, stasjoner)//fav_stations.add(getObj(st)!!)
                else break
            }
        }
    }

    fun addToFavStations(station: String, stasjoner: MutableList<Stasjon>){

        for (st in stasjoner){
            if (st.name.equals(station, ignoreCase = true)){ // finner match

                if (!inFavStations(station)){ // ikke satt til CardView ennaa
                    fav_stations.add(st)
                    fav_adapter.notifyDataSetChanged()
                    Log.d("ADDED TO FAV", st.name)

                    if (!inPref(station)) setElem(station, ++antKeys) // ikke satt til pref ennaa = ny favorittby

                } else {
                    toastMsg("${station} is already your favorite city.")
                }
                break
            }
        }
    }


    fun getElem(key: Int) : String {

        val station : String? = pref.getString(key.toString(), "")
        Log.d("getElem() station", station.toString())

        return station.toString()
        /*val favStation = gson.fromJson(e, Stasjon::class.java)
        //if (favStation != null) Log.d("getElem(${key}) return", favStation.name)

        return favStation*/
    }

    @SuppressLint("ApplySharedPref")
    fun setResetBut(root: View){

        resetB.setOnClickListener{

            editor.clear().commit()
            fav_stations = mutableListOf()
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
        //Log.d(" - setElem(${key})", "${station.name}")
        //val s = gson.toJson(station)
        editor.putString(key.toString(), station)
        editor.commit()
    }

    fun setSearchFrag(root: View){

        addBut.setOnClickListener {
            tilSearch(root)  // navigere til SearchFragment
            if (antKeys == 3) toastMsg("List is full! Reset favorite stations to add new favorite.")
        }
    }

    fun tilSearch(root: View){

        root.findNavController().navigate(
            FavoritesFragmentDirections.actionNavigationFavoritesToNavigationSearch2()
        )
    }

    fun getDataTilbake(stasjoner: MutableList<Stasjon>){

        val station: String? = FavoritesFragmentArgs.fromBundle(requireArguments()).favoriteStation //args.favoriteStation

        if (antKeys != 3) { // kan lagre MAKS TRE favorittstasjoner -- antall elementer som kan legges til kan endres
            if (station != null) addToFavStations(station, stasjoner)
            else Log.d("STATION", "IS NULL")
        }
    }

    fun toastMsg(msg: String){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

}