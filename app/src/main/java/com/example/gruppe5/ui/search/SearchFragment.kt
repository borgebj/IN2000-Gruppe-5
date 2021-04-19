package com.example.gruppe5.ui.search

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.view.inputmethod.InputMethodManager
import android.widget.*
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
    lateinit var adapter : ArrayAdapter<*>
    lateinit var listView : ListView
    /*lateinit var searchBar: EditText
    lateinit var searchBut: ImageButton
    lateinit var stasjoner : List<Stasjon>
    private val path: String = "https://api.met.no/weatherapi/airqualityforecast/0.1/stations"
    val gson = Gson()*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root : View = inflater.inflate(R.layout.search_fragment, container, false)

        setHasOptionsMenu(true)

        assignId(root)
        //setSearchBut(root)
        setOnListView(root)

        return root
    }

    //TODO: Gjøre slik at hvis man søker tilsvarende en av de tre område-gruppene (delområde, grunnkrets, kommune)
    // (f.eks. oslo) så går de gjennom listen med alle stasjoner, og henter og viser kun de som inneholder kommunen Oslo

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        viewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
    }

    fun assignId(root: View) {
        textView = root.findViewById(R.id.search_text)
        listView = root.findViewById(R.id.list_view)
        adapter = ArrayAdapter(root.context, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.search_bar_strings))
        listView.adapter = adapter
        //searchBar = root.findViewById(R.id.search_bar)
        //searchBut = root.findViewById(R.id.search_but)
    }

    fun setOnListView(root: View){
        listView.onItemClickListener = AdapterView.OnItemClickListener{ parent, view, position, id ->
            val station = parent?.getItemAtPosition(position).toString()
            //toastMsg(station)
            closeKeyboard(listView)

            val action = SearchFragmentDirections.actionNavigationSearchToNavigationFavorites(station)
            root.findNavController().navigate(action)
            Log.d("SENDE STATION TIL FAV", station)
        }
        listView.emptyView = root.findViewById(R.id.empy_text_view)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.search_bar_menu, menu)
        val search = menu.findItem(R.id.nav_search)
        val searchView = search?.actionView as SearchView
        searchView.queryHint= "Search a station"

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    fun closeKeyboard(e : View){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(e.windowToken, 0)
    }

/*
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
                    //Log.d("I FOR_LOEKKE", station.name)
                    if (station.name.equals(wanted, ignoreCase = true)){ // finner match
                            //if (root.findNavController().currentDestination?.id == R.id.navigation_search)
                            val action = SearchFragmentDirections.actionNavigationSearchToNavigationFavorites(station)
                            root.findNavController().navigate(action)
                            Log.d("SENDE STATION TIL FAV", station.name)

                            return@withContext
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
*/
}