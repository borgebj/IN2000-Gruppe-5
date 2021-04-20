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
                   val kommune: Kommune,
                   var verdier : HashMap<String, Double>) : Parcelable // key:type : value:verdi -> (o3, pm10, pm25, no2)
{
    override fun toString(): String {
        return "[$eoi] $name [Lat($latitude) Lng($longitude)] - - Verdier[${verdier}]"
    }
}
@Parcelize
// indre-objekter / klasser
data class Grunnkets(
        val name: String,
        val areacode: Int
): Parcelable
{ override fun toString(): String {
    return "Grunnkrets:[$areacode]$name"
} }

@Parcelize
data class Delomrade(
        val name: String,
        val areacode: Int
): Parcelable
{ override fun toString(): String {
    return "Delomrade:[$areacode]$name"
} }

@Parcelize
data class Kommune(
        val name: String,
        val areacode: Int
): Parcelable
{ override fun toString(): String {
    return "Kommune:[$areacode]$name"
} }
