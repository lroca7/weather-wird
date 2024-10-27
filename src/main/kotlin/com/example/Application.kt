package com.example

import com.example.plugins.*
import com.example.services.RedisClient
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {


    RedisClient.initialize(environment)

    configureSerialization()
    configureRouting()


}
