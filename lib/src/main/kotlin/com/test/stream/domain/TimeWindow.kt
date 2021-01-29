package com.test.stream.domain

enum class TimeWindow(private val duration: Int) {
    Minute(60);

    fun durationInMillis(): Int {
        return duration * 1000
    }
}