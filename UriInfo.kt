package com.henpocket.app.files

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import com.henpocket.app.model.PickedFile

object UriInfo {

    fun query(resolver: ContentResolver, uri: Uri): PickedFile {
        var name = uri.lastPathSegment ?: "file"
        var size = -1L
        try {
            resolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx >= 0) {
                        cursor.getString(nameIdx)?.let { name = it }
                    }
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) {
                        size = cursor.getLong(sizeIdx)
                    }
                }
            }
        } catch (e: Exception) {
            // keep the fallback values
        }
        return PickedFile(uri, name, size)
    }
}
