package com.subhrajyoti.pivlo.data.repository

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.subhrajyoti.pivlo.data.indexing.StorageScanner
import com.subhrajyoti.pivlo.data.local.PivloDatabase
import com.subhrajyoti.pivlo.data.local.entity.FileEntity
import com.subhrajyoti.pivlo.data.local.entity.FileHistoryEntity
import com.subhrajyoti.pivlo.data.search.QueryParser
import com.subhrajyoti.pivlo.data.search.SearchFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

/** How many rows Paging 3 loads per page */
private const val PAGE_SIZE = 50

/** Pre-fetch distance — rows loaded ahead of the visible area */
private const val PREFETCH_DISTANCE = 100

/** Max rows kept in memory across all loaded pages.
 *  When exceeded, the furthest page is dropped and re-loaded on demand. */
private const val MAX_SIZE = 400

/** Batch size when writing discovered files to Room (avoids giant transactions) */
private const val INDEX_WRITE_BATCH = 200

class FileRepository(private val context: Context) {

    private val db  = PivloDatabase.getDatabase(context)
    private val dao = db.fileDao()
    private val scanner     = StorageScanner(context)
    private val queryParser = QueryParser()

    // ------------------------------------------------------------------
    // Paginated search — main result stream
    // ------------------------------------------------------------------

    fun searchFilesPaged(filter: SearchFilter): Flow<PagingData<FileEntity>> {
        val sqlQuery = queryParser.buildPagingQuery(filter)
        return Pager(
            config = PagingConfig(
                pageSize         = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false,
                maxSize          = MAX_SIZE
            ),
            pagingSourceFactory = { dao.pagingSourceRaw(sqlQuery) }
        ).flow.flowOn(Dispatchers.IO)
    }

    // ------------------------------------------------------------------
    // Bounded flows (home screen / status cards)
    // ------------------------------------------------------------------

    val totalIndexedCount: Flow<Int>         = dao.getAvailableFileCount()
    val allExtensions:     Flow<List<String>> = dao.getAllDiscoveredExtensions()
    val recentFiles:       Flow<List<FileEntity>> = dao.getRecentFiles(50)
    val missingFiles:      Flow<List<FileEntity>> = dao.getMissingFiles()
    val trashedFiles:      Flow<List<FileEntity>> = dao.getTrashedFiles()
    val confirmedDuplicates: Flow<List<FileEntity>> = dao.getConfirmedDuplicates()

    // ------------------------------------------------------------------
    // Single-item lookup
    // ------------------------------------------------------------------

    suspend fun getFileById(id: Long): FileEntity? = withContext(Dispatchers.IO) {
        dao.getFileById(id)
    }

    // ------------------------------------------------------------------
    // Indexing
    // ------------------------------------------------------------------

    /**
     * Scans storage and writes discovered files to Room in bounded batches.
     * This keeps individual transactions small regardless of how many files
     * are discovered, and also prevents OOM on large collections.
     *
     * @param onProgress called after each batch with the running total so far.
     */
    suspend fun performIndexScan(onProgress: (total: Int) -> Unit = {}) =
        withContext(Dispatchers.IO) {
            val allDiscoveredUris = mutableListOf<String>()
            var runningTotal = 0

            scanner.scanStorage().collect { batch ->
                // Write one bounded batch at a time
                batch.chunked(INDEX_WRITE_BATCH).forEach { chunk ->
                    dao.insertOrUpdateAll(chunk)
                    allDiscoveredUris.addAll(chunk.map { it.uri })
                    runningTotal += chunk.size
                    onProgress(runningTotal)
                }
            }

            // Mark anything no longer on storage as MISSING.
            // markMissingFiles uses NOT IN — split into chunks to stay under
            // SQLite's 999-variable limit.
            if (allDiscoveredUris.isNotEmpty()) {
                allDiscoveredUris.chunked(500).forEach { chunk ->
                    dao.markMissingFiles(chunk)
                }
            }
        }

    // ------------------------------------------------------------------
    // Duplicate detection
    // ------------------------------------------------------------------

