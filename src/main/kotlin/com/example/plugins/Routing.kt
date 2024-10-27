package com.example.plugins

import com.example.model.*
import com.example.services.RedisClient
import com.example.services.WeatherService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import redis.clients.jedis.Jedis

fun Application.configureRouting() {

    routing {

        staticResources("static", "static")

        val apiKey = environment.config.property("ktor.application.apiKey").getString()
        val weatherService = WeatherService(apiKey)

        get("/") {
            call.respondText("Hello Liz!")
        }

        routing {

            get("/weather/{location}") {
                val location = call.parameters["location"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Location parameter is missing")

                // Conectar a Redis
                val redisClient = RedisClient

                // Obtener el valor guardado en Redis
                val weatherDataJson = redisClient.get(location)

                if (weatherDataJson != null) {
                    // Deserializar el JSON en un objeto de tipo Data
                    val weatherData: WeatherResponse? = try {
                        Json.decodeFromString(weatherDataJson)
                    } catch (e: Exception) {
                        println("Error deserializing JSON: ${e.message}")
                        null
                    }

                    if (weatherData != null) {
                        // Devolver la respuesta
                        call.respond(HttpStatusCode.OK, weatherData)
                    } else {
                        // Responder con un error si la deserialización falló
                        call.respond(HttpStatusCode.InternalServerError, "Failed to deserialize weather data")
                    }

                } else {
                    // Responder con un error si no se encuentra la localidad
                    call.respond(HttpStatusCode.NotFound, "Weather data for location '$location' not found")
                }

            }

            get("/weather/multiple") {
                val locations = listOf("CL", "CH", "NZ", "AU", "UK", "USA")
                val weatherData = weatherService.getWeatherForMultipleLocations(locations)

                call.respond(weatherData)
            }
        }

        routing {
            get("/test-redis") {
                RedisClient.set("testKey", "Hello Liz!")
                val value = RedisClient.get("testKey")
                call.respondText("Redis value: $value")
            }
        }
    }
}
