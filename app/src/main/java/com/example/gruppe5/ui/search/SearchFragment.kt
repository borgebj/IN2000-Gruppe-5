package com.example.gruppe5.ui.search

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.gruppe5.R

class SearchFragment : Fragment() {

    lateinit var textView: TextView
    lateinit var adapter: ArrayAdapter<*>
    private lateinit var listView: ListView //liste over stasjoner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root: View = inflater.inflate(R.layout.search_fragment, container, false)

        assignId(root)
        setHasOptionsMenu(true)
        setOnListView(root)
        return root
    }

    fun assignId(root: View) {
        textView = root.findViewById(R.id.search_text)
        listView = root.findViewById(R.id.list_view)
        adapter = ArrayAdapter(
            root.context, android.R.layout.simple_list_item_1, resources.getStringArray(
                R.array.search_bar_strings
            )
        )
        listView.adapter = adapter
    }

    // oppretter søke-meny
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.search_bar_menu, menu)
        val search = menu.findItem(R.id.nav_search)
        val searchView = search?.actionView as SearchView
        searchView.queryHint = "Søk på en stasjon"
        searchView.imeOptions = EditorInfo.IME_FLAG_NO_FULLSCREEN

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

    // returnerer valgt stasjon(String) til Map/Favorite
    private fun setOnListView(root: View) {
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->

            val station = parent?.getItemAtPosition(position).toString()
            closeKeyboard(listView)

            val arg: String? = SearchFragmentArgs.fromBundle(requireArguments()).map

            if (arg != null) { //fra Map
                val action =
                    SearchFragmentDirections.actionNavigationSearchToNavigationMap(station)
                root.findNavController().navigate(action)
            } else { //fra Favorite
                val action =
                    SearchFragmentDirections.actionNavigationSearchToNavigationFavorites(station)
                root.findNavController().navigate(action)
            }

        }
        listView.emptyView = root.findViewById(R.id.empty_text_view)
    }

    private fun closeKeyboard(e: View) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(e.windowToken, 0)
    }
}