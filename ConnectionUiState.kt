package com.henpocket.app.model

data class ConnectionUiState(
    val status: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val ip: String? = null,
    val scanProgress: Int = 0,
    val message: String = "",
    val telemetry: Telemetry? = null,
    val telemetryNote: String? = null,
    val remoteBusy: Boolean = false,
    val remoteMessage: String? = null
)
