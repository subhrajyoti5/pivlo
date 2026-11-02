package com.subhrajyoti.pivlo.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.subhrajyoti.pivlo.data.local.entity.FileEntity
import com.subhrajyoti.pivlo.data.local.entity.FileHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {

    // ----------------------------
    // Paginated search (main flow)
    // ----------------------------

    /**
     * Raw query returning a PagingSource for Paging 3.
     * The caller builds the SQL with ORDER BY and WHERE clauses;
     * Paging 3 appends its own LIMIT/OFFSET so we must NOT include them here.
     */
    @RawQuery(observedEntities = [FileEntity::class])
    fun pagingSourceRaw(query: SupportSQLiteQuery): PagingSource<Int, FileEntity>

    // ----------------------------
    // Recent files (home screen, bounded)
    // ----------------------------

    @Query("SELECT * FROM files ORDER BY modified_at DESC LIMIT :limit")
    fun getRecentFiles(limit: Int = 50): Flow<List<FileEntity>>

    // ----------------------------
    // Single-item lookups
    // ----------------------------

    @Query("SELECT * FROM files WHERE id = :id")
    suspend fun getFileById(id: Long): FileEntity?

    @Query("SELECT * FROM files WHERE uri = :uri LIMIT 1")
    suspend fun getFileByUri(uri: String): FileEntity?

    // ----------------------------
    // Counts
    // ----------------------------

    @Query("SELECT COUNT(*) FROM files")
    fun getTotalFileCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM files WHERE availability_status = 'AVAILABLE'")
    fun getAvailableFileCount(): Flow<Int>

    // ----------------------------
    // Write operations
    // ----------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(file: FileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(files: List<FileEntity>)

    @Update
    suspend fun update(file: FileEntity)

    @Query("DELETE FROM files WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM files WHERE uri = :uri")
    suspend fun deleteByUri(uri: String)

    @Query("DELETE FROM files")
    suspend fun clearAll()

    // ----------------------------
    // Availability management
    // ----------------------------

    @Query("UPDATE files SET availability_status = :status WHERE uri = :uri")
    suspend fun updateAvailabilityStatus(uri: String, status: String)

    /**
     * Marks files MISSING in batches to avoid SQLite "too many variables" limit.
     * Call this with chunked URI lists (≤ 500 items each).
     */
    @Query("UPDATE files SET availability_status = 'MISSING' WHERE uri NOT IN (:activeUris)")
    suspend fun markMissingFiles(activeUris: List<String>)

    // ----------------------------
    // Extensions / filters
    // ----------------------------

    @Query(
        """
        SELECT DISTINCT extension FROM files 
        WHERE extension != '' 
        ORDER BY extension ASC
        LIMIT 50
        """
    )
    fun getAllDiscoveredExtensions(): Flow<List<String>>

    // ----------------------------
    // Duplicate detection
    // ----------------------------

    /**
     * First pass: size candidates (no joins, no subqueries on large sets).
     * Bounded by LIMIT to avoid loading the whole table.
     */
    @Query(
        """
        SELECT * FROM files 
        WHERE size_bytes > 0 
          AND size_bytes IN (
              SELECT size_bytes FROM files 
              WHERE size_bytes > 0 
              GROUP BY size_bytes 
              HAVING COUNT(*) > 1
          )
        ORDER BY size_bytes DESC
        LIMIT 2000
        """
    )
    suspend fun getCandidateDuplicatesBySize(): List<FileEntity>

    /** Second pass: confirmed duplicates by hash (Flow for live UI). */
    @Query(
        """
        SELECT * FROM files 
        WHERE content_hash IS NOT NULL 
          AND content_hash IN (
              SELECT content_hash FROM files 
              WHERE content_hash IS NOT NULL 
              GROUP BY content_hash 
              HAVING COUNT(*) > 1
          )
        ORDER BY content_hash ASC, size_bytes DESC
        """
    )
    fun getConfirmedDuplicates(): Flow<List<FileEntity>>

    // ----------------------------
    // Status views
    // ----------------------------

    @Query("SELECT * FROM files WHERE availability_status = 'MISSING' ORDER BY last_seen_at DESC LIMIT 200")
    fun getMissingFiles(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE availability_status = 'TRASHED' ORDER BY last_seen_at DESC LIMIT 200")
    fun getTrashedFiles(): Flow<List<FileEntity>>

    // ----------------------------
    // History
    // ----------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: FileHistoryEntity)

    @Query("SELECT * FROM file_history WHERE file_id = :fileId ORDER BY last_seen_at DESC")
    fun getHistoryForFile(fileId: Long): Flow<List<FileHistoryEntity>>

    // ----------------------------
    // Deep Clean Queries
    // ----------------------------

    @Query(
        """
        SELECT * FROM files 
        WHERE LOWER(extension) IN ('tmp', 'log', 'bak', 'chk', 'part', 'crdownload', 'cache', 'dmp')
           OR LOWER(name) LIKE '%.tmp'
           OR LOWER(name) LIKE '%.log'
           OR LOWER(name) LIKE '%.bak'
        LIMIT 500
        """
    )
    suspend fun getJunkFiles(): List<FileEntity>

    @Query("SELECT * FROM files WHERE size_bytes >= :minSizeBytes ORDER BY size_bytes DESC LIMIT 200")
    suspend fun getLargeFiles(minSizeBytes: Long = 104_857_600L): List<FileEntity>

    @Query("SELECT * FROM files WHERE modified_at < :beforeTimestampMs ORDER BY modified_at ASC LIMIT 200")
    suspend fun getOldFiles(beforeTimestampMs: Long): List<FileEntity>

    @Query(
        """
        SELECT * FROM files 
        WHERE content_hash IS NOT NULL 
          AND content_hash IN (
              SELECT content_hash FROM files 
              WHERE content_hash IS NOT NULL 
              GROUP BY content_hash 
              HAVING COUNT(*) > 1
          )
        ORDER BY content_hash ASC, modified_at DESC
        """
    )
    suspend fun getConfirmedDuplicatesList(): List<FileEntity>

    @Query("DELETE FROM files WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}
