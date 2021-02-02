package com.test.stream.infrastructure

import com.test.stream.domain.Event
import com.test.stream.domain.Stats
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*

fun Route.eventsRouting(stats: Stats) {
    route("/events") {
        post {
            try {
                val body = call.receive<String>()
                val events = makeEvents(body)
                val validEvents = events.mapNotNull { it.getOrNull() }
                val invalidEvents = events.mapNotNull { it.exceptionOrNull() }.toList()
                stats.record(validEvents.toSet())
                respond(invalidEvents)
            } catch (ex: Throwable) {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.respond(
    invalidEvents: List<Throwable>
) = when {
    invalidEvents.isEmpty() ->
        call.respond(HttpStatusCode.Accepted)
    else ->
        call.respond(HttpStatusCode.BadRequest)
}


private fun makeEvents(body: String): Sequence<Result<Event>> {
    return body.lineSequence().map { line ->
        runCatching {
            line.split(",", limit = 3).let { (occurredOn, x, y) ->
                Event(occurredOn.toLong(), x.toDouble(), y.toLong())
            }
        }
    }
}
