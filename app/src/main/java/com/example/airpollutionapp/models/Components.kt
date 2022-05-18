package com.example.airpollutionapp.models

import java.io.Serializable

data class Components(
    val co: Double? = null,
    val nh3: Double? = null,
    val no: Double? = null,
    val no2: Double? = null,
    val o3: Double? = null,
    val pm10: Double? = null,
    val pm25: Double? = null,
    val so2: Double? = null
) : Serializable