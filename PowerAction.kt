package com.henpocket.app.model

enum class PowerAction(
    val dialogTitle: String,
    val dialogBody: String,
    val confirmLabel: String
) {
    REBOOT(
        "Reboot PS3?",
        "The console will restart. Any running game or transfer will be interrupted.",
        "Reboot"
    ),
    SHUTDOWN(
        "Shut down PS3?",
        "The console will power off completely. You will need to turn it on by hand.",
        "Shut down"
    )
}
