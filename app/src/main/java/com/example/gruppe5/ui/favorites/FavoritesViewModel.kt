package com.example.gruppe5.ui.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FavoritesViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    private val _text = MutableLiveData<String>().apply {
        value = "Dette er favoritt-siden"
    }
    val text: LiveData<String> = _text

}