package com.test.stream

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.util.*

class Stats(private val timeWindow: TimeWindow, private val time: Time) {
    private var total = 0
    private var treeMap = TreeMap<Long, Event>()
    private val eventChannel = Channel<Set<Event>>()
    private val cleanupChannel = Channel<Unit>()

    init {
        GlobalScope.launch {
            while (true) {
                select<Unit> {
                    eventChannel.onReceive { events ->
                        treeMap.putAll(events.map { it.occurredOn to it })
                        events.forEach { _ ->
                            total += 1
                        }
                    }
                    cleanupChannel.onReceive {
                        val toBeRemoved = treeMap.headMap(time.now() - timeWindow.durationInMillis())
                        toBeRemoved.forEach { _ -> total -= 1 }
                        toBeRemoved.clear()
                    }
                }
            }
        }
        GlobalScope.launch {
            delay(1000)
            cleanupChannel.send(Unit)
        }
    }

    fun total(): Int {
        return total
    }

    suspend fun record(events: Set<Event>): Stats {
        eventChannel.send(events)
        return this
    }
}
