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
import com.google.gson.Gson

class FavoritesFragment : Fragment() {

    // globale variabler
    private lateinit var viewModel: FavoritesViewModel
    lateinit var textView: TextView
    lateinit var addBut: ImageButton
    lateinit var resetB : Button
    lateinit var fav_recycler: RecyclerView
    lateinit var fav_adapter: StasjonAdapter // gjenbruker StasjonAdapter fra testFiler

    private val path: String = "https://api.met.no/weatherapi/airqualityforecast/0.1/stations"

    var fav_stations: MutableList<Stasjon> = mutableListOf()
    lateinit var pref : SharedPreferences// = requireContext().getSharedPreferences("my_pref", MODE_PRIVATE)
    lateinit var editor : SharedPreferences.Editor// = pref.edit()
    var antKeys = 0 // antall lagrede favorittstasjoner
    var gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root: View = inflater.inflate(R.layout.fragment_favorites, container, false)

        assignId(root)
        addAdapter()
        setResetBut(root)
        setSearchFrag(root)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FavoritesViewModel::class.java)
        viewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
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

        setFavStations()
        fav_adapter = StasjonAdapter(fav_stations)
        fav_recycler.adapter = fav_adapter
    }

    fun setFavStations(){

        if (antKeys != 0){ // fav_statioins er ikke tom
            for (i in 1 .. antKeys){
                val st = getElem(i)
                if (st != null && !sjekkDup(st)) fav_stations.add(st)
                else break
            }
        }
    }

    fun getElem(key: Int) : Stasjon? {

        val e = pref.getString(key.toString(), "")
        val favStation = gson.fromJson(e, Stasjon::class.java)
        //if (favStation != null) Log.d("getElem(${key}) return", favStation.name)

        return favStation
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

    private fun sjekkDup(station: Stasjon) : Boolean = station in fav_stations


    @SuppressLint("CommitPrefEdits")
    fun setElem(station: Stasjon, key: Int){
        //Log.d(" - setElem(${key})", "${station.name}")
        val s = gson.toJson(station)
        editor.putString(key.toString(), s)
        editor.commit()

    }

    fun setSearchFrag(root: View){

        addBut.setOnClickListener {
            tilSearch(root)  // navigere til SearchFragment
        }
        getDataTilbake()
    }

    fun tilSearch(root: View){

        root.findNavController().navigate(
            FavoritesFragmentDirections.actionNavigationFavoritesToNavigationSearch2()
        )
    }

    fun getDataTilbake(){

        val station: Stasjon? = FavoritesFragmentArgs.fromBundle(requireArguments()).favoriteStation //args.favoriteStation

        if (antKeys == 3) toastMsg("List is full!") // kan lagre MAKS TRE favorittstasjoner -- antall elementer som kan legges til kan endres
        else {
            if (station != null) {
                if (!sjekkDup(station)){ // unngaa duplikater

                    setElem(station, ++antKeys)
                    fav_stations.add(station)
                    fav_adapter.notifyDataSetChanged()

                } else {
                    toastMsg("${station.name} is already your favorite city.")
                }
            }
            else Log.d("STATION", "IS NULL")
        }
    }

    fun toastMsg(msg: String){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

}