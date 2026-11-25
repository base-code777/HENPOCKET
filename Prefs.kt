package com.henpocket.app.data

import android.content.Context
import com.henpocket.app.model.DnsEntry

class Prefs(context: Context) {

    private val sp = context.applicationContext
        .getSharedPreferences("henpocket_prefs", Context.MODE_PRIVATE)

    var lastIp: String?
        get() = sp.getString(KEY_LAST_IP, null)
        set(value) {
            sp.edit().putString(KEY_LAST_IP, value).apply()
        }

    var dnsEntries: List<DnsEntry>
        get() {
            val raw = sp.getString(KEY_DNS, null) ?: return DEFAULT_DNS
            return raw.split("\n")
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val p = line.split("\t")
                    if (p.size >= 3) DnsEntry(p[0], p[1], p[2]) else null
                }
        }
        set(value) {
            val raw = value.joinToString("\n") {
                "${clean(it.name)}\t${clean(it.primary)}\t${clean(it.secondary)}"
            }
            sp.edit().putString(KEY_DNS, raw).apply()
        }

    private fun clean(s: String): String = s.replace("\t", " ").replace("\n", " ").trim()

    companion object {
        private const val KEY_LAST_IP = "last_ip"
        private const val KEY_DNS = "dns_entries"

        val DEFAULT_DNS = listOf(
            DnsEntry("Google Public DNS", "8.8.8.8", "8.8.4.4"),
            DnsEntry("Cloudflare DNS", "1.1.1.1", "1.0.0.1")
        )
    }
}
