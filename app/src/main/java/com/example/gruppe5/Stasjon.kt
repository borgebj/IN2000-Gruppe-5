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
    ) {
        override fun toString(): String {
            return "[$eoi]$name - h($height)l($longitude)"
        }
    }

    data class Grunnkets(
        val name: String,
        val areacode: Int
    ) { override fun toString(): String {
        return "[$areacode]$name"
    } }

    data class Delomrade(
        val name: String,
        val areacode: Int
    )  { override fun toString(): String {
        return "[$areacode]$name"
    } }

    data class Kommune(
        val name: String,
        val areacode: Int
    ) { override fun toString(): String {
            return "[$areacode]$name"
        } }

    override fun toString(): String {
        return "test"
    }
}