package com.subhrajyoti.pivlo.data.indexing

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.subhrajyoti.pivlo.core.storage.FileCategory
import com.subhrajyoti.pivlo.data.local.entity.FileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

data class IndexingProgress(
    val currentBatch: Int = 0,
    val totalDiscovered: Int = 0,
    val isFinished: Boolean = false,
    val currentPath: String = ""
)

class StorageScanner(private val context: Context) {

    fun scanStorage(): Flow<List<FileEntity>> = flow {
        val discoveredFiles = mutableListOf<FileEntity>()
        val currentTime = System.currentTimeMillis()

        // 1. Scan MediaStore for Images, Video, Audio, Downloads, and Files
        val mediaStoreFiles = scanMediaStore(currentTime)
        discoveredFiles.addAll(mediaStoreFiles)
        emit(discoveredFiles.toList())

        // 2. Scan Shared Storage Directory (Documents, Downloads, etc.) if permitted
        try {
            val rootDir = Environment.getExternalStorageDirectory()
            if (rootDir != null && rootDir.exists() && rootDir.canRead()) {
                val traversedFiles = scanDirectoryRecursive(rootDir, currentTime)
                // Filter out any duplicates already collected from MediaStore by URI or Path
                val existingPaths = mediaStoreFiles.mapNotNull { it.path }.toSet()
                val newFiles = traversedFiles.filter { it.path != null && !existingPaths.contains(it.path) }
                discoveredFiles.addAll(newFiles)
                emit(discoveredFiles.toList())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }.flowOn(Dispatchers.IO)

    private fun scanMediaStore(timestamp: Long): List<FileEntity> {
        val results = mutableListOf<FileEntity>()
        val collections = mutableListOf<Uri>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collections.add(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL))
            collections.add(MediaStore.Downloads.EXTERNAL_CONTENT_URI)
        } else {
            collections.add(MediaStore.Files.getContentUri("external"))
        }

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.DATA
        )

        for (collectionUri in collections) {
            try {
                val cursor: Cursor? = context.contentResolver.query(
                    collectionUri,
                    projection,
                    null,
                    null,
                    "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
                )

                cursor?.use {
                    val idCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                    val nameCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    val mimeCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                    val sizeCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                    val modCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                    val dataCol = it.getColumnIndex(MediaStore.Files.FileColumns.DATA)

                    while (it.moveToNext()) {
                        val id = it.getLong(idCol)
                        val name = it.getString(nameCol) ?: "Unknown"
                        val mime = it.getString(mimeCol) ?: "application/octet-stream"
                        val size = it.getLong(sizeCol)
                        val mod = it.getLong(modCol) * 1000 // Convert seconds to ms
                        val path = if (dataCol != -1) it.getString(dataCol) else null
                        val itemUri = ContentUris.withAppendedId(collectionUri, id).toString()

                        val ext = name.substringAfterLast('.', "")
                        val category = FileCategory.fromExtension(ext)

                        results.add(
                            FileEntity(
                                name = name,
                                normalizedName = name.lowercase().trim(),
                                extension = ext.lowercase(),
                                mimeType = mime,
                                uri = itemUri,
                                path = path,
                                sizeBytes = size,
                                modifiedAt = if (mod > 0) mod else timestamp,
                                firstSeenAt = timestamp,
                                lastSeenAt = timestamp,
                                category = category,
                                availabilityStatus = "AVAILABLE"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return results
    }

    private fun scanDirectoryRecursive(directory: File, timestamp: Long): List<FileEntity> {
        val list = mutableListOf<FileEntity>()
        val excludedDirs = setOf("Android/data", "Android/obb", ".git", ".gradle", "node_modules")

        fun walk(dir: File) {
            if (!dir.canRead() || dir.isHidden) return
            val files = dir.listFiles() ?: return

            for (file in files) {
                if (file.isDirectory) {
                    val relPath = file.absolutePath
                    val isExcluded = excludedDirs.any { relPath.contains(it) }
                    if (!isExcluded) {
                        walk(file)
                    }
                } else if (file.isFile && file.length() > 0) {
                    val name = file.name
                    val ext = file.extension
                    val category = FileCategory.fromExtension(ext)

                    list.add(
                        FileEntity(
                            name = name,
                            normalizedName = name.lowercase().trim(),
                            extension = ext.lowercase(),
                            mimeType = getMimeTypeFromExtension(ext),
                            uri = Uri.fromFile(file).toString(),
                            path = file.absolutePath,
                            sizeBytes = file.length(),
                            modifiedAt = file.lastModified(),
                            firstSeenAt = timestamp,
                            lastSeenAt = timestamp,
                            category = category,
                            availabilityStatus = "AVAILABLE"
                        )
                    )
                }
            }
        }

        walk(directory)
        return list
    }

    companion object {
        fun calculateSha256(file: File): String? {
            return try {
                val digest = MessageDigest.getInstance("SHA-256")
                FileInputStream(file).use { fis ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        digest.update(buffer, 0, bytesRead)
                    }
                }
                digest.digest().joinToString("") { "%02x".format(it) }
            } catch (e: Exception) {
                null
            }
        }

        fun getMimeTypeFromExtension(ext: String): String {
            return android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.lowercase())
                ?: "application/octet-stream"
        }
    }
}

