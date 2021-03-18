package com.example.gruppe5

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StasjonAdapter(private val liste: MutableList<Stasjon>) :
        RecyclerView.Adapter<StasjonAdapter.ViewHolder>(){

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textEn: TextView = view.findViewById(R.id.textEn)
        val textTo: TextView = view.findViewById(R.id.textTo)
        var textTre: TextView = view.findViewById(R.id.textTre)
        var textFire: TextView = view.findViewById(R.id.textFire)
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.element, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.textEn.text = "Name: ${liste[position].name}"
        viewHolder.textTo.text = "eoi: [${liste[position].eoi}]"
        viewHolder.textTre.text = "Height: ${liste[position].height}"
        viewHolder.textFire.text = "Longitude: ${liste[position].longitude}"
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = liste.size
}