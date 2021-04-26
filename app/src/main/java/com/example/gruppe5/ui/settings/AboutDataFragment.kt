package com.example.gruppe5.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.gruppe5.R


class AboutDataFragment : Fragment() {

    lateinit var textView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root: View = inflater.inflate(R.layout.fragment_about_data, container, false)

        assignId(root)

        return root
    }

    fun assignId(root: View) {
        textView = root.findViewById(R.id.infoData)
    }
}