package com.example.gruppe5.ui.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gruppe5.Stasjon

class FavoritesViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    private val _text = MutableLiveData<String>().apply {
        value = "Favoritt-fragment"
    }
    val text: LiveData<String> = _text

    private val _fav_stations = MutableLiveData<List<Stasjon>>().apply {
        getFavStations()
    }

    var fav_stations : LiveData<List<Stasjon>> = _fav_stations

    fun getFavStations() : LiveData<List<Stasjon>> {
        return fav_stations
    }

    fun addFavStation(station : Stasjon){
        //fav_stations.add(station)
    }

}