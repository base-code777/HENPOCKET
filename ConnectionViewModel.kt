package com.henpocket.app.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.henpocket.app.data.Prefs
import com.henpocket.app.model.ConnectionStatus
import com.henpocket.app.model.ConnectionUiState
import com.henpocket.app.network.IpValidator
import com.henpocket.app.network.NetworkInfoHelper
import com.henpocket.app.network.SubnetScanner
import com.henpocket.app.network.WebmanClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConnectionViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = Prefs(app)
    private val _state = MutableStateFlow(ConnectionUiState())
    val state: StateFlow<ConnectionUiState> = _state.asStateFlow()

    val savedIp: String?
        get() = prefs.lastIp

    private var connectJob: Job? = null
    private var failures = 0

    init {
        val saved = prefs.lastIp
        if (!saved.isNullOrBlank()) connectTo(saved)
    }

    // ---------- connecting ----------

    fun findPs3() {
        if (_state.value.status == ConnectionStatus.SCANNING) return
        connectJob?.cancel()
        connectJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    status = ConnectionStatus.SCANNING,
                    scanProgress = 0,
                    telemetry = null,
                    message = "Looking for your PS3 on Wi-Fi…"
                )
            }
            try {
                val localIp = withContext(Dispatchers.IO) {
                    NetworkInfoHelper.localIpv4(getApplication<Application>())
                }
                if (localIp == null) {
                    fail("Not on a Wi-Fi network. Join the same Wi-Fi as your PS3 and try again.")
                    return@launch
                }
                val found = SubnetScanner.scan(localIp) { percent ->
                    _state.update { it.copy(scanProgress = percent) }
                }
                if (found == null) {
                    fail("No PS3 found. Make sure HEN and webMAN MOD are running, or enter the IP by hand.")
                    return@launch
                }
                if (!attemptConnect(found)) {
                    fail("Found a device at $found, but webMAN MOD did not answer.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                fail("Scan failed: ${e.message ?: e.javaClass.simpleName}")
            }
        }
    }

    fun connectManual(raw: String) {
        val ip = raw.trim()
        if (!IpValidator.isIpv4(ip)) {
            fail("That does not look like an IPv4 address (example: 192.168.1.42).")
            return
        }
        connectTo(ip)
    }

    fun disconnect() {
        connectJob?.cancel()
        failures = 0
        _state.update {
            ConnectionUiState(message = "Disconnected. Tap Find My PS3 to reconnect.")
        }
    }

    private fun connectTo(ip: String) {
        connectJob?.cancel()
        connectJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    status = ConnectionStatus.SCANNING,
                    scanProgress = 0,
                    message = "Connecting to $ip…"
                )
            }
            try {
                if (!attemptConnect(ip)) {
                    fail("Nothing answered at $ip. Is the PS3 on with webMAN MOD running?")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                fail("Connection failed: ${e.message ?: e.javaClass.simpleName}")
            }
        }
    }

    private suspend fun attemptConnect(ip: String): Boolean {
        val result = WebmanClient(ip).telemetry()
        val telemetry = result.getOrNull() ?: return false
        prefs.lastIp = ip
        failures = 0
        _state.update {
            it.copy(
                status = ConnectionStatus.CONNECTED,
                ip = ip,
                telemetry = telemetry,
                telemetryNote = null,
                scanProgress = 0,
                message = "Connected to $ip"
            )
        }
        return true
    }

    private fun fail(message: String) {
        _state.update {
            it.copy(
                status = ConnectionStatus.DISCONNECTED,
                ip = null,
                telemetry = null,
                scanProgress = 0,
                message = message
            )
        }
    }

    // ---------- telemetry ----------

    suspend fun pollTelemetry() {
        val current = _state.value
        val ip = current.ip ?: return
        if (current.status != ConnectionStatus.CONNECTED) return

        val telemetry = WebmanClient(ip).telemetry().getOrNull()
        if (telemetry != null) {
            failures = 0
            _state.update { it.copy(telemetry = telemetry, telemetryNote = null) }
        } else {
            failures++
            if (failures >= 3) {
                fail("Lost connection to the PS3 (rebooting or powered off?).")
            } else {
                _state.update { it.copy(telemetryNote = "Waiting for PS3…") }
            }
        }
    }

    // ---------- remote controls ----------

    fun refreshXmb() = runRemote("Refreshing XMB…") { client ->
        client.refreshXmb().fold(
            onSuccess = { "XMB refreshed." },
            onFailure = { "Refresh failed: ${it.message ?: "no response"}" }
        )
    }

    fun reboot() = runRemote("Sending reboot…") { client ->
        if (client.telemetry().isFailure) {
            "The PS3 is not responding."
        } else {
            client.powerCommand("/restart.ps3")
            "Reboot command sent. The PS3 is restarting."
        }
    }

    fun shutdown() = runRemote("Sending shutdown…") { client ->
        if (client.telemetry().isFailure) {
            "The PS3 is not responding."
        } else {
            client.powerCommand("/shutdown.ps3")
            "Shutdown command sent. The PS3 is powering off."
        }
    }

    private fun runRemote(busyMessage: String, block: suspend (WebmanClient) -> String) {
        val current = _state.value
        val ip = current.ip ?: return
        if (current.remoteBusy) return
        viewModelScope.launch {
            _state.update { it.copy(remoteBusy = true, remoteMessage = busyMessage) }
            val message = try {
                block(WebmanClient(ip))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                "Failed: ${e.message ?: e.javaClass.simpleName}"
            }
            _state.update { it.copy(remoteBusy = false, remoteMessage = message) }
        }
    }
}
