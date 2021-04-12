package com.example.gruppe5.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// skal inneholde logikk

class MapViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Kart-fragment"
    }
    val text: LiveData<String> = _text

}