package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    val data: Data,
    val location: Location
)

@Serializable
data class Data(
    val time: String,
    val values: Values
)

@Serializable
data class Values(
    val temperature: Double,
    val cloudCover: Int?,
    val humidity: Int,
    val precipitationProbability: Int,
    val pressureSurfaceLevel: Double,
    val windSpeed: Double,
    val windDirection: Double,
    val weatherCode: Int
)

@Serializable
data class Location(
    val lat: Double,
    val lon: Double,
    val name: String
)
