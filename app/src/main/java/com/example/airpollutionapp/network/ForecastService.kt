package com.example.airpollutionapp.network

import com.example.airpollutionapp.models.PollutionResponse
import retrofit.Call
import retrofit.http.GET
import retrofit.http.Query

interface ForecastService {
    @GET("2.5/air_pollution/forecast")
    fun getForecast(
            @Query("lat") lat: Double,
            @Query("lon") lon: Double,
            @Query("appid") appid: String
    ) : Call<PollutionResponse>
}

//https://api.openweathermap.org/data/2.5/air_pollution/forecast?lat=50&lon=50&appid=fa88f55815a1fec406c195a31ed90158
