package com.henpocket.app.model

data class Telemetry(
    val cpuC: Int?,
    val rsxC: Int?,
    val fanPercent: Int?,
    val hddFree: String?
) {
    val hasAnyValue: Boolean
        get() = cpuC != null || rsxC != null || fanPercent != null || hddFree != null
}
