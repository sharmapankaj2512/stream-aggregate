package com.test.stream.domain

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.time.Clock
import java.time.Duration
import java.time.Instant

class Stats(private val reportDuration: Duration, private val clock: Clock) {
    private var total: Int = 0
    private var sumX: Double = 0.0
    private var sumY: Long = 0L
    private var events: MutableList<Event> = mutableListOf()

    private val writes = Channel<WriteOperation>(BUFFER_SIZE)
    private val reads = Channel<ReadOperation>(BUFFER_SIZE)
    private val cleanups = Channel<CleanupOperation>(BUFFER_SIZE)

    init {
        startStateChangeListener()
        triggerPeriodicCleanUp(Duration.ofSeconds(1))
    }

    suspend fun record(events: Set<Event>) {
        Channel<Unit>().apply {
            writes.send(WriteOperation(events, this))
        }.receive()
    }

    suspend fun read(): StatsResult {
        return Channel<StatsResult>().apply {
            reads.send(ReadOperation(this))
        }.receive()
    }

    private fun startStateChangeListener() {
        GlobalScope.launch {
            while (true) {
                select<Unit> {
                    writes.onReceive { operation ->
                        onEventsReceived(operation.events)
                        operation.response.send(Unit)
                    }
                    reads.onReceive { operation ->
                        val result = StatsResult(total, sumX, sumY)
                        operation.response.send(result)
                    }
                    cleanups.onReceive { operation ->
                        onCleanupTriggered()
                        operation.response.send(Unit)
                    }
                }
            }
        }
    }

    private fun triggerPeriodicCleanUp(duration: Duration) {
        GlobalScope.launch {
            while (true) {
                Channel<Unit>().apply {
                    delay(duration.toMillis())
                    cleanups.send(CleanupOperation(this))
                }.receive()
            }
        }
    }

    private fun onCleanupTriggered() {
        println("Clean up triggered")
        val allowedThreshold = clock.instant().minusMillis(reportDuration.toMillis())
        val toBeRemoved = events.filter { Instant.ofEpochMilli(it.occurredOn).isBefore(allowedThreshold) }
        toBeRemoved.forEach { event ->
            total -= 1
            sumX -= event.x
            sumY -= event.y
        }
        events.removeAll(toBeRemoved)
    }

    private fun onEventsReceived(events: Set<Event>) {
        this.events.addAll(events)
        events.forEach { event ->
            total += 1
            sumX += event.x
            sumY += event.y
        }
    }

    data class WriteOperation(val events: Set<Event>, val response: Channel<Unit>)
    data class ReadOperation(val response: Channel<StatsResult>)
    data class CleanupOperation(val response: Channel<Unit>)

    companion object {
        private const val BUFFER_SIZE = 1024
    }
}
