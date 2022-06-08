package com.example.airpollutionapp.models

import java.io.Serializable

data class Components(
    val no2: Double? = null,
    val o3: Double? = null,
    var pm10: Double? = null,
    val pm2_5: Double? = null,
    val so2: Double? = null
) : Serializable