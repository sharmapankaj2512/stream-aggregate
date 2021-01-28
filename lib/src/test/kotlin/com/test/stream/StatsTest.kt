package com.test.stream

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class StatsTest {
    class MockTime: Time {
        override fun now(): Long {
//            return Instant.EPOCH.toEpochMilli()
            return 1580203689000
        }
    }

    @ParameterizedTest
    @MethodSource("eventSource")
    fun testRow(events: Set<Event>, expectedTotal: Int) {
        runBlocking {
            val stats = Stats(TimeWindow.Minute, MockTime()).record(events)

            delay(2000)

            assertEquals(expectedTotal, stats.total())
        }
    }

    companion object {
        @JvmStatic
        fun eventSource(): List<Arguments> {
            return listOf(
                Arguments.of(setOf<Event>(), 0),
                Arguments.of(setOf(Event(1580203689000, 1.1, 2.4)), 1),
                Arguments.of(setOf(Event(1580203689000, 1.1, 2.4), Event(1480203687000, 1.1, 2.4)), 1),
                Arguments.of(setOf(
                    Event(1580203689000, 1.1, 2.4),
                    Event(1580203699001, 1.1, 2.4),
                    Event(1580203628999, 1.1, 2.4),
                    Event(1480203687000, 1.1, 2.4)), 2),
            )
        }
    }
}