package com.example.gruppe5

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    lateinit var navView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        assignId()

        // setter opp navbar
        navView.setupWithNavController(findNavController(R.id.nav_host_fragment))
    }

    fun assignId() {
        navView = findViewById(R.id.nav_view)
    }

    // for aa kunne sende data fra SearchFragment til FavoriteFragment -- fav sitter fast i skjermen etter dette kallet
    /*override fun passDataCom(editTextInput: String) {
        val bundle = Bundle()
        bundle.putString("inputText", editTextInput)
        val transaction = this.supportFragmentManager.beginTransaction()
        val fav = FavoritesFragment()
        fav.arguments = bundle
        transaction.replace(R.id.nav_host_fragment, fav) // tilbake til FavoriteFragment
        .addToBackStack(null) // kunne gaa tilbeke til siden brukeren sist var i
        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        .commit()
        //navView.setupWithNavController(findNavController(R.id.nav_host_fragment)) // maa knytte til navcontroller!!!!!!!!!!!!!!!!!!!!!

    }*/
}
