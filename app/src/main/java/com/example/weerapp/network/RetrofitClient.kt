package com.example.weerapp.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    internal val client = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        )
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val openWeatherApi: OpenWeatherApi =
        retrofit.create(OpenWeatherApi::class.java)

    private val usgsRetrofit = Retrofit.Builder()
        .baseUrl("https://earthquake.usgs.gov/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val usgsApi: UsgsApi = usgsRetrofit.create(UsgsApi::class.java)

    fun clientForRawCalls(): OkHttpClient = client
}
