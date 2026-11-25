package com.henpocket.app.files

fun formatBytes(bytes: Long): String {
    if (bytes < 0L) return "unknown size"
    val kb = 1024.0
    val mb = kb * 1024.0
    val gb = mb * 1024.0
    val b = bytes.toDouble()
    return when {
        b >= gb -> "%.2f GB".format(b / gb)
        b >= mb -> "%.1f MB".format(b / mb)
        b >= kb -> "%.0f KB".format(b / kb)
        else -> "$bytes B"
    }
}
