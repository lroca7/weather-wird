// WeatherService.kt
package com.example.services

import com.example.model.WeatherResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


class WeatherService(private val apiKey: String) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getWeather(location: String): WeatherResponse? {
        return try {
            val response: HttpResponse = client.get("https://api.tomorrow.io/v4/weather/realtime") {
                parameter("location", location)
                parameter("apikey", apiKey)
            }
            response.body<WeatherResponse>()
        } catch (e: Exception) {
            println("Error fetching weather data: ${e.message}")
            null
        }
    }

    suspend fun getWeatherForMultipleLocations(locations: List<String>): Map<String, WeatherResponse?> {
        val results = mutableMapOf<String, WeatherResponse?>()

        for (location in locations) {
            results[location] = getWeather(location)
        }

        return results
    }
}
