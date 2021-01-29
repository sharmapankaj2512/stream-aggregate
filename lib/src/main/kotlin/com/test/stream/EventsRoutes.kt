package com.test.stream

import com.test.stream.domain.Event
import com.test.stream.domain.Stats
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.eventsRouting(stats: Stats) {
     route("/event") {
        post {
            val body = call.receive<String>()
            val events = body.split("\n").map { Event.from(it) }.toSet()
            stats.record(events)
            call.respond(HttpStatusCode.Accepted)
        }
    }
}