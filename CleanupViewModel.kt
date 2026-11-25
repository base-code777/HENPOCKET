package com.henpocket.app.ui.tools

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.henpocket.app.files.FtpClient
import com.henpocket.app.model.CleanupState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CleanupViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(CleanupState())
    val state: StateFlow<CleanupState> = _state.asStateFlow()

    fun scan(host: String) {
        if (_state.value.loading) return
        viewModelScope.launch { loadList(host, null) }
    }

    fun toggle(name: String) {
        _state.update {
            val next = if (name in it.selected) it.selected - name else it.selected + name
            it.copy(selected = next)
        }
    }

    fun selectAll() {
        _state.update { s ->
            val all = s.files.map { it.name }.toSet()
            s.copy(selected = if (s.selected.size == all.size) emptySet() else all)
        }
    }

    fun deleteSelected(host: String) {
        val names = _state.value.selected.toList()
        if (names.isEmpty() || _state.value.loading) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = "Deleting ${names.size} file(s)…") }
            var deleted = 0
            var lastError: String? = null
            try {
                withContext(Dispatchers.IO) {
                    FtpClient(host).use { ftp ->
                        ftp.connect()
                        ftp.login()
                        for (name in names) {
                            try {
                                ftp.delete(PACKAGES_DIR, name)
                                deleted++
                            } catch (e: Exception) {
                                lastError = e.message ?: e.javaClass.simpleName
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastError = e.message ?: e.javaClass.simpleName
            }
            val error = lastError
            val summary = if (error == null) "Deleted $deleted file(s)." else "Deleted $deleted, some failed: $error."
            loadList(host, summary)
        }
    }

    private suspend fun loadList(host: String, prefix: String?) {
        _state.update { it.copy(loading = true, message = "Reading $PACKAGES_DIR …") }
        try {
            val files = withContext(Dispatchers.IO) {
                FtpClient(host).use { ftp ->
                    ftp.connect()
                    ftp.login()
                    ftp.list(PACKAGES_DIR)
                }
            }
            val found = if (files.isEmpty()) "No packages left in $PACKAGES_DIR." else "${files.size} file(s) in $PACKAGES_DIR."
            _state.update {
                it.copy(
                    files = files,
                    selected = emptySet(),
                    loading = false,
                    message = if (prefix == null) found else "$prefix $found"
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.update {
                it.copy(loading = false, message = "Scan failed: ${e.message ?: e.javaClass.simpleName}")
            }
        }
    }

    companion object {
        const val PACKAGES_DIR = "/dev_hdd0/packages"
    }
}
