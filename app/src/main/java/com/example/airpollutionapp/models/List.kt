package com.example.airpollutionapp.models

import java.io.Serializable

data class List (
    var components: Components?,
    var dt: Int?,
    val main: Main?
) : Serializable {


}
