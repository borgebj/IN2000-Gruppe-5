package com.example.gruppe5.ui.favorites

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.gruppe5.R
import com.example.gruppe5.Stasjon
import com.example.gruppe5.testFiler.StasjonAdapter
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import com.google.gson.Gson
import kotlinx.coroutines.*

class FavoritesFragment : Fragment() {

    // globale variabler
    private lateinit var viewModel: FavoritesViewModel
    lateinit var textView: TextView
    lateinit var searchBar: EditText
    lateinit var searchBut: ImageButton
    lateinit var recycler: RecyclerView
    lateinit var adapter: StasjonAdapter // gjenbruker StasjonAdapter fra testFiler

    private val path: String = "https://in2000-apiproxy.ifi.uio.no/weatherapi/airqualityforecast/0.1/stations"

    // maa beholde denne listen for brukeren paa en eller annen maate -- kanskje Model?????
    var fav_stations: MutableList<Stasjon> = mutableListOf()
    val gson = Gson()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root: View = inflater.inflate(R.layout.fragment_favorites, container, false)

        assignId(root)
        addAdapter()

        var wanted : String

        suspend fun getData() : List<Stasjon> {

            var ret : List<Stasjon> = listOf()
            try {
                ret = gson.fromJson(Fuel.get(path).awaitString(), Array<Stasjon>::class.java).toList()
            }
            catch (exception: Exception) {
                println("A network request exception was thrown: ${exception.message}")
            }

            return ret
        }

        searchBut.setOnClickListener {

            wanted = searchBar.text.toString()

            val data = "https://in2000-apiproxy.ifi.uio.no/weatherapi/airqualityforecast/0.1/stations"

            CoroutineScope(Dispatchers.IO).launch {

                val response = getData()//gson.fromJson(Fuel.get(data).awaitString(), Array<Stasjon>::class.java)//getData(data)

                withContext(Dispatchers.Main){
                    Log.d("API FETCHING", response.toString())
                }

            }


        }


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
        recycler = root.findViewById(R.id.favorites_recycler)
    }

    fun addAdapter() {
        adapter = StasjonAdapter(fav_stations)
        recycler.adapter = adapter
    }


    private fun setOnClickers() {

        var wanted : String

        searchBut.setOnClickListener {

            wanted = searchBar.text.toString()


            CoroutineScope(Dispatchers.IO).launch {

                val response = gson.fromJson(Fuel.get(path).awaitString(), Array<Stasjon>::class.java).toList()

                Log.d("ETTER API FETCHING", path)
                Log.d("API FETCHING", response.toString())

            }



            //    gson.fromJson(Fuel.get(distriktAdr).awaitString(), Array<Candidate>::class.java).toList()

            //henter info om "wanted" (station) med API-fetching

            //if API success
            //    --> legge til "wanted" i lista, fav_stations
            // data vi fikk tilbake fra API-fetching

            //    fav_stations.add(response)



            //else
            //    --> "Station not found"-melding til brukeren



        }
    }

}