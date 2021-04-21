package com.example.gruppe5.ui.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannableString
import android.text.style.BulletSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.gruppe5.R


class AboutAirqualityFragment : Fragment() {

    lateinit var textView : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root : View = inflater.inflate(R.layout.search_fragment, container, false)


        assignId(root)
        setText(root)



        return root
    }

    fun assignId(root: View) {
        textView = root.findViewById(R.id.infoAirQuality)
    }

    fun setText(root: View) {
        val bulletedList = listOf(
            "God luftkvalitet er viktig for å bevare god helse.",
            "Det finnes mange forskjellige luftforurensningskomponenter, inkludert ulike typer svevestøv og gasser, som kan gi uønskede helseeffekter.",
            "Luftkvalitet er bra")
            .toBulletedList()
        textView.text = bulletedList
    }

    @SuppressLint("ResourceAsColor")
    fun List<String>.toBulletedList(): CharSequence {
        return SpannableString(this.joinToString("\n")).apply {
            this@toBulletedList.foldIndexed(0) { index, acc, span ->
                val end = acc + span.length + if (index != this@toBulletedList.size - 1) 1 else 0
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    this.setSpan(BulletSpan(16, R.color.teal_700, 20), acc, end, 0)
                }
                end
            }
        }
    }
}




