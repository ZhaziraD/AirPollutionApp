package com.example.airpollutionapp.network

import com.example.airpollutionapp.models.PollutionResponse
import retrofit.Call
import retrofit.http.GET
import retrofit.http.Query

interface HistoricalService {
    @GET("2.5/air_pollution/history")
    fun getHistory(
            @Query("lat") lat: Double,
            @Query("lon") lon: Double,
            @Query("start") start: Int,
            @Query("end") end: Int,
            @Query("appid") appid: String
    ) : Call<PollutionResponse>
}

//http://api.openweathermap.org/data/2.5/air_pollution/history?lat=508&lon=50&start=1606223802&end=1606482999&appid=api_key
