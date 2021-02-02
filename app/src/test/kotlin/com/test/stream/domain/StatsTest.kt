package com.test.stream.domain

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class StatsTest {
    @ParameterizedTest
    @MethodSource("eventSource")
    fun testRow(events: Set<Event>, expectedTotal: Int, expectedSumX: Double, expectedSumY: Long) {
        runBlocking {
            val stats = Stats(Duration.ofMinutes(1), Clock.fixed(Instant.ofEpochMilli(1580203689000), ZoneId.of("UTC")))
                .record(events)

            delay(2000)

            val result = stats.read()
            assertEquals(expectedTotal, result.total)
            assertEquals(expectedSumX, result.sumX)
            assertEquals(expectedSumY, result.sumY)
        }
    }

    companion object {
        @JvmStatic
        fun eventSource(): List<Arguments> {
            return listOf(
                Arguments.of(setOf<Event>(), 0, 0.0, 0),
                Arguments.of(setOf(Event(1580203689000, 1.1, 2147483647)), 1, 1.1, 2147483647),
                Arguments.of(setOf(
                    Event(1580203689000, 1.4, 2147483647),
                    Event(1480203687000, 1.1, 147483647)
                ), 1, 1.4, 2147483647),
                Arguments.of(setOf(
                    Event(1580203689000, 1.1, 21),
                    Event(1580203699001, 1.1, 21),
                    Event(1580203628999, 1.8, 421),
                    Event(1480203687000, 4.1, 218)
                ), 2, 2.2, 42),
            )
        }
    }
}