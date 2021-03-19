package com.example.gruppe5

data class Stasjon(
        val name: String,
        val eoi: String,
        val height: Double,
        val longitude: Double,
        val latitude: Double,
        val grunnkrets: Grunnkets,
        val delomrade: Delomrade,
        val kommune: Kommune
        )
{
    override fun toString(): String {
        return "[$eoi] $name [H($height) L($longitude)]"
    }
}

// indre-objekter / klasser
data class Grunnkets(
        val name: String,
        val areacode: Int
)
{ override fun toString(): String {
    return "[$areacode]$name"
} }

data class Delomrade(
        val name: String,
        val areacode: Int
)
{ override fun toString(): String {
    return "[$areacode]$name"
} }

data class Kommune(
        val name: String,
        val areacode: Int
)
{ override fun toString(): String {
    return "[$areacode]$name"
} }

data class StasjonListe(val list: List<Stasjon>)