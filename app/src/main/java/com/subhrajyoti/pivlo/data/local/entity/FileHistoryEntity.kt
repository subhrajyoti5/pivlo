package com.subhrajyoti.pivlo.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "file_history",
    indices = [
        Index(value = ["file_id"]),
        Index(value = ["previous_name"]),
        Index(value = ["last_seen_at"])
    ]
)
data class FileHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "file_id")
    val fileId: Long,
    @ColumnInfo(name = "previous_name")
    val previousName: String,
    @ColumnInfo(name = "previous_location")
    val previousLocation: String,
    @ColumnInfo(name = "last_seen_at")
    val lastSeenAt: Long,
    val status: String, // MISSING, TRASHED, RENAMED
    @ColumnInfo(name = "content_hash")
    val contentHash: String? = null
)

