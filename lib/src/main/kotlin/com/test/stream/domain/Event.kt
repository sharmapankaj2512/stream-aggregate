package com.test.stream.domain

import kotlinx.serialization.Serializable

@Serializable
data class Event(val occurredOn: Long, val x: Double, val y: Long) {
    companion object {
        fun from(eventText: String): Event {
            val cells = eventText.split(",")
            return Event(cells[0].toLong(), cells[1].toDouble(), cells[2].toLong())
        }
    }
}
