package com.henpocket.app.model

data class CleanupState(
    val files: List<RemoteFile> = emptyList(),
    val selected: Set<String> = emptySet(),
    val loading: Boolean = false,
    val message: String = "Tap Scan to list the packages stored on your PS3."
) {
    val selectedBytes: Long
        get() = files.filter { it.name in selected }.sumOf { it.size }
}
