package com.example.gruppe5.ui.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BulletSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.gruppe5.R


class AboutAppFragment : Fragment() {

    lateinit var textView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root: View = inflater.inflate(R.layout.fragment_about_app, container, false)


        assignId(root)
        setText()

        return root
    }

    fun assignId(root: View) {
        textView = root.findViewById(R.id.infoApp)
    }

    private fun setText() {
        val bulletedList = listOf(
            "God luftkvalitet er viktig for å bevare god helse.",
            "Det finnes mange forskjellige luftforurensningskomponenter, inkludert ulike typer svevestøv og gasser, som kan gi uønskede helseeffekter.",
            "Informasjonen er hentet fra: https://www.fhi.no/nettpub/luftkvalitet/sammendrag-og-bakgrunnsinformasjon/hva-mener-vi-med-luftkvalitetskriterier/"
        )
            .toBulletedList()
        textView.text = bulletedList
    }

    @SuppressLint("ResourceAsColor")
    fun List<String>.toBulletedList(): CharSequence {
        return SpannableString(this.joinToString("\n")).apply {
            this@toBulletedList.foldIndexed(0) { index, acc, span ->
                val end = acc + span.length + if (index != this@toBulletedList.size - 1) 1 else 0
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    this.setSpan(BulletSpan(40, R.color.teal_700,40), acc, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                end
            }
        }
    }
}