package com.example.airpollutionapp.models

import java.io.Serializable

data class PollutionResponse(
    val coord: Coord?,
    val list: kotlin.collections.List<List>?
) : Serializable