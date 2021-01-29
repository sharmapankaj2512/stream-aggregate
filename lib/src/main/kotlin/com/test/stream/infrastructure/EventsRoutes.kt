package com.test.stream.infrastructure

import com.test.stream.domain.Event
import com.test.stream.domain.Stats
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.lang.IndexOutOfBoundsException
import java.lang.NumberFormatException

fun Route.eventsRouting(stats: Stats) {
     route("/event") {
        post {
            try {
                val body = call.receive<String>()
                val events = makeEvents(body)
                stats.record(events)
                call.respond(HttpStatusCode.Accepted)
            } catch (ex: NumberFormatException) {
                call.respond(HttpStatusCode.BadRequest)
            } catch (ex: IndexOutOfBoundsException) {
                call.respond(HttpStatusCode.BadRequest)
            } catch (ex: Throwable) {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}

private fun makeEvents(body: String): Set<Event> {
    return body.split("\n").map {
        val cells = it.split(",")
        Event(cells[0].toLong(), cells[1].toDouble(), cells[2].toLong())
    }.toSet()
}