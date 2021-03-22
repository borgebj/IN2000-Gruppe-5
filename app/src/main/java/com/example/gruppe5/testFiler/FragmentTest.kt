package com.example.gruppe5.testFiler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.gruppe5.R

class FragmentTest : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_test)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarFragment).title = "Fragment-test"
    }
}