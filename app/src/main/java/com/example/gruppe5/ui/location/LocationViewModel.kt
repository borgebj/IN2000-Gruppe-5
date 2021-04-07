<<<<<<< Updated upstream:app/src/main/java/com/example/gruppe5/ui/location/LocationViewModel.kt
package com.example.gruppe5.ui.location
=======
package com.example.gruppe5.ui.search
>>>>>>> Stashed changes:app/src/main/java/com/example/gruppe5/ui/search/SearchViewModel.kt

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

<<<<<<< Updated upstream:app/src/main/java/com/example/gruppe5/ui/location/LocationViewModel.kt
class LocationViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Location-fragment"
=======
class SearchViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    private val _text = MutableLiveData<String>().apply {
        value = "Search-fragment"
>>>>>>> Stashed changes:app/src/main/java/com/example/gruppe5/ui/search/SearchViewModel.kt
    }
    val text: LiveData<String> = _text
}