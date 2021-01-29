package com.test.stream

import com.test.stream.domain.Stats
import com.test.stream.domain.Time
import com.test.stream.domain.TimeWindow
import com.test.stream.infrastructure.eventsRouting
import com.test.stream.infrastructure.statsRouting
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.time.Instant

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        json()
    }

    registerRoutes(Stats(TimeWindow.Minute, EpochTime()))
}

fun Application.registerRoutes(stats: Stats) {
    routing {
        eventsRouting(stats)
        statsRouting(stats)
    }
}

fun main() {
    embeddedServer(
        factory = Netty,
        port = 8080,
        watchPaths = listOf("StatsAppKt"),
        module = Application::main)
        .start(true)
}

class EpochTime: Time {
    override fun now(): Long {
        return Instant.now().toEpochMilli()
    }
}
