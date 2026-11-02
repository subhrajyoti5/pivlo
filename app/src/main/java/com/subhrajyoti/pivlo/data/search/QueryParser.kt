package com.subhrajyoti.pivlo.data.search

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.subhrajyoti.pivlo.core.storage.FileCategory

data class SearchFilter(
    val query: String = "",
    val category: FileCategory = FileCategory.ALL,
    val extension: String? = null,
    val minSize: Long? = null,
    val maxSize: Long? = null,
    val modifiedAfter: Long? = null,
    val sortBy: SortOption = SortOption.DATE_DESC
)

enum class SortOption(val displayName: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    SIZE_DESC("Largest First"),
    SIZE_ASC("Smallest First"),
    NAME_ASC("Name (A→Z)"),
    NAME_DESC("Name (Z→A)")
}

class QueryParser {

    /**
     * Parses structured tokens mixed with free text, e.g.:
     *   "ext:pdf notes"
     *   "type:audio"
     *   "size:>100MB video"
     *   "modified:7d invoice"
     *   "ext:zip size:>50MB"
     */
    fun parse(rawQuery: String, existingFilter: SearchFilter = SearchFilter()): SearchFilter {
        var cleanText = ""
        var ext = existingFilter.extension
        var cat = existingFilter.category
        var minSize = existingFilter.minSize
        var maxSize = existingFilter.maxSize
        var modAfter = existingFilter.modifiedAfter

        val tokens = rawQuery.trim().split("\\s+".toRegex())

        for (token in tokens) {
            when {
                token.startsWith("ext:", ignoreCase = true) -> {
                    ext = token.substringAfter(":").lowercase().removePrefix(".")
                }
                token.startsWith("type:", ignoreCase = true) -> {
                    val typeStr = token.substringAfter(":").uppercase()
                    cat = try {
                        FileCategory.valueOf(typeStr)
                    } catch (_: Exception) {
                        FileCategory.fromExtension(typeStr)
                    }
                }
                token.startsWith("size:>", ignoreCase = true) -> {
                    minSize = parseSizeToBytes(token.substringAfter(">"))
                }
                token.startsWith("size:<", ignoreCase = true) -> {
                    maxSize = parseSizeToBytes(token.substringAfter("<"))
                }
                token.startsWith("modified:", ignoreCase = true) -> {
                    val days = token.substringAfter(":").removeSuffix("d").toLongOrNull()
                    if (days != null) {
                        modAfter = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
                    }
                }
                token.isNotBlank() -> {
                    cleanText += if (cleanText.isEmpty()) token else " $token"
                }
            }
        }

        return existingFilter.copy(
            query = cleanText.trim(),
            category = cat,
            extension = ext,
            minSize = minSize,
            maxSize = maxSize,
            modifiedAfter = modAfter
        )
    }

    /**
     * Builds a pageable SupportSQLiteQuery.
     * DO NOT add LIMIT/OFFSET — Paging 3 appends those itself.
     */
    fun buildPagingQuery(filter: SearchFilter): SupportSQLiteQuery {
        val conditions = mutableListOf<String>()
        val args = mutableListOf<Any>()

        if (filter.query.isNotBlank()) {
            // Use LIKE on the pre-normalised column; fast because normalized_name is indexed.
            conditions.add("(normalized_name LIKE ? OR path LIKE ?)")
            val pattern = "%${filter.query.lowercase()}%"
            args.add(pattern)
            args.add(pattern)
        }

        if (filter.category != FileCategory.ALL) {
            conditions.add("category = ?")
            args.add(filter.category.name)
        }

        if (!filter.extension.isNullOrBlank()) {
            conditions.add("extension = ?")
            args.add(filter.extension.lowercase().removePrefix("."))
        }

        if (filter.minSize != null) {
            conditions.add("size_bytes >= ?")
            args.add(filter.minSize)
        }

        if (filter.maxSize != null) {
            conditions.add("size_bytes <= ?")
            args.add(filter.maxSize)
        }

        if (filter.modifiedAfter != null) {
            conditions.add("modified_at >= ?")
            args.add(filter.modifiedAfter)
        }

        val whereClause = if (conditions.isNotEmpty())
            "WHERE " + conditions.joinToString(" AND ")
        else ""

        val orderClause = when (filter.sortBy) {
            SortOption.DATE_DESC -> "ORDER BY modified_at DESC"
            SortOption.DATE_ASC  -> "ORDER BY modified_at ASC"
            SortOption.SIZE_DESC -> "ORDER BY size_bytes DESC"
            SortOption.SIZE_ASC  -> "ORDER BY size_bytes ASC"
            SortOption.NAME_ASC  -> "ORDER BY name COLLATE NOCASE ASC"
            SortOption.NAME_DESC -> "ORDER BY name COLLATE NOCASE DESC"
        }

        // No LIMIT — Paging 3 handles pagination
        val sql = "SELECT * FROM files $whereClause $orderClause"
        return SimpleSQLiteQuery(sql, args.toTypedArray())
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private fun parseSizeToBytes(sizeStr: String): Long? {
        val clean = sizeStr.uppercase().trim()
        return when {
            clean.endsWith("GB") -> clean.removeSuffix("GB").toDoubleOrNull()?.let { (it * 1_073_741_824).toLong() }
            clean.endsWith("MB") -> clean.removeSuffix("MB").toDoubleOrNull()?.let { (it * 1_048_576).toLong() }
            clean.endsWith("KB") -> clean.removeSuffix("KB").toDoubleOrNull()?.let { (it * 1_024).toLong() }
            clean.endsWith("B")  -> clean.removeSuffix("B").toLongOrNull()
            else                 -> clean.toLongOrNull()
        }
    }
}
