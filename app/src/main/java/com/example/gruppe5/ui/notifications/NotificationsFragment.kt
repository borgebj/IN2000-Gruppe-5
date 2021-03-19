package com.example.gruppe5.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.gruppe5.R

class NotificationsFragment : Fragment() {

  // ViewModel
  private lateinit var notificationsViewModel: NotificationsViewModel

  // elementer
  lateinit var textView: TextView

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val root = inflater.inflate(R.layout.fragment_notifications, container, false)

    //region [assign] assigner viewModel - NotificationViewModel.kt
    notificationsViewModel = ViewModelProvider(this).get(NotificationsViewModel::class.java)
    textView = root.findViewById(R.id.text_notifications)
    //endregion

    notificationsViewModel.text.observe(viewLifecycleOwner, Observer {
      textView.text = it
    })

    return root
  }
}