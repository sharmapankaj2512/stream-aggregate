package com.test.stream.domain

import kotlinx.serialization.Serializable

@Serializable
data class Event(val occurredOn: Long, val x: Double, val y: Long)

