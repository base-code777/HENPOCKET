package com.henpocket.app.files

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import java.io.IOException
import java.io.InputStream

/**
 * Splits a big file into .66600, .66601 ... parts through the Storage Access Framework,
 * streaming through an 8 MB buffer so memory use stays flat.
 */
object FileSplitter {

    /** FAT32 cannot store a file larger than this. */
    const val FAT32_MAX = 4_294_967_295L

    /** Each part is 0xFFFF0000 bytes: under the FAT32 limit and a multiple of 2048. */
    const val PART_SIZE = 4_294_901_760L

    const val BUFFER_SIZE = 8 * 1024 * 1024
    private const val FIRST_SUFFIX = 66600

    fun partCount(size: Long): Int = ((size + PART_SIZE - 1L) / PART_SIZE).toInt()

    fun partName(baseName: String, index: Int): String = "$baseName.${FIRST_SUFFIX + index}"

    fun split(
        resolver: ContentResolver,
        source: Uri,
        sourceName: String,
        sourceSize: Long,
        destTree: Uri,
        onProgress: (bytesDone: Long, partIndex: Int) -> Unit,
        checkCancelled: () -> Unit
    ) {
        val parentDoc = DocumentsContract.buildDocumentUriUsingTree(
            destTree,
            DocumentsContract.getTreeDocumentId(destTree)
        )
        val created = mutableListOf<Uri>()
        var success = false
        try {
            val input = resolver.openInputStream(source)
                ?: throw IOException("Cannot open the source file")
            input.use { stream ->
                val buffer = ByteArray(BUFFER_SIZE)
                var total = 0L
                var part = 0
                var eof = false
                while (!eof && total < sourceSize) {
                    checkCancelled()
                    val name = partName(sourceName, part)
                    val partUri = DocumentsContract.createDocument(
                        resolver, parentDoc, "application/octet-stream", name
                    ) ?: throw IOException("Could not create $name on the destination")
                    created.add(partUri)

                    val out = resolver.openOutputStream(partUri, "w")
                        ?: throw IOException("Could not write $name")
                    var written = 0L
                    out.use { o ->
                        while (written < PART_SIZE && total < sourceSize) {
                            checkCancelled()
                            val want = minOf(buffer.size.toLong(), PART_SIZE - written).toInt()
                            val n = readFully(stream, buffer, want)
                            if (n <= 0) {
                                eof = true
                                break
                            }
                            o.write(buffer, 0, n)
                            written += n
                            total += n
                            onProgress(total, part)
                        }
                        o.flush()
                    }
                    part++
                }
            }
            success = true
        } finally {
            if (!success) {
                for (uri in created) {
                    try {
                        DocumentsContract.deleteDocument(resolver, uri)
                    } catch (e: Exception) {
                        // best effort cleanup of partial parts
                    }
                }
            }
        }
    }

    private fun readFully(input: InputStream, buffer: ByteArray, length: Int): Int {
        var offset = 0
        while (offset < length) {
            val n = input.read(buffer, offset, length - offset)
            if (n < 0) break
            offset += n
        }
        return offset
    }
}
