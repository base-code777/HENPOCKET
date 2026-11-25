package com.henpocket.app.network

import com.henpocket.app.model.Telemetry

/**
 * Parses the HTML that webMAN MOD returns from /cpursx.ps3.
 * The exact wording differs between webMAN versions, so every field is optional.
 */
object CpursxParser {

    private val TAGS = Regex("<[^>]*>")
    private val SPACES = Regex("\\s+")
    private val CPU = Regex("CPU\\s*:?\\s*(\\d+)", RegexOption.IGNORE_CASE)
    private val RSX = Regex("RSX\\s*:?\\s*(\\d+)", RegexOption.IGNORE_CASE)
    private val FAN = Regex("FAN\\s*(?:SPEED)?\\s*:?\\s*(\\d+)\\s*%", RegexOption.IGNORE_CASE)
    private val FREE = Regex(
        "(?:HDD|FREE|dev_hdd0)[^0-9]{0,25}([0-9][0-9.,]*)\\s*(TB|GB|MB|KB)",
        RegexOption.IGNORE_CASE
    )

    fun parse(html: String): Telemetry {
        val text = html
            .replace(TAGS, " ")
            .replace("&nbsp;", " ")
            .replace(SPACES, " ")
            .trim()

        val cpu = CPU.find(text)?.groupValues?.get(1)?.toIntOrNull()
        val rsx = RSX.find(text)?.groupValues?.get(1)?.toIntOrNull()
        val fan = FAN.find(text)?.groupValues?.get(1)?.toIntOrNull()
        val free = FREE.find(text)?.let { "${it.groupValues[1]} ${it.groupValues[2].uppercase()}" }

        return Telemetry(cpuC = cpu, rsxC = rsx, fanPercent = fan, hddFree = free)
    }
}
