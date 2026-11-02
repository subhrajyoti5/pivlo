package com.subhrajyoti.pivlo.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.subhrajyoti.pivlo.core.storage.FileCategory

@Entity(
    tableName = "files",
    indices = [
        Index(value = ["uri"], unique = true),
        Index(value = ["normalized_name"]),
        Index(value = ["extension"]),
        Index(value = ["category"]),
        Index(value = ["size_bytes"]),
        Index(value = ["modified_at"]),
        Index(value = ["content_hash"]),
        Index(value = ["availability_status"])
    ]
)
data class FileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "normalized_name")
    val normalizedName: String,
    val extension: String,
    @ColumnInfo(name = "mime_type")
    val mimeType: String,
    val uri: String,
    val path: String? = null,
    @ColumnInfo(name = "size_bytes")
    val sizeBytes: Long,
    @ColumnInfo(name = "modified_at")
    val modifiedAt: Long,
    @ColumnInfo(name = "first_seen_at")
    val firstSeenAt: Long,
    @ColumnInfo(name = "last_seen_at")
    val lastSeenAt: Long,
    @ColumnInfo(name = "content_hash")
    val contentHash: String? = null,
    @ColumnInfo(name = "availability_status")
    val availabilityStatus: String = "AVAILABLE", // AVAILABLE, MISSING, TRASHED
    val category: FileCategory
)

