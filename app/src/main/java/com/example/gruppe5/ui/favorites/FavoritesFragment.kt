package com.example.gruppe5.ui.favorites

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gruppe5.R
import com.example.gruppe5.Stasjon
import com.example.gruppe5.StasjonAdapter
import com.example.gruppe5.ViewModel


@Suppress("UsePropertyAccessSyntax")
class FavoritesFragment : Fragment() {

    // globale variabler
    private val viewModel: ViewModel by activityViewModels() // <- bruker EN felles viewmodel

    private lateinit var addBut: ImageButton
    private lateinit var resetB: ImageButton
    private lateinit var favRecycler: RecyclerView
    private lateinit var favAdapter: StasjonAdapter

    private lateinit var root: View

    private var favStations: MutableList<Stasjon> = mutableListOf()
    private lateinit var pref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private var antKeys = 0 // antall lagrede favorittstasjoner - maks fem


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
        assignId(root)
        setHasOptionsMenu(true)
        addAdapter()

        setResetBut(root)
        setSearchFrag(root)

        viewModel.stations.observe(viewLifecycleOwner, {
            favStations.clear()
            setFavStations(it)
            handleArgs(it)
        })
    }

    @SuppressLint("CommitPrefEdits")
    fun assignId(root: View) {

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
    fun setResetBut(root: View) {
        resetB.setOnClickListener {
            editor.clear().commit()
            favStations = mutableListOf()
            antKeys = 0
            favAdapter.notifyDataSetChanged()
            refresh(root)
        }
    }

    private fun refresh(root: View) {
        root.findNavController().navigate(
            FavoritesFragmentDirections.actionNavigationFavoritesSelf()
        )
    }

    private fun setSearchFrag(root: View) {

        addBut.setOnClickListener {
            tilSearch(root)  // navigere til SearchFragment
            val msg =
                "Tøm listen over favorittstasjoner eller slett noen stasjoner for å legge til en ny favoritt."
            if (antKeys == 5) Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    // naviger til SearchFragment
    private fun tilSearch(root: View) {
        root.findNavController()
            .navigate(FavoritesFragmentDirections.actionNavigationFavoritesToNavigationSearch2())
    }

    // legger til stasjonene i preference til MutableList favStations
    private fun setFavStations(stasjoner: MutableList<Stasjon>) {

        val keys: Map<String, *> = pref.getAll()
        for ((_, value) in keys) {
            addToFavStations(value as String, stasjoner)
        }
    }
    // legg til station til MutableList, stasjoner
    private fun addToFavStations(station: String, stasjoner: MutableList<Stasjon>) {

        for (st in stasjoner) {
            if (st.name.equals(station, ignoreCase = true)) { // finner match

                if (!inFavStations(station)) { // ikke satt til CardView ennaa
                    favStations.add(st)
                    favAdapter.notifyDataSetChanged()

                    if (!inPref(station)) { // ikke satt til pref ennaa = ny favorittby
                        setElem(station, station)
                        antKeys = pref.all.size
                    }
                }
                break
            }
        }
    }
    // return true hvis station er i pref
    private fun inPref(station: String): Boolean {

        val keys: Map<String, *> = pref.getAll()
        for ((_, value) in keys) {
            if (station == value) return true
        }
        return false
    }

    // return true hvis station er i favStation
    private fun inFavStations(station: String): Boolean {

        for (st in favStations) {
            if (st.name == station) return true
        }
        return false
    }

    // legger til en stasjon til preferences
    @SuppressLint("CommitPrefEdits")
    fun setElem(station: String, key: String) {
        editor.putString(key, station)
        editor.commit()
    }

    // Håndterer både add og delete via favorite_station argumentet
    private fun handleArgs(stasjoner: MutableList<Stasjon>) {
        val args = FavoritesFragmentArgs.fromBundle(requireArguments())
        val stationName: String? = args.favoriteStation
        if (stationName.isNullOrEmpty()) return

        if (inPref(stationName)) {
            // Slett hvis den finnes
            val keys = pref.all
            for ((key, value) in keys) {
                if (value == stationName) {
                    editor.remove(key).commit()
                    antKeys = pref.all.size

                    val iterator = favStations.iterator()
                    while (iterator.hasNext()) {
                        if (iterator.next().name.equals(stationName, ignoreCase = true)) {
                            iterator.remove()
                            break
                        }
                    }
                    favAdapter.notifyDataSetChanged()
                    break
                }
            }
        } else if (antKeys < 5) {
            // Legg til hvis den ikke finnes og vi har plass
            addToFavStations(stationName, stasjoner)
        } else {
            val msg = "Maksimum 5 favoritter tillatt. Slett en for å legge til en ny."
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
        // Null ut argumentet så det ikke trigges igjen på f.eks. rotasjon
        val newArgs = Bundle(requireArguments())
        newArgs.putString("favorite_station", null)
        arguments = newArgs
    }

    //infoknapp
    private fun alertView(message: String, command: String) {
        val dialogB = AlertDialog.Builder(context)
        dialogB.setTitle("Hvordan fungerer det?")
            .setIcon(R.drawable.ic_info_green)
            .setMessage(message)
            .setPositiveButton("Lukk") { _, _ -> }
        slideShow(command, dialogB)
    }

    private fun slideShow(command: String, dialog: AlertDialog.Builder) {
        val animasjonsDialog: AlertDialog = dialog.create()
        if (command == "open") animasjonsDialog.window?.attributes?.windowAnimations =
            R.style.DialogThOpen //animasjon
        else if (command == "close") animasjonsDialog.window?.attributes?.windowAnimations =
            R.style.DialogThClose //animasjon
        return (animasjonsDialog.show())
    }

    // infoknapp på toolbar
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.info_fav -> {
            alertView(getString(R.string.str_info_favorites), "open")
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.infoknapp_on_favorites_menu, menu)
    }
}