package com.henpocket.app.files

/** Decides which PS3 folder a file belongs in, based on its name. */
object FileTypeRouter {

    data class Route(val dir: String, val label: String, val canMount: Boolean)

    private val PS3_ISO = Route("/dev_hdd0/PS3ISO", "PS3 game ISO", true)
    private val PACKAGES = Route("/dev_hdd0/packages", "PKG installer", false)
    private val EXDATA = Route("/dev_hdd0/exdata", "RAP license", false)
    private val PSX_BIN = Route("/dev_hdd0/PSXISO", "PS1 image", true)
    private val PSX_CUE = Route("/dev_hdd0/PSXISO", "PS1 cue sheet", false)
    private val PS2_ISO = Route("/dev_hdd0/PS2ISO", "PS2 image", true)

    private val SPLIT_ISO = Regex("\\.iso\\.\\d{5}$")

    fun routeFor(fileName: String): Route? {
        val n = fileName.lowercase()
        return when {
            n.endsWith(".bin.enc") -> PS2_ISO
            SPLIT_ISO.containsMatchIn(n) -> PS3_ISO.copy(label = "PS3 ISO part", canMount = false)
            n.endsWith(".iso") -> PS3_ISO
            n.endsWith(".pkg") -> PACKAGES
            n.endsWith(".rap") -> EXDATA
            n.endsWith(".bin") -> PSX_BIN
            n.endsWith(".cue") -> PSX_CUE
            else -> null
        }
    }

    val table: List<Pair<String, String>> = listOf(
        ".iso" to "/dev_hdd0/PS3ISO/",
        ".pkg" to "/dev_hdd0/packages/",
        ".rap" to "/dev_hdd0/exdata/",
        ".bin / .cue" to "/dev_hdd0/PSXISO/",
        ".BIN.ENC" to "/dev_hdd0/PS2ISO/"
    )
}
