package com.example.gruppe5.ui.favorites

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gruppe5.R
import com.example.gruppe5.Stasjon
import com.google.gson.Gson
import kotlinx.coroutines.*
import com.example.gruppe5.StasjonAdapter

class FavoritesFragment : Fragment() {

    // globale variabler
    private lateinit var viewModel: FavoritesViewModel
    lateinit var textView: TextView
    lateinit var searchBar: EditText
    lateinit var searchBut: ImageButton
    lateinit var fav_recycler: RecyclerView
    lateinit var fav_adapter: StasjonAdapter // gjenbruker StasjonAdapter fra testFiler
    lateinit var stasjoner : List<Stasjon>

    private val path: String = "https://api.met.no/weatherapi/airqualityforecast/0.1/stations"

    // maa beholde denne listen for brukeren paa en eller annen maate -- kanskje Model?????
    var fav_stations: MutableList<Stasjon> = mutableListOf()
    val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root: View = inflater.inflate(R.layout.fragment_favorites, container, false)

        assignId(root)
        addAdapter()
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
        searchBar = root.findViewById(R.id.search_bar)
        searchBut = root.findViewById(R.id.search_but)
        fav_recycler = root.findViewById(R.id.favorites_recycler)
        fav_recycler.layoutManager = LinearLayoutManager(root.context, RecyclerView.VERTICAL, false)
    }

    fun addAdapter() {
        fav_adapter = StasjonAdapter(fav_stations)
        fav_recycler.adapter = fav_adapter
    }

    fun toastMsg(msg: String){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun getData(): String {
        return khttp.get(path).text
    }

    fun setSearchFrag(root : View){
        searchBar.setOnClickListener {
            tilSearch(root)  // navigere til SearchFragment
        }
        getDataTilbake()
    }

    fun tilSearch(root : View){

        root.findNavController().navigate(
            FavoritesFragmentDirections.actionNavigationFavoritesToNavigationSearch2())
    }

    fun getDataTilbake(){
        //val args: FavoritesFragmentArgs by navArgs()
        val station: Stasjon? = FavoritesFragmentArgs.fromBundle(requireArguments()).favoriteStation //args.favoriteStation
        if (station != null) {
            Log.d("DATA FRA SEARCH", station.name)

            fav_stations.add(station!!)
            fav_adapter.notifyDataSetChanged()
        }
        else {
            Log.d("STATION", "IS NULL")
        }

    }

    fun setSearchBut(){
        searchBut.setOnClickListener {

            val wanted = searchBar.text.toString() // henter det brukeren tastet inn

            CoroutineScope(Dispatchers.IO).launch {

                stasjoner = gson.fromJson(getData(), Array<Stasjon>::class.java).toList()

                withContext(Dispatchers.Main){
                    //Log.d("API FETCHING", stasjoner.toString())

                    if (wanted != "") {
                        var added = false
                        for (station in stasjoner){
                            Log.d("I FOR_LOEKKE", station.name)

                            if (station.name.equals(wanted, ignoreCase = true)){
                                if (station in fav_stations) { // unngaa duplikater
                                    toastMsg("${station.name} is already your favorite city.")
                                    return@withContext
                                }
                                else {
                                    fav_stations.add(station)
                                    fav_adapter.notifyDataSetChanged()
                                    Log.d("FANT STASJON", station.name)
                                    added = true
                                }
                            }
                        }
                        if (!added) toastMsg("${wanted} does not exsist.")

                    }
                    else toastMsg("Enter a city name")
                }
            }
            searchBar.text.clear()
            closeKeyboard(searchBar)
        }
    }

    fun closeKeyboard(e : EditText){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(e.windowToken, 0)
    }

}