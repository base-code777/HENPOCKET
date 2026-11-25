package com.henpocket.app.model

data class SplitState(
    val sourceName: String? = null,
    val sourceSize: Long = 0L,
    val destLabel: String? = null,
    val running: Boolean = false,
    val finished: Boolean = false,
    val bytesDone: Long = 0L,
    val currentPart: Int = 0,
    val totalParts: Int = 0,
    val speedMbPerSec: Double = 0.0,
    val message: String = "Choose a large ISO or PKG, then a folder on your USB drive."
) {
    val percent: Int
        get() = if (sourceSize <= 0L) 0 else ((bytesDone * 100L) / sourceSize).toInt().coerceIn(0, 100)
}
