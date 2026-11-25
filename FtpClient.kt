package com.henpocket.app.files

import com.henpocket.app.model.RemoteFile
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Minimal passive-mode FTP client written for the PS3's webMAN FTP server.
 * Blocking calls: always run it on Dispatchers.IO.
 */
class FtpClient(private val host: String, private val port: Int = 21) : Closeable {

    private data class Reply(val code: Int, val text: String)

    private var socket: Socket? = null
    private var reader: BufferedReader? = null
    private var writer: BufferedWriter? = null

    fun connect() {
        val s = Socket()
        s.connect(InetSocketAddress(host, port), 5000)
        s.soTimeout = 20000
        socket = s
        reader = BufferedReader(InputStreamReader(s.getInputStream(), Charsets.UTF_8))
        writer = BufferedWriter(OutputStreamWriter(s.getOutputStream(), Charsets.UTF_8))
        val hello = readReply()
        if (hello.code != 220) throw IOException("PS3 FTP not ready: ${hello.text}")
    }

    fun login(user: String = "anonymous", pass: String = "anonymous") {
        var r = command("USER $user")
        if (r.code == 331) r = command("PASS $pass")
        if (r.code != 230) throw IOException("FTP login failed: ${r.text}")
        command("TYPE I")
    }

    /** Changes into [dir], creating missing folders on the way. */
    fun ensureDir(dir: String) {
        if (command("CWD $dir").code == 250) return
        var path = ""
        for (part in dir.trim('/').split('/')) {
            path += "/$part"
            if (command("CWD $path").code != 250) {
                command("MKD $path")
            }
        }
        if (command("CWD $dir").code != 250) {
            throw IOException("Cannot open $dir on the PS3")
        }
    }

    /**
     * Uploads [input] into the current folder as [remoteName].
     * [onBytes] receives the running total, [checkCancelled] should throw when the job is cancelled.
     */
    fun upload(
        input: InputStream,
        remoteName: String,
        onBytes: (Long) -> Unit,
        checkCancelled: () -> Unit
    ) {
        val data = openPassive()
        try {
            val r = command("STOR $remoteName")
            if (r.code != 150 && r.code != 125) {
                throw IOException("PS3 refused the upload: ${r.text}")
            }
            val out = data.getOutputStream()
            val buffer = ByteArray(256 * 1024)
            var total = 0L
            while (true) {
                checkCancelled()
                val n = input.read(buffer)
                if (n < 0) break
                out.write(buffer, 0, n)
                total += n
                onBytes(total)
            }
            out.flush()
        } finally {
            try {
                data.close()
            } catch (e: Exception) {
                // ignore
            }
        }
        val done = readReply()
        if (done.code != 226 && done.code != 250) {
            throw IOException("Upload not confirmed by the PS3: ${done.text}")
        }
    }

    /** Lists regular files in [dir] (folders are skipped). */
    fun list(dir: String): List<RemoteFile> {
        if (command("CWD $dir").code != 250) {
            throw IOException("Folder $dir was not found on the PS3")
        }
        val lines = mutableListOf<String>()
        val data = openPassive()
        try {
            val r = command("LIST")
            if (r.code != 150 && r.code != 125) {
                throw IOException("Could not list $dir: ${r.text}")
            }
            data.getInputStream().bufferedReader(Charsets.UTF_8).useLines { seq ->
                seq.forEach { lines.add(it) }
            }
        } finally {
            try {
                data.close()
            } catch (e: Exception) {
                // ignore
            }
        }
        readReply()

        val files = mutableListOf<RemoteFile>()
        for (raw in lines) {
            val line = raw.trim()
            if (line.isEmpty() || line.startsWith("d") || line.startsWith("total")) continue
            val parts = line.split(Regex("\\s+"), limit = 9)
            val file = if (parts.size >= 9) {
                RemoteFile(parts[8], parts[4].toLongOrNull() ?: 0L)
            } else {
                RemoteFile(line, 0L)
            }
            if (file.name != "." && file.name != "..") files.add(file)
        }
        return files.sortedBy { it.name.lowercase() }
    }

    fun delete(dir: String, name: String) {
        val r = command("DELE $dir/$name")
        if (r.code != 250) throw IOException("Could not delete $name: ${r.text}")
    }

    override fun close() {
        try {
            writer?.write("QUIT\r\n")
            writer?.flush()
        } catch (e: Exception) {
            // ignore
        }
        try {
            socket?.close()
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun command(cmd: String): Reply {
        val w = writer ?: throw IOException("Not connected")
        w.write(cmd + "\r\n")
        w.flush()
        return readReply()
    }

    private fun readReply(): Reply {
        val r = reader ?: throw IOException("Not connected")
        val first = r.readLine() ?: throw IOException("The PS3 closed the connection")
        val code = if (first.length >= 3) first.substring(0, 3).toIntOrNull() else null
        if (code == null) throw IOException("Unexpected FTP reply: $first")
        val text = StringBuilder(first)
        if (first.length > 3 && first[3] == '-') {
            while (true) {
                val line = r.readLine() ?: throw IOException("The PS3 closed the connection")
                text.append('\n').append(line)
                if (line.length >= 4 && line.startsWith(code.toString()) && line[3] == ' ') break
            }
        }
        return Reply(code, text.toString())
    }

    private fun openPassive(): Socket {
        val r = command("PASV")
        if (r.code != 227) throw IOException("PASV failed: ${r.text}")
        val m = Regex("(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+)").find(r.text)
            ?: throw IOException("Could not read the PASV reply")
        val g = m.groupValues
        val dataPort = g[5].toInt() * 256 + g[6].toInt()
        val data = Socket()
        data.connect(InetSocketAddress(host, dataPort), 5000)
        data.soTimeout = 30000
        return data
    }
}
