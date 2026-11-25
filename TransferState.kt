package com.henpocket.app.model

data class TransferState(
    val fileName: String? = null,
    val fileSize: Long = 0L,
    val routeLabel: String? = null,
    val destination: String? = null,
    val bytesDone: Long = 0L,
    val speedMbPerSec: Double = 0.0,
    val running: Boolean = false,
    val finished: Boolean = false,
    val mountPath: String? = null,
    val message: String = "Pick a file to send to your PS3."
) {
    val percent: Int
        get() = if (fileSize <= 0L) 0 else ((bytesDone * 100L) / fileSize).toInt().coerceIn(0, 100)

    val canSend: Boolean
        get() = destination != null && !running
}
