package com.henpocket.app.network

import android.content.Context
import android.net.ConnectivityManager
import java.net.Inet4Address

object NetworkInfoHelper {

    /** Returns the phone's IPv4 address on the active network, or null if there is none. */
    fun localIpv4(context: Context): String? {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return null
            val props = cm.getLinkProperties(network) ?: return null
            props.linkAddresses
                .map { it.address }
                .firstOrNull { it is Inet4Address && !it.isLoopbackAddress }
                ?.hostAddress
        } catch (e: Exception) {
            null
        }
    }
}
