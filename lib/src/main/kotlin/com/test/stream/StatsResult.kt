package com.test.stream

data class StatsResult(val total: Int, val sumX: Double, val sumY: Long) {
    val avgX = if (total != 0) sumX / total else 0
    val avgY = if (total != 0) sumY / total else 0
}
