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

    private lateinit var textView: TextView

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
            "\nLuft, mat og vann er essensielt for menneskers eksistens, og behovet for god luft er konstant. All forskning viser at inhalering av forurenset luft er dårlig for helsa, og for utsatte grupper kan svært usunn luft være livsfarlig.",
            "\nGod luftkvalitet er viktig for å bevare god helse, og denne appen skal bidra med å skape forståelse for luftkvalitetens effekt på egen helse, og ",
            "gjøre det mulig å enkelt avgjøre om det er sunt og trygt å bevege seg utendørs i området man befinner seg i, eller skal oppsøke.",
            "\nI Norge er luftkvaliteten generelt tilfredsstillende, men utsatte grupper anbefales å ta hensyn i perioder med dårlig luftkvalitet.",
            "\nDenne appen er for alle. Både de med luftveisproblemer, de som syns luftkvalitet er interessant, og de som bare har lyst til å unngå byens mest forurensede områder."
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
                    this.setSpan(
                        BulletSpan(40, R.color.teal_700, 40),
                        acc,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                end
            }
        }
    }
}