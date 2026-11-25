package com.henpocket.app.ui.splitter

import android.app.Application
import android.net.Uri
import android.provider.DocumentsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.henpocket.app.files.FileSplitter
import com.henpocket.app.files.UriInfo
import com.henpocket.app.files.formatBytes
import com.henpocket.app.model.PickedFile
import com.henpocket.app.model.SplitState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplitterViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(SplitState())
    val state: StateFlow<SplitState> = _state.asStateFlow()

    private var source: PickedFile? = null
    private var destTree: Uri? = null
    private var job: Job? = null

    fun onSourcePicked(uri: Uri?) {
        if (uri == null || _state.value.running) return
        viewModelScope.launch {
            val resolver = getApplication<Application>().contentResolver
            val file = withContext(Dispatchers.IO) { UriInfo.query(resolver, uri) }
            source = file
            val note = when {
                file.size <= 0L -> "Could not read the size of this file."
                file.size <= FileSplitter.FAT32_MAX ->
                    "This file is under 4 GB (${formatBytes(file.size)}), so it fits on FAT32 without splitting."
                else -> "Will be split into ${FileSplitter.partCount(file.size)} parts."
            }
            _state.update {
                it.copy(
                    sourceName = file.name,
                    sourceSize = file.size,
                    totalParts = if (file.size > FileSplitter.FAT32_MAX) FileSplitter.partCount(file.size) else 0,
                    finished = false,
                    bytesDone = 0L,
                    currentPart = 0,
                    message = note
                )
            }
        }
    }

    fun onDestinationPicked(uri: Uri?) {
        if (uri == null || _state.value.running) return
        destTree = uri
        val label = try {
            DocumentsContract.getTreeDocumentId(uri)
        } catch (e: Exception) {
            uri.toString()
        }
        _state.update { it.copy(destLabel = label) }
    }

    fun start() {
        val src = source ?: return
        val dest = destTree ?: return
        if (_state.value.running) return
        if (src.size <= 0L) {
            _state.update { it.copy(message = "The size of this file is unknown, so it cannot be split.") }
            return
        }
        if (src.size <= FileSplitter.FAT32_MAX) {
            _state.update { it.copy(message = "No split needed: the file is under 4 GB. Copy it to the drive directly.") }
            return
        }

        val resolver = getApplication<Application>().contentResolver
        val parts = FileSplitter.partCount(src.size)

        job = viewModelScope.launch {
            _state.update {
                it.copy(
                    running = true,
                    finished = false,
                    bytesDone = 0L,
                    currentPart = 1,
                    totalParts = parts,
                    speedMbPerSec = 0.0,
                    message = "Splitting…"
                )
            }
            try {
                withContext(Dispatchers.IO) {
                    var lastTime = System.nanoTime()
                    var lastBytes = 0L
                    FileSplitter.split(
                        resolver = resolver,
                        source = src.uri,
                        sourceName = src.name,
                        sourceSize = src.size,
                        destTree = dest,
                        onProgress = { total, partIndex ->
                            val now = System.nanoTime()
                            if (now - lastTime >= 500_000_000L) {
                                val seconds = (now - lastTime) / 1_000_000_000.0
                                val speed = ((total - lastBytes) / 1_048_576.0) / seconds
                                lastTime = now
                                lastBytes = total
                                _state.update {
                                    it.copy(
                                        bytesDone = total,
                                        currentPart = partIndex + 1,
                                        speedMbPerSec = speed
                                    )
                                }
                            }
                        },
                        checkCancelled = { ensureActive() }
                    )
                }
                _state.update {
                    it.copy(
                        running = false,
                        finished = true,
                        bytesDone = src.size,
                        speedMbPerSec = 0.0,
                        message = "Done! Wrote $parts parts. Plug the drive into the PS3."
                    )
                }
            } catch (e: CancellationException) {
                _state.update {
                    it.copy(running = false, speedMbPerSec = 0.0, message = "Cancelled. Partial parts were removed.")
                }
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        running = false,
                        speedMbPerSec = 0.0,
                        message = "Split failed: ${e.message ?: e.javaClass.simpleName}. Partial parts were removed."
                    )
                }
            }
        }
    }

    fun cancel() {
        job?.cancel()
    }
}
