package com.example.airpollutionapp.network

import com.example.airpollutionapp.models.PollutionResponse
import retrofit.Call
import retrofit.http.GET
import retrofit.http.Query

interface PollutionService {
    @GET("2.5/air_pollution")
    fun getPollution(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appid: String
    ) : Call<PollutionResponse>
}

//https://api.openweathermap.org/data/2.5/air_pollution?lat=50&lon=50&appid=api_key
