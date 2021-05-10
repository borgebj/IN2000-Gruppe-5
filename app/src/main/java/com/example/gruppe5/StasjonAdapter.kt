package com.example.gruppe5

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.gruppe5.ui.favorites.FavoritesFragment
import com.example.gruppe5.ui.location.LocationFragment


class StasjonAdapter(private val liste: MutableList<Stasjon>) :
        RecyclerView.Adapter<StasjonAdapter.ViewHolder>(){

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textEn: TextView = view.findViewById(R.id.textEn)
        val textTo: TextView = view.findViewById(R.id.textTo)
        val star: ImageButton = view.findViewById(R.id.star_but)

    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.element, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.textEn.text = liste[position].name
        viewHolder.textTo.text = "Kommune: ${liste[position].kommune.name}"

        viewHolder.itemView.setOnClickListener{ //
            val fragment = LocationFragment()
            val bundle = Bundle()
            bundle.putParcelable("location", liste[position] )
            fragment.arguments = bundle

            Navigation.findNavController(it).navigate(R.id.navigation_location, bundle)
        }

        viewHolder.star.setOnClickListener {

            val fragment = FavoritesFragment()
            val bundle = Bundle()
            bundle.putParcelable("station", liste[position] )
            fragment.arguments = bundle

            Navigation.findNavController(it).navigate(R.id.navigation_favorites, bundle)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = liste.size
}
