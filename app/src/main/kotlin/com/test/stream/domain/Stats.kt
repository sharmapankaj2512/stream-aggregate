package com.test.stream.domain

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.util.*

class Stats(private val timeWindow: TimeWindow, private val time: Time) {
    private var total: Int = 0
    private var sumX: Double = 0.0
    private var sumY: Long = 0L
    private var events: MutableList<Event> = mutableListOf()
    private val writes = Channel<EventRecordOperation>()
    private val reads = Channel<ReadStatsOperation>()
    private val cleanups = Channel<Channel<Unit>>()

    init {
        GlobalScope.launch {
            while (true) {
                select<Unit> {
                    writes.onReceive { operation ->
                        onEventsReceived(operation.events)
                        operation.response.send(true)
                    }
                    reads.onReceive { operation ->
                        val result = StatsResult(total, sumX, sumY)
                        operation.response.send(result)
                    }
                    cleanups.onReceive { operation ->
                        onCleanupTriggered()
                        operation.send(Unit)
                    }
                }
            }
        }
        GlobalScope.launch {
            while (true) {
                val response = Channel<Unit>()
                delay(1000)
                cleanups.send(response)
                response.receive()
            }
        }
    }

    suspend fun record(events: Set<Event>): Stats {
        val response = Channel<Boolean>()
        writes.send(EventRecordOperation(events, response))
        response.receive()
        return this
    }

    suspend fun read(): StatsResult {
        val response = Channel<StatsResult>()
        reads.send(ReadStatsOperation(response))
        return response.receive()
    }

    private fun onCleanupTriggered() {
        println("Clean up triggered")
        val allowedThreshold = time.now() - timeWindow.durationInMillis()
        val toBeRemoved = events.filter { it.occurredOn < allowedThreshold }
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

    data class EventRecordOperation(val events: Set<Event>, val response: Channel<Boolean>)
    data class ReadStatsOperation(val response: Channel<StatsResult>)
}
