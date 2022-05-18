package com.example.airpollutionapp.models

import android.content.Context
import java.io.Serializable

data class Coord(
        var lat: Double? = null,
        var lon: Double? = null
) : Serializable {

    fun getLat(): Double {
        return lat!!
    }

    fun setLat(lat: Double) {
        this.lat = lat
    }

    fun getLon(): Double {
        return lon!!
    }

    fun setLon(lon: Double) {
        this.lon = lon
    }

}