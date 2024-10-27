package com.example.services

import io.ktor.server.application.ApplicationEnvironment
import redis.clients.jedis.Jedis

object RedisClient {
    private lateinit var jedis: Jedis

    fun initialize(environment: ApplicationEnvironment) {
        val redisUrl = environment.config.property("ktor.application.redis.url").getString()
        val redisPassword = environment.config.property("ktor.application.redis.password").getString()

        jedis = Jedis(redisUrl).apply {
            if (redisPassword.isNotEmpty()) auth(redisPassword)
        }
    }

    fun set(key: String, value: String) {
        jedis.set(key, value)
    }

    fun get(key: String): String? {
        return jedis.get(key)
    }

    fun lpush(key: String, value: String) {
        jedis.lpush(key, value)
    }
}
