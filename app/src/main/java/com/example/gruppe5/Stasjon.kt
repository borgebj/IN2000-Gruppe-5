package com.example.gruppe5

class Stasjon {

    data class StasjonInfo(
    val name: String,
    val eoi: String,
    val height: Int,
    val longitude: Double,
    val grunnkrets: Grunnkets,
    val delomrade: Delomrade,
    val kommune: Kommune
    )

    data class Grunnkets(
        val name: String,
        val areacode: Int
    )

    data class Delomrade(
        val name: String,
        val areacode: Int
    )

    data class Kommune(
        val name: String,
        val areacode: Int
    )
}
