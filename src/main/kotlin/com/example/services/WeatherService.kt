// WeatherService.kt
package com.example.services

import com.example.model.WeatherResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.random.Random

@Serializable
data class WeatherStorage(
    val location: String,
    val timestamp: Long,
    val weatherData: WeatherResponse?
)

class WeatherService(private val apiKey: String) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    private val redisClient = RedisClient

    suspend fun getWeather(location: String): WeatherResponse? {
        return try {
            val response: HttpResponse = client.get("https://api.tomorrow.io/v4/weather/realtime") {
                parameter("location", location)
                parameter("apikey", apiKey)
            }

            // Verificar el código de estado de la respuesta
            if (response.status.value == 429) {
                throw Exception("Error 429: Limite de solicitudes alcanzado. Intente más tarde.")
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



    suspend fun getWeatherWithRetry(location: String): WeatherResponse? {
        var attempt = 0
        val maxAttempts = 3
//        val delayDuration = 3600000L
        val delayDuration = 30000L

        while (attempt < maxAttempts) {
            try {
                // Simulación de fallo del 20%
                if (Random.nextDouble() < 0.2) throw Exception("The API Request Failed")

                val weatherData = getWeather(location)


                return weatherData
            } catch (e: Exception) {
                attempt++
                println("Intento $attempt fallido para $location: ${e.message}")

                redisClient.lpush("weather_errors", "Error fetching $location at ${Date()}: ${e.message}")

                // Verificar si el error es 429
                if (e.message?.contains("429") == true) {
                    println("Error 429: Debe esperar una hora antes de volver a intentar...")
                    delay(delayDuration) // Esperar antes de intentar nuevamente
                    return null
                }

                if (attempt == maxAttempts) return null
            }
        }
        return null
    }

    // Consulta y almacena datos para múltiples localizaciones
    suspend fun updateWeatherData(locations: List<String>) {
        locations.forEach { location ->
            val weatherData = getWeatherWithRetry(location)


            // Mostrar en consola lo que se va a guardar en Redis
            println("Guardando datos en Redis para la localidad: $location")
            // println("Datos: $weatherData")

            // Serializar a JSON
            val jsonData = Json.encodeToString(weatherData)


            weatherData?.let {
                redisClient.set(location, jsonData)
            }
        }
    }

    init {
        // Configuración del timer para actualizar cada 5 minutos
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runBlocking {
                    val locations = listOf("UK", "USA")
                    updateWeatherData(locations)
                }
            }
        }, 0, 15 * 60 * 1000)
    }
}