    /**
     * Computes SHA-256 hashes for size-based duplicate candidates.
     * Works in IO dispatcher; processes files one at a time to bound memory.
     */
    suspend fun computeHashesForDuplicates() = withContext(Dispatchers.IO) {
        val candidates = dao.getCandidateDuplicatesBySize()
        for (item in candidates) {
            if (item.contentHash == null && item.path != null) {
                val f = File(item.path)
                if (f.exists() && f.canRead() && f.length() < 500 * 1_048_576) { // cap at 500 MB
                    val hash = StorageScanner.calculateSha256(f)
                    if (hash != null) {
                        dao.update(item.copy(contentHash = hash))
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // File operations
    // ------------------------------------------------------------------

    suspend fun deleteFile(file: FileEntity): Boolean = withContext(Dispatchers.IO) {
        dao.insertHistory(
            FileHistoryEntity(
                fileId           = file.id,
                previousName     = file.name,
                previousLocation = file.path ?: file.uri,
                lastSeenAt       = System.currentTimeMillis(),
                status           = "DELETED",
                contentHash      = file.contentHash
            )
        )

        var physicallyDeleted = false
        if (file.path != null) {
            val f = File(file.path)
            if (f.exists()) physicallyDeleted = f.delete()
        }

        dao.deleteById(file.id)
        physicallyDeleted
    }

    // ------------------------------------------------------------------
    // Deep Clean Operations
    // ------------------------------------------------------------------

    suspend fun performDeepCleanScan(): com.subhrajyoti.pivlo.data.clean.DeepCleanScanResult =
        withContext(Dispatchers.IO) {
            // 1. Junk files
            val junkFiles = dao.getJunkFiles()

            // 2. Duplicate copies (group by hash, keep newest, return remaining)
            val allDuplicates = dao.getConfirmedDuplicatesList()
            val duplicateCopies = mutableListOf<FileEntity>()
            allDuplicates.groupBy { it.contentHash }.values.forEach { group ->
                if (group.size > 1) {
                    duplicateCopies.addAll(group.drop(1))
                }
            }

            // 3. Large files (> 100MB)
            val largeFiles = dao.getLargeFiles(104_857_600L)

            // 4. Stale files (> 180 days old)
            val sixMonthsAgo = System.currentTimeMillis() - (180L * 24 * 60 * 60 * 1000)
            val oldFiles = dao.getOldFiles(sixMonthsAgo)

            val categories = listOf(
                com.subhrajyoti.pivlo.data.clean.DeepCleanCategoryResult(
                    com.subhrajyoti.pivlo.data.clean.DeepCleanType.JUNK, junkFiles
                ),
                com.subhrajyoti.pivlo.data.clean.DeepCleanCategoryResult(
                    com.subhrajyoti.pivlo.data.clean.DeepCleanType.DUPLICATE, duplicateCopies
                ),
                com.subhrajyoti.pivlo.data.clean.DeepCleanCategoryResult(
                    com.subhrajyoti.pivlo.data.clean.DeepCleanType.LARGE, largeFiles
                ),
                com.subhrajyoti.pivlo.data.clean.DeepCleanCategoryResult(
                    com.subhrajyoti.pivlo.data.clean.DeepCleanType.OLD, oldFiles
                )
            )

            com.subhrajyoti.pivlo.data.clean.DeepCleanScanResult(categories = categories)
        }

    suspend fun permanentlyDeleteFiles(
        files: List<FileEntity>,
        onProgress: (done: Int, total: Int, bytesFreed: Long) -> Unit
    ): Long = withContext(Dispatchers.IO) {
        var totalFreed = 0L
        val totalCount = files.size
        val idsToDelete = mutableListOf<Long>()

        files.forEachIndexed { index, file ->
            dao.insertHistory(
                FileHistoryEntity(
                    fileId           = file.id,
                    previousName     = file.name,
                    previousLocation = file.path ?: file.uri,
                    lastSeenAt       = System.currentTimeMillis(),
                    status           = "PERMANENTLY_DELETED",
                    contentHash      = file.contentHash
                )
            )

            if (file.path != null) {
                val f = File(file.path)
                if (f.exists()) {
                    val size = f.length()
                    if (f.delete()) {
                        totalFreed += size
                    }
                }
            } else {
                try {
                    val uri = android.net.Uri.parse(file.uri)
                    context.contentResolver.delete(uri, null, null)
                    totalFreed += file.sizeBytes
                } catch (_: Exception) {}
            }

            idsToDelete.add(file.id)

            if ((index + 1) % 5 == 0 || index == files.lastIndex) {
                onProgress(index + 1, totalCount, totalFreed)
            }
        }

        if (idsToDelete.isNotEmpty()) {
            idsToDelete.chunked(200).forEach { chunk ->
                dao.deleteByIds(chunk)
            }
        }

        totalFreed
    }
}
