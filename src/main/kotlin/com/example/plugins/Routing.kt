package com.example.plugins

import com.example.model.*
import com.example.services.WeatherService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {

        staticResources("static", "static")

        val apiKey = environment.config.property("ktor.application.apiKey").getString()
        val weatherService = WeatherService(apiKey)

        get("/") {
            call.respondText("Hello Liz!")
        }

        //updated implementation
        route("/tasks") {
            get {
                val tasks = TaskRepository.allTasks()
                call.respond(tasks)
            }

            get("/byName/{taskName}") {
                val name = call.parameters["taskName"]
                if (name == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                val task = TaskRepository.taskByName(name)
                if (task == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                call.respond(task)
            }
            get("/byPriority/{priority}") {
                val priorityAsText = call.parameters["priority"]
                if (priorityAsText == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    val priority = Priority.valueOf(priorityAsText)
                    val tasks = TaskRepository.tasksByPriority(priority)

                    if (tasks.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(tasks)
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }


        routing {
            get("/weather/{location}") {
                val location = call.parameters["location"] ?: return@get call.respondText("Location not provided", status = io.ktor.http.HttpStatusCode.BadRequest)

                val weatherData = weatherService.getWeather(location)
                if (weatherData != null) {
                    call.respond(weatherData)
                } else {
                    call.respondText("Unable to fetch weather data", status = io.ktor.http.HttpStatusCode.ServiceUnavailable)
                }
            }

            get("/weather/multiple") {
                val locations = listOf("CL", "CH", "NZ", "AU", "UK", "USA")
                val weatherData = weatherService.getWeatherForMultipleLocations(locations)

                call.respond(weatherData)
            }
        }


    }
}
