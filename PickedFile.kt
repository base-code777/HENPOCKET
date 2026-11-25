package com.henpocket.app.model

import android.net.Uri

data class PickedFile(
    val uri: Uri,
    val name: String,
    val size: Long
)
