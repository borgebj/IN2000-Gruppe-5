package com.example.gruppe5.ui.favorites

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gruppe5.R
import com.example.gruppe5.Stasjon
import com.example.gruppe5.testFiler.StasjonAdapter
import com.google.gson.Gson
import kotlinx.coroutines.*

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
        //"https://in2000-apiproxy.ifi.uio.no/weatherapi/airqualityforecast/0.1/stations"

    // maa beholde denne listen for brukeren paa en eller annen maate -- kanskje Model?????
    var fav_stations: MutableList<Stasjon> = mutableListOf()
    val gson = Gson()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root: View = inflater.inflate(R.layout.fragment_favorites, container, false)

        assignId(root)
        addAdapter()

        var wanted : String

        fun getData(): String {
            return khttp.get(path).text
        }

        searchBut.setOnClickListener {

            wanted = searchBar.text.toString() // henter det brukeren tastet inn
            Log.d("WANTED", wanted)

            CoroutineScope(Dispatchers.IO).launch {

                stasjoner = gson.fromJson(getData(), Array<Stasjon>::class.java).toList()
                    //gson.fromJson(Fuel.get(data).awaitString(), Array<Stasjon>::class.java)//getData(data)

                withContext(Dispatchers.Main){
                    Log.d("API FETCHING", stasjoner.toString())

                    if (wanted != "") {
                        var added = false
                        for (station in stasjoner){
                            Log.d("I FOR_LOEKKE", station.name)

                            if (station.name.equals(wanted, ignoreCase = true)){
                                if (station in fav_stations) { // unngaa duplikasjon
                                    toastMsg("${wanted} is already your favorite city.")
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

                    searchBar.text.clear()
                    //searchBar.onEditorAction(EditorInfo.IME_ACTION_DONE) // lukker IKKE tastatur
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
        fav_recycler = root.findViewById(R.id.favorites_recycler)
        fav_recycler.layoutManager = LinearLayoutManager(root.context, RecyclerView.VERTICAL, false)
    }

    fun addAdapter() {
        fav_adapter = StasjonAdapter(fav_stations)
        fav_recycler.adapter = fav_adapter
    }

    fun toastMsg(msg : String){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }


    /*suspend fun getData() : List<Stasjon> { // Fuel. funksjonen funker kun med proxy-serveren. funksjonen settes i onCreateView()

        var ret : List<Stasjon> = listOf()
        try {
            ret = gson.fromJson(Fuel.get(path).awaitString(), Array<Stasjon>::class.java).toList()
        }
        catch (exception: Exception) {
            println("A network request exception was thrown: ${exception.message}")
        }

        return ret
    }*/

}