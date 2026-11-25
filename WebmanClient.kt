package com.henpocket.app.network

import com.henpocket.app.model.Telemetry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** Thin wrapper around the webMAN MOD HTTP API. Every call runs on Dispatchers.IO. */
class WebmanClient(private val host: String) {

    suspend fun get(path: String, timeoutMs: Int = 4000): Result<String> =
        withContext(Dispatchers.IO) {
            var conn: HttpURLConnection? = null
            try {
                conn = URL("http://$host$path").openConnection() as HttpURLConnection
                conn.connectTimeout = timeoutMs
                conn.readTimeout = timeoutMs
                conn.requestMethod = "GET"
                val code = conn.responseCode
                if (code !in 200..299) throw IOException("PS3 answered HTTP $code")
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                Result.success(body)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                try {
                    conn?.disconnect()
                } catch (e: Exception) {
                    // ignore
                }
            }
        }

    suspend fun telemetry(): Result<Telemetry> =
        get("/cpursx.ps3").map { CpursxParser.parse(it) }

    suspend fun refreshXmb(): Result<String> = get("/refresh.ps3")

    suspend fun mount(fullPath: String): Result<String> = get("/mount.ps3" + encodePath(fullPath))

    /**
     * restart.ps3 / shutdown.ps3 often drop the connection before answering,
     * so any outcome after the request is sent counts as "sent".
     */
    suspend fun powerCommand(path: String) {
        get(path, 3000)
    }

    private fun encodePath(path: String): String =
        path.split("/").joinToString("/") {
            URLEncoder.encode(it, "UTF-8").replace("+", "%20")
        }
}
