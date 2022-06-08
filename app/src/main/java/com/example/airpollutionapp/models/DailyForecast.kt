package com.example.airpollutionapp.models

import java.io.Serializable
import kotlin.collections.List

data class DailyForecast(
        val dt: String,
        val aqi: Int
) : Serializable