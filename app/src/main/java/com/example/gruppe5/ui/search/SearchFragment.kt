package com.example.gruppe5.ui.search

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.example.gruppe5.R
import com.example.gruppe5.Stasjon
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchFragment : Fragment() {

    private lateinit var viewModel: SearchViewModel

    lateinit var textView: TextView
    lateinit var searchBar: EditText
    lateinit var searchBut: ImageButton

    lateinit var stasjoner : List<Stasjon>

    private val path: String = "https://api.met.no/weatherapi/airqualityforecast/0.1/stations"

    var fav_stations: MutableList<Stasjon> = mutableListOf() // MAA BEHOLDES
    val gson = Gson()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root : View = inflater.inflate(R.layout.search_fragment, container, false)

        assignId(root)
        setSearchBut(root)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        viewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
    }

    fun assignId(root: View) {
        textView = root.findViewById(R.id.search_text)
        searchBar = root.findViewById(R.id.search_bar)
        searchBut = root.findViewById(R.id.search_but)
    }


    fun toastMsg(msg: String){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun getData(): String {
        return khttp.get(path).text
    }

    fun setSearchBut(root : View){
        searchBut.setOnClickListener {

            val wanted = searchBar.text.toString() // henter det brukeren tastet inn

            CoroutineScope(Dispatchers.IO).launch {

                stasjoner = gson.fromJson(getData(), Array<Stasjon>::class.java).toList()
                handleData(root, wanted)
            }
            searchBar.text.clear()
            closeKeyboard(searchBar)
        }
    }

    suspend fun handleData(root : View, wanted : String){

        withContext(Dispatchers.Main){

            if (wanted != "") {
                for (station in stasjoner){
                    Log.d("I FOR_LOEKKE", station.name)

                    if (station.name.equals(wanted, ignoreCase = true)){ // finner match

                        if (station in fav_stations) { // unngaa duplikater -- fav_stations oppdateres hver gang -- MAA FIKSES
                            Log.d("I FAV_STATION", station.name)
                            toastMsg("${station.name} is already your favorite city.")
                        }
                        else {
                            // sender station tilbake til FavorteFragment
                            fav_stations.add(station) // MAA FIKSES slik at elementene i fav_stations ikke forandres fra forrige kall
                            Log.d("ADDED", station.name)

                            //if (root.findNavController().currentDestination?.id == R.id.navigation_search){
                            val action = SearchFragmentDirections.actionNavigationSearchToNavigationFavorites(station)
                            root.findNavController().navigate(action)
                            Log.d("SENDE STATION TIL FAV", station.name)
                            //}
                            return@withContext
                        }
                    }
                }
                toastMsg("Station: ${wanted} does not exsist.")
            }
            else toastMsg("Enter a city name")
        }
    }

    fun closeKeyboard(e : EditText){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(e.windowToken, 0)
    }


}