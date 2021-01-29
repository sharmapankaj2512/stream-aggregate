package com.test.stream.infrastructure

import com.test.stream.domain.Stats
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.statsRouting(stats: Stats) {
    route("/stats") {
        get {
            call.respond(stats.read().toString())
        }
    }
}