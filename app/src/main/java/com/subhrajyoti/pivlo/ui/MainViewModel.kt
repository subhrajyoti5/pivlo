package com.subhrajyoti.pivlo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.subhrajyoti.pivlo.core.storage.FileCategory
import com.subhrajyoti.pivlo.data.local.entity.FileEntity
import com.subhrajyoti.pivlo.data.repository.FileRepository
import com.subhrajyoti.pivlo.data.search.QueryParser
import com.subhrajyoti.pivlo.data.search.SearchFilter
import com.subhrajyoti.pivlo.data.search.SortOption
import com.subhrajyoti.pivlo.data.clean.DeepCleanProgress
import com.subhrajyoti.pivlo.data.clean.DeepCleanScanResult
import com.subhrajyoti.pivlo.data.clean.DeepCleanType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PivloUiState(
    val query: String = "",
    val filter: SearchFilter = SearchFilter(),
    val isIndexing: Boolean = false,
    val indexingStatus: String = "Ready to search",
    val activeFilterCount: Int = 0,
    val selectedFileForDetails: FileEntity? = null,
    val showFilterSheet: Boolean = false,
    val showDeepCleanSheet: Boolean = false,
    val isDeepCleanScanning: Boolean = false,
    val isDeepCleanCleaning: Boolean = false,
    val deepCleanScanResult: DeepCleanScanResult? = null,
    val deepCleanProgress: DeepCleanProgress? = null
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository  = FileRepository(application)
    private val queryParser = QueryParser()

    private val _uiState = MutableStateFlow(PivloUiState())
    val uiState: StateFlow<PivloUiState> = _uiState.asStateFlow()

    // ------------------------------------------------------------------
    // Supporting state flows
    // ------------------------------------------------------------------

    val totalCount: StateFlow<Int> = repository.totalIndexedCount.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), 0
    )

    val discoveredExtensions: StateFlow<List<String>> = repository.allExtensions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    val duplicateFiles: StateFlow<List<FileEntity>> = repository.confirmedDuplicates.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    // ------------------------------------------------------------------
    // Paged search results
    //
    // _filterFlow drives changes; a 150 ms debounce prevents re-querying
    // on every single keystroke. flatMapLatest cancels the previous
    // PagingSource when the filter changes.  cachedIn keeps the page
    // cache alive across recompositions (e.g. configuration changes).
    // ------------------------------------------------------------------

    private val _filterFlow = MutableStateFlow(SearchFilter())

    val pagedFiles: Flow<PagingData<FileEntity>> = _filterFlow
        .debounce(150)
        .distinctUntilChanged()
        .flatMapLatest { filter -> repository.searchFilesPaged(filter) }
        .cachedIn(viewModelScope)

    // ------------------------------------------------------------------
    // Init
    // ------------------------------------------------------------------

    init {
        startIndexing()
    }

    // ------------------------------------------------------------------
    // Query / filter mutations
    // ------------------------------------------------------------------

    fun onQueryChanged(newQuery: String) {
        val updated = queryParser.parse(newQuery, _uiState.value.filter)
        applyFilter(updated, rawQuery = newQuery)
    }

    fun onCategorySelected(category: FileCategory) {
        val current = _uiState.value.filter
        val newCat  = if (current.category == category) FileCategory.ALL else category
        applyFilter(current.copy(category = newCat))
    }

    fun onExtensionSelected(ext: String?) {
        val current = _uiState.value.filter
        val newExt  = if (current.extension == ext) null else ext
        applyFilter(current.copy(extension = newExt))
    }

    fun onSortSelected(sortOption: SortOption) {
        applyFilter(_uiState.value.filter.copy(sortBy = sortOption))
    }

    fun onSizeFilterSelected(min: Long?, max: Long?) {
        applyFilter(_uiState.value.filter.copy(minSize = min, maxSize = max))
    }

    fun onDateFilterSelected(modifiedAfterMs: Long?) {
        applyFilter(_uiState.value.filter.copy(modifiedAfter = modifiedAfterMs))
    }

    fun resetFilters() {
        val reset = SearchFilter(query = _uiState.value.filter.query)
        applyFilter(reset)
    }

    private fun applyFilter(filter: SearchFilter, rawQuery: String = filter.query) {
        _uiState.value = _uiState.value.copy(
            query = rawQuery,
            filter = filter,
            activeFilterCount = countActiveFilters(filter)
        )
        _filterFlow.value = filter
    }

    // ------------------------------------------------------------------
    // Indexing
    // ------------------------------------------------------------------

    fun startIndexing() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isIndexing     = true,
                indexingStatus = "Scanning storage…"
            )
            try {
                repository.performIndexScan { total ->
                    _uiState.value = _uiState.value.copy(
                        indexingStatus = "Indexed $total files…"
                    )
                }
                _uiState.value = _uiState.value.copy(
                    isIndexing     = false,
                    indexingStatus = "Index up to date"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isIndexing     = false,
                    indexingStatus = "Completed with notices"
                )
            }
        }
    }

    fun findDuplicates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isIndexing     = true,
                indexingStatus = "Computing hashes for duplicates…"
            )
            repository.computeHashesForDuplicates()
            _uiState.value = _uiState.value.copy(
                isIndexing     = false,
                indexingStatus = "Duplicate scan complete"
            )
        }
    }

    // ------------------------------------------------------------------
    // UI toggles
    // ------------------------------------------------------------------

    fun toggleFilterSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showFilterSheet = show)
    }

    fun toggleDeepCleanSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(
            showDeepCleanSheet = show,
            deepCleanProgress  = null
        )
        if (show) {
            startDeepCleanScan()
        }
    }

    fun startDeepCleanScan() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeepCleanScanning = true)
            repository.computeHashesForDuplicates()
            val result = repository.performDeepCleanScan()
            _uiState.value = _uiState.value.copy(
                isDeepCleanScanning = false,
                deepCleanScanResult = result
            )
        }
    }

    fun toggleDeepCleanCategory(type: DeepCleanType) {
        val currentResult = _uiState.value.deepCleanScanResult ?: return
        val updatedCategories = currentResult.categories.map { cat ->
            if (cat.type == type) cat.copy(isSelected = !cat.isSelected)
            else cat
        }
        val updatedResult = currentResult.copy(categories = updatedCategories)
        _uiState.value = _uiState.value.copy(deepCleanScanResult = updatedResult)
    }

    fun executeDeepClean() {
        val currentResult = _uiState.value.deepCleanScanResult ?: return
        val selectedFiles = currentResult.categories
            .filter { it.isSelected }
            .flatMap { it.files }
            .distinctBy { it.id }

        if (selectedFiles.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDeepCleanCleaning = true,
                deepCleanProgress   = DeepCleanProgress(
                    filesDeleted = 0,
                    totalFiles   = selectedFiles.size,
                    bytesFreed   = 0L,
                    isComplete   = false
                )
            )

            val totalFreed = repository.permanentlyDeleteFiles(selectedFiles) { done, total, freed ->
                _uiState.value = _uiState.value.copy(
                    deepCleanProgress = DeepCleanProgress(
                        filesDeleted = done,
                        totalFiles   = total,
                        bytesFreed   = freed,
                        isComplete   = (done == total)
                    )
                )
            }

            val newResult = repository.performDeepCleanScan()
            _uiState.value = _uiState.value.copy(
                isDeepCleanCleaning = false,
                deepCleanScanResult = newResult,
                deepCleanProgress   = DeepCleanProgress(
                    filesDeleted = selectedFiles.size,
                    totalFiles   = selectedFiles.size,
                    bytesFreed   = totalFreed,
                    isComplete   = true
                )
            )
        }
    }

    fun selectFileDetails(file: FileEntity?) {
        _uiState.value = _uiState.value.copy(selectedFileForDetails = file)
    }

    // ------------------------------------------------------------------
    // File operations
    // ------------------------------------------------------------------

    fun deleteFile(file: FileEntity) {
        viewModelScope.launch {
            repository.deleteFile(file)
            if (_uiState.value.selectedFileForDetails?.id == file.id) {
                _uiState.value = _uiState.value.copy(selectedFileForDetails = null)
            }
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private fun countActiveFilters(filter: SearchFilter): Int {
        var c = 0
        if (filter.category != FileCategory.ALL)          c++
        if (!filter.extension.isNullOrEmpty())            c++
        if (filter.minSize != null || filter.maxSize != null) c++
        if (filter.modifiedAfter != null)                 c++
        return c
    }
}
