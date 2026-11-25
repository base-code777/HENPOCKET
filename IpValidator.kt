package com.henpocket.app.network

object IpValidator {
    private val IPV4 = Regex(
        "^(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}$"
    )

    fun isIpv4(value: String): Boolean = IPV4.matches(value.trim())
}
