package com.example.gruppe5.ui.location


import junit.framework.TestCase
import org.junit.Test

class LocationFragmentTest : TestCase() {
    var myObj = LocationFragment()
    @Test
    fun testColor() {
        val expected = 2131099861
        assertEquals(expected, myObj.setColor(3))
    }
    @Test
    fun testDangerLevel() {
        val expected = 2
        assertEquals(expected, myObj.calculateDangerLevel(68.00,"pm10"))
    }

}
