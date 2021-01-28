package com.test.stream

enum class TimeWindow(private val duration: Int) {
    Minute(60);

    fun durationInMillis(): Int {
        return duration * 1000
    }
}