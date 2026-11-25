package com.henpocket.app.ui.transfer

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.henpocket.app.files.FileTypeRouter
import com.henpocket.app.files.FtpClient
import com.henpocket.app.files.UriInfo
import com.henpocket.app.model.PickedFile
import com.henpocket.app.model.TransferState
import com.henpocket.app.network.WebmanClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class TransferViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(TransferState())
    val state: StateFlow<TransferState> = _state.asStateFlow()

    private var picked: PickedFile? = null
    private var job: Job? = null

    fun onFilePicked(uri: Uri?) {
        if (uri == null || _state.value.running) return
        viewModelScope.launch {
            val resolver = getApplication<Application>().contentResolver
            val file = withContext(Dispatchers.IO) { UriInfo.query(resolver, uri) }
            picked = file
            val route = FileTypeRouter.routeFor(file.name)
            _state.value = if (route == null) {
                TransferState(
                    fileName = file.name,
                    fileSize = file.size,
                    message = "Unsupported file type. Supported: .iso .pkg .rap .bin .cue .BIN.ENC"
                )
            } else {
                TransferState(
                    fileName = file.name,
                    fileSize = file.size,
                    routeLabel = route.label,
                    destination = route.dir + "/",
                    message = "Ready. This file will be sent to ${route.dir}/"
                )
            }
        }
    }

    fun start(host: String) {
        val file = picked ?: return
        val route = FileTypeRouter.routeFor(file.name) ?: return
        if (_state.value.running) return
        val resolver = getApplication<Application>().contentResolver

        job = viewModelScope.launch {
            _state.update {
                it.copy(
                    running = true,
                    finished = false,
                    bytesDone = 0L,
                    speedMbPerSec = 0.0,
                    mountPath = null,
                    message = "Connecting to $host…"
                )
            }
            try {
                withContext(Dispatchers.IO) {
                    FtpClient(host).use { ftp ->
                        ftp.connect()
                        ftp.login()
                        ftp.ensureDir(route.dir)
                        _state.update { it.copy(message = "Uploading…") }

                        val input = resolver.openInputStream(file.uri)
                            ?: throw IOException("Cannot open the selected file")
                        input.use { stream ->
                            var lastTime = System.nanoTime()
                            var lastBytes = 0L
                            ftp.upload(
                                input = stream,
                                remoteName = file.name,
                                onBytes = { total ->
                                    val now = System.nanoTime()
                                    if (now - lastTime >= 500_000_000L) {
                                        val seconds = (now - lastTime) / 1_000_000_000.0
                                        val speed = ((total - lastBytes) / 1_048_576.0) / seconds
                                        lastTime = now
                                        lastBytes = total
                                        _state.update { it.copy(bytesDone = total, speedMbPerSec = speed) }
                                    }
                                },
                                checkCancelled = { ensureActive() }
                            )
                        }
                    }
                }

                _state.update {
                    it.copy(
                        bytesDone = if (file.size > 0L) file.size else it.bytesDone,
                        message = "Upload complete. Refreshing the XMB…"
                    )
                }
                val refresh = WebmanClient(host).refreshXmb()
                val message = if (refresh.isSuccess) {
                    "Done! The XMB game list was refreshed."
                } else {
                    "Uploaded, but the XMB refresh failed. Use Refresh XMB on the Home tab."
                }
                _state.update {
                    it.copy(
                        running = false,
                        finished = true,
                        speedMbPerSec = 0.0,
                        mountPath = if (route.canMount) route.dir + "/" + file.name else null,
                        message = message
                    )
                }
            } catch (e: CancellationException) {
                _state.update { it.copy(running = false, speedMbPerSec = 0.0, message = "Transfer cancelled.") }
                withContext(NonCancellable + Dispatchers.IO) {
                    removePartial(host, route.dir, file.name)
                }
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        running = false,
                        speedMbPerSec = 0.0,
                        message = "Transfer failed: ${e.message ?: e.javaClass.simpleName}"
                    )
                }
            }
        }
    }

    fun cancel() {
        job?.cancel()
    }

    fun mount(host: String) {
        val path = _state.value.mountPath ?: return
        viewModelScope.launch {
            _state.update { it.copy(message = "Mounting…") }
            val result = WebmanClient(host).mount(path)
            _state.update {
                it.copy(
                    message = if (result.isSuccess) {
                        "Mount command sent. Check the game on your XMB."
                    } else {
                        "Mount failed: ${result.exceptionOrNull()?.message ?: "no response"}"
                    }
                )
            }
        }
    }

    private fun removePartial(host: String, dir: String, name: String) {
        try {
            FtpClient(host).use { ftp ->
                ftp.connect()
                ftp.login()
                ftp.delete(dir, name)
            }
        } catch (e: Exception) {
            // the partial file may remain if the PS3 is unreachable
        }
    }
}
