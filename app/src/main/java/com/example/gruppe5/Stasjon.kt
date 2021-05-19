package com.example.gruppe5

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class Stasjon(
    @SerializedName(
        value = "name",
        alternate = ["station"]
    ) // hvis navn ikke finnes, velger den station - for NILU api
    var name: String,
    val eoi: String, // felles
    val height: Double,
    val longitude: Double, // felles
    val latitude: Double, // felles
    val grunnkrets: Grunnkets,
    val delomrade: Delomrade,
    val kommune: Kommune,
    var verdier: HashMap<String, Double>
) : Parcelable,
    Serializable // key:type : value:verdi -> (o3, pm10, pm25, no2)
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
) : Parcelable {
    override fun toString(): String {
        return "Grunnkrets:[$areacode]$name"
    }
}

@Parcelize
data class Delomrade(
    val name: String,
    val areacode: Int
) : Parcelable {
    override fun toString(): String {
        return "Delomrade:[$areacode]$name"
    }
}

@Parcelize
data class Kommune(
    val name: String,
    val areacode: Int
) : Parcelable {
    override fun toString(): String {
        return "Kommune:[$areacode]$name"
    }
}

