package com.henpocket.app.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger

/**
 * Sweeps .1 to .254 of the phone's /24 subnet, checking port 80 (webMAN HTTP)
 * and port 21 (FTP) with a 300 ms socket timeout, then confirms the best
 * candidate by asking webMAN for /cpursx.ps3.
 */
object SubnetScanner {

    private const val TIMEOUT_MS = 300
    private const val MAX_PARALLEL = 48

    suspend fun scan(localIp: String, onProgress: (Int) -> Unit): String? =
        withContext(Dispatchers.IO) {
            val prefix = localIp.substringBeforeLast('.', "")
            if (prefix.isEmpty()) return@withContext null

            val gate = Semaphore(MAX_PARALLEL)
            val finished = AtomicInteger(0)
            val candidates = Collections.synchronizedList(mutableListOf<String>())

            coroutineScope {
                for (host in 1..254) {
                    launch {
                        val ip = "$prefix.$host"
                        gate.withPermit {
                            if (ip != localIp) {
                                val http = isOpen(ip, 80)
                                val ftp = if (http) isOpen(ip, 21) else false
                                if (http && ftp) candidates.add(ip)
                            }
                        }
                        onProgress(finished.incrementAndGet() * 100 / 254)
                    }
                }
            }

            val ordered = candidates.toList().sortedBy { it.substringAfterLast('.').toIntOrNull() ?: 0 }
            for (ip in ordered) {
                if (WebmanClient(ip).telemetry().isSuccess) return@withContext ip
            }
            ordered.firstOrNull()
        }

    private fun isOpen(ip: String, port: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), TIMEOUT_MS)
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
