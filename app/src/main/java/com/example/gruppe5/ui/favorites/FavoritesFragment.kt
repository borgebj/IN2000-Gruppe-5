package com.example.gruppe5.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.gruppe5.R
import com.example.gruppe5.Stasjon
import com.example.gruppe5.StasjonAdapter

class FavoritesFragment : Fragment() {

    // globale variabler
    private lateinit var viewModel: FavoritesViewModel
    lateinit var textView: TextView
    lateinit var searchBar: EditText
    lateinit var searchBut: ImageButton

    lateinit var recycler: RecyclerView
    lateinit var adapter: StasjonAdapter // gjenbruker StasjonAdapter fra testFiler

    // maa beholde denne listen for brukeren paa en eller annen maate -- kanskje Model?????
    var fav_stations: MutableList<Stasjon> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root: View = inflater.inflate(R.layout.fragment_favorites, container, false)

        assignId(root)
        addAdapter()
        setOnClickers()


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
        //searchBut = root.findViewById(R.id.search_but)
        //recycler = root.findViewById(R.id.favorites_recycler)
    }

    fun addAdapter() {
        adapter = StasjonAdapter(fav_stations)
        recycler.adapter = adapter
    }

    private fun setOnClickers() {

        var wanted : String

        searchBut.setOnClickListener {

            wanted = searchBar.text.toString()

            // sjekker om jeg kan hente text for wanted
            val duration = Toast.LENGTH_SHORT
            val toast = Toast.makeText(context, wanted, duration)
            toast.show()

            /*
            henter info om "wanted" (station) med API-fetching

            if API success
                --> legge til "wanted" i lista, fav_stations

            else
                --> "Station not found"-melding til brukeren

            */

        }
    }

}