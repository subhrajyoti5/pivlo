package com.subhrajyoti.pivlo.data.clean

import com.subhrajyoti.pivlo.data.local.entity.FileEntity

enum class DeepCleanType(val id: String, val title: String, val description: String) {
    JUNK("junk", "Junk & Temporary Files", "Log files, cache, .tmp, .bak, and partial downloads"),
    DUPLICATE("duplicates", "Duplicate Copies", "Identical duplicate files (keeps 1 original)"),
    LARGE("large", "Large Files (>100MB)", "Single files occupying substantial storage"),
    OLD("old", "Stale Files (>6 Months)", "Files un-modified for over 180 days")
}

data class DeepCleanCategoryResult(
    val type: DeepCleanType,
    val files: List<FileEntity>,
    val totalSizeBytes: Long = files.sumOf { it.sizeBytes },
    val isSelected: Boolean = true
)

data class DeepCleanScanResult(
    val categories: List<DeepCleanCategoryResult> = emptyList(),
    val totalReclaimableBytes: Long = categories.filter { it.isSelected }.sumOf { it.totalSizeBytes }
)

data class DeepCleanProgress(
    val filesDeleted: Int = 0,
    val totalFiles: Int = 0,
    val bytesFreed: Long = 0L,
    val isComplete: Boolean = false
)
