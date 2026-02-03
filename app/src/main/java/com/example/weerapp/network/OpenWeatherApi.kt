package com.example.weerapp.network

import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherResponse(
    val name: String,
    val main: Main,
    val wind: Wind,
    val weather: List<WeatherDesc>
)

data class Main(val temp: Double, val humidity: Int)
data class Wind(val speed: Double)
data class WeatherDesc(val description: String)

interface OpenWeatherApi {
    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "nl"
    ): WeatherResponse
}
