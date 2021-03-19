package com.example.gruppe5.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.gruppe5.R
import org.w3c.dom.Text

class DashboardFragment : Fragment() {

  // ViewModel
  private lateinit var dashboardViewModel: DashboardViewModel

  // elementer
  lateinit var textView: TextView

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val root: View = inflater.inflate(R.layout.fragment_dashboard, container, false)

    //region [assign] assigner viewModel - DashBoardViewModel.kt
    dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
    textView = root.findViewById(R.id.text_dashboard)
    //endregion

    dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
      textView.text = it
    })

    return root
  }
}