package com.example.airpollutionapp.models

import java.io.Serializable

data class List (
    val components: Components?,
    var dt: Int?,
    val main: Main?
) : Serializable {
    fun getDt(): Int {
        return dt!!
    }

    fun setDtt(dt: Int) {
        this.dt = dt
    }
}
