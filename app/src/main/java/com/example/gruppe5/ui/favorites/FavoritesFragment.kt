package com.example.gruppe5.ui.favorites

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gruppe5.R
import com.example.gruppe5.Stasjon
import com.example.gruppe5.StasjonAdapter
import com.example.gruppe5.ui.map.ViewModel


@Suppress("UsePropertyAccessSyntax")
class FavoritesFragment : Fragment() {

    // globale variabler
    private lateinit var viewModel: ViewModel

    private lateinit var addBut: ImageButton
    private lateinit var resetB : ImageButton
    private lateinit var favRecycler: RecyclerView
    private lateinit var favAdapter: StasjonAdapter

    private lateinit var root: View

    private var favStations: MutableList<Stasjon> = mutableListOf()
    private lateinit var pref : SharedPreferences// = requireContext().getSharedPreferences("my_pref", MODE_PRIVATE)
    private lateinit var editor : SharedPreferences.Editor// = pref.edit()
    private var antKeys = 0 // antall lagrede favorittstasjoner - maks5


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
        viewModel = ViewModelProvider(this).get(ViewModel::class.java)

        assignId(root)
        setHasOptionsMenu(true)
        addAdapter()
        setResetBut(root)
        setSearchFrag(root)
        //setInfoBut()

        viewModel.stations.observe(viewLifecycleOwner, {
            //Log.d("I OBSERVE", "KALT")

            setFavStations(it)
            getDataTilbake(it)
        })
        checkDeleteElem()
    }

    @SuppressLint("CommitPrefEdits")
    fun assignId(root: View) {

        Log.d("assignID", "KALT")

        addBut = root.findViewById(R.id.add_but)
        resetB = root.findViewById(R.id.reset_but)
        favRecycler = root.findViewById(R.id.favorites_recycler)
        favRecycler.layoutManager = LinearLayoutManager(root.context, RecyclerView.VERTICAL, false)

        pref = requireContext().getSharedPreferences("pre", MODE_PRIVATE)
        editor = pref.edit()
        antKeys = pref.all.size

    }

    private fun addAdapter() {
        favAdapter = StasjonAdapter(favStations)
        favRecycler.adapter = favAdapter
    }

    @SuppressLint("ApplySharedPref")
    fun setResetBut(root: View){

        resetB.setOnClickListener{

            editor.clear().commit()
            favStations = mutableListOf()
            favAdapter.notifyDataSetChanged()
            refresh(root)
        }
    }

    private fun refresh(root: View){

        root.findNavController().navigate(
            FavoritesFragmentDirections.actionNavigationFavoritesSelf()
        )
    }

    private fun setSearchFrag(root: View){

        Log.d("setSearchFrag", "KALT")

        addBut.setOnClickListener {
            tilSearch(root)  // navigere til SearchFragment
            if (antKeys == 5) Toast.makeText(context, "List is full! delete/reset favorite stations to add new favorite.", Toast.LENGTH_LONG).show()
        }
    }

    private fun tilSearch(root: View){

        root.findNavController().navigate(FavoritesFragmentDirections.actionNavigationFavoritesToNavigationSearch2())
    }

    private fun setFavStations(stasjoner: MutableList<Stasjon>){

        val keys: Map<String, *> = pref.getAll()
        for ((key, value) in keys) {
            Log.d("map values", key + ": " + value.toString())
            addToFavStations(value as String, stasjoner)
        }
    }

    private fun addToFavStations(station: String, stasjoner: MutableList<Stasjon>){

        Log.d("addToFavStations", "KALT med $station")

        for (st in stasjoner){
            if (st.name.equals(station, ignoreCase = true)){ // finner match

                if (!inFavStations(station)){ // ikke satt til CardView ennaa
                    favStations.add(st)
                    favAdapter.notifyDataSetChanged()
                    Log.d("ADDED TO FAV", st.name)

                    if (!inPref(station)) { // ikke satt til pref ennaa = ny favorittby
                        setElem(station, station)
                        antKeys++
                    }
                }
                break
            }
        }
    }

    private fun inPref(station: String) : Boolean {

        val keys: Map<String, *> = pref.getAll()
        for ((key, value) in keys) {
            Log.d("map values", key + ": " + value.toString())

            if (station == value) return true
        }
        return false
    }

    private fun inFavStations(station: String) : Boolean {

        for (st in favStations){
            if (st.name == station) return true
        }
        return false
    }


    @SuppressLint("CommitPrefEdits")
    fun setElem(station: String, key: String){
        editor.putString(key, station)
        editor.commit()
    }


    private fun getDataTilbake(stasjoner: MutableList<Stasjon>){

        val station: String? = FavoritesFragmentArgs.fromBundle(requireArguments()).favoriteStation //args.favoriteStation
        Log.d("getdatatilbake()", "$station")
        if (antKeys != 5) { // kan lagre MAKS FEM favorittstasjoner -- antall elementer som kan legges til kan endres
            if (station != null && !inFavStations(station)) addToFavStations(station, stasjoner)
        }
    }

    private fun checkDeleteElem(){

        val args = arguments // station as Stasjon som skal slettes fra pref
        if (args != null) {
            val slettes: Stasjon? = args.getParcelable("station") as Stasjon?

            val keys: Map<String, *> = pref.getAll()
            for ((key, value) in keys) {
                Log.d("map values", key + ": " + value.toString())
                if (slettes?.name == value) {
                    editor.remove(key)
                    editor.commit()
                    antKeys--
                    favStations.remove(slettes)
                    favAdapter.notifyDataSetChanged()
                    break
                }
            }
        }
        else Log.d("bundle for slettes", "NULL")
    }


    //infoknapp
    private fun alertView(message: String, command: String) {
        val dialogB = AlertDialog.Builder(context)
        dialogB.setTitle("Hvordan fungerer det?")
            .setIcon(R.drawable.ic_info_green)
            .setMessage(message)
            .setPositiveButton("Lukk") { _, _ ->}
        slideShow(command, dialogB)
    }

    private fun slideShow(command: String, dialog: AlertDialog.Builder){
        val animasjonsDialog : AlertDialog = dialog.create()
        if (command == "open") animasjonsDialog.window?.attributes?.windowAnimations = R.style.DialogThOpen //animasjon
        else if (command == "close") animasjonsDialog.window?.attributes?.windowAnimations = R.style.DialogThClose //animasjon
        return (animasjonsDialog.show())
    }

    // info knapp
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.info_fav -> {
            alertView(getString(R.string.str_info_favorites), "open")
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.infoknapp_on_favorites_menu, menu)

    }

}