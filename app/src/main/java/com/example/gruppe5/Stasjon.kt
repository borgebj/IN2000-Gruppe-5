package com.example.gruppe5

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Stasjon(val name: String,
                   val eoi: String,
                   val height: Double,
                   val longitude: Double,
                   val latitude: Double,
                   val grunnkrets: Grunnkets,
                   val delomrade: Delomrade,
                   val kommune: Kommune) : Parcelable
{
    override fun toString(): String {
        return "[$eoi] $name [H($height) L($longitude)]"
    }
}
@Parcelize
// indre-objekter / klasser
data class Grunnkets(
        val name: String,
        val areacode: Int
): Parcelable
{ override fun toString(): String {
    return "[$areacode]$name"
} }

@Parcelize
data class Delomrade(
        val name: String,
        val areacode: Int
): Parcelable
{ override fun toString(): String {
    return "[$areacode]$name"
} }

@Parcelize
data class Kommune(
        val name: String,
        val areacode: Int
): Parcelable
{ override fun toString(): String {
    return "[$areacode]$name"
} }

data class StasjonListe(val list: List<Stasjon>)