package com.test.stream.domain

import kotlinx.serialization.Serializable

@Serializable
data class StatsResult(val total: Int, val sumX: Double, val sumY: Long) {
    private val avgX: Double = if (total != 0) sumX / total else 0.0
    private val avgY: Long = if (total != 0) sumY / total else 0

    override fun toString(): String {
        return "$total,${"%.10f".format(sumX)},${"%.10f".format(avgX)},$sumY,$avgY"
    }
}
