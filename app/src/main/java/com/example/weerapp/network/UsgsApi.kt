package com.example.weerapp.network

import retrofit2.http.GET

data class UsgsResponse(val features: List<UsgsFeature>)
data class UsgsFeature(val properties: UsgsProps)
data class UsgsProps(val mag: Double?, val place: String?)

interface UsgsApi {
    @GET("earthquakes/feed/v1.0/summary/all_day.geojson")
    suspend fun allDay(): UsgsResponse
}
