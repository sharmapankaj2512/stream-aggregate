package com.test.stream

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
    private var treeMap = TreeMap<Long, Event>()
    private val writes = Channel<EventRecordOperation>()
    private val reads = Channel<ReadStatsOperation>()
    private val cleanups = Channel<Unit>()

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
                    cleanups.onReceive {
                        onCleanupTriggered()
                    }
                }
            }
        }
        GlobalScope.launch {
            delay(1000)
            cleanups.send(Unit)
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
        val toBeRemoved = treeMap.headMap(time.now() - timeWindow.durationInMillis())
        toBeRemoved.forEach { entry ->
            total -= 1
            sumX -= entry.value.x
            sumY -= entry.value.y
        }
        toBeRemoved.clear()
    }

    private fun onEventsReceived(events: Set<Event>) {
        treeMap.putAll(events.map { it.occurredOn to it })
        events.forEach { event ->
            total += 1
            sumX += event.x
            sumY += event.y
        }
    }

    data class EventRecordOperation(val events: Set<Event>, val response: Channel<Boolean>)
    data class ReadStatsOperation(val response: Channel<StatsResult>)
}
