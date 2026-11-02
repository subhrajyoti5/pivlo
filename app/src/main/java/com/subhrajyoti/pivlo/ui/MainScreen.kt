package com.subhrajyoti.pivlo.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.subhrajyoti.pivlo.data.clean.DeepCleanType
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.subhrajyoti.pivlo.core.designsystem.PivloCategoryIcon
import com.subhrajyoti.pivlo.core.designsystem.PivloEmptyState
import com.subhrajyoti.pivlo.core.designsystem.PivloFileRow
import com.subhrajyoti.pivlo.core.designsystem.PivloFilterChip
import com.subhrajyoti.pivlo.core.designsystem.PivloHeader
import com.subhrajyoti.pivlo.core.designsystem.PivloSearchBar
import com.subhrajyoti.pivlo.core.designsystem.PivloSectionLabel
import com.subhrajyoti.pivlo.core.designsystem.PivloStatusStrip
import com.subhrajyoti.pivlo.core.designsystem.SkeletonFileRow
import com.subhrajyoti.pivlo.core.storage.FileCategory
import com.subhrajyoti.pivlo.data.local.entity.FileEntity
import com.subhrajyoti.pivlo.data.search.SortOption
import com.subhrajyoti.pivlo.ui.theme.LocalPivloExtended
import com.subhrajyoti.pivlo.ui.theme.ThemePreference
import com.subhrajyoti.pivlo.ui.theme.ThemeStore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// MainScreen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context    = LocalContext.current
    val uiState    by viewModel.uiState.collectAsState()
    val total      by viewModel.totalCount.collectAsState()
    val extensions by viewModel.discoveredExtensions.collectAsState()

    val lazyItems: LazyPagingItems<FileEntity> =
        viewModel.pagedFiles.collectAsLazyPagingItems()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val extended   = LocalPivloExtended.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // ── Header ──────────────────────────────────────────────────────
            PivloHeader(
                trailingContent = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text     = "Deep Clean",
                            style    = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color    = Color(0xFFDC2626),
                            modifier = Modifier.clickable(
                                indication        = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick           = { viewModel.toggleDeepCleanSheet(true) }
                            )
                        )
                        Text(
                            text     = "Duplicates",
                            style    = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                            color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.clickable(
                                indication        = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick           = { viewModel.findDuplicates() }
                            )
                        )
                    }
                }
            )

            // ── Search bar ──────────────────────────────────────────────────
            PivloSearchBar(
                query             = uiState.query,
                onQueryChange     = viewModel::onQueryChanged,
                onSearch          = { viewModel.onQueryChanged(it) },
                activeFilterCount = uiState.activeFilterCount,
                onFilterClick     = { viewModel.toggleFilterSheet(true) }
            )

            // ── Status strip ────────────────────────────────────────────────
            PivloStatusStrip(
                isIndexing   = uiState.isIndexing,
                indexedCount = total,
                statusText   = uiState.indexingStatus
            )

            // ── Category rail — only when not actively typing ───────────────
            AnimatedVisibility(visible = uiState.query.isEmpty()) {
                LazyRow(
                    modifier            = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        listOf(
                            FileCategory.ALL, FileCategory.DOCUMENTS, FileCategory.IMAGES,
                            FileCategory.AUDIO, FileCategory.VIDEO, FileCategory.ARCHIVES,
                            FileCategory.APPLICATIONS, FileCategory.DEVELOPMENT
                        )
                    ) { cat ->
                        PivloFilterChip(
                            label    = cat.displayName,
                            selected = uiState.filter.category == cat,
                            onClick  = { viewModel.onCategorySelected(cat) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── File list ───────────────────────────────────────────────────
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {

                    // Result count header — only when results exist
                    when (val refresh = lazyItems.loadState.refresh) {
                        is LoadState.Loading -> skeletonItems()
                        is LoadState.Error   -> item(key = "err_refresh") {
                            ErrorItem(refresh.error.localizedMessage ?: "Load error") {
                                lazyItems.retry()
                            }
                        }
                        else -> {
                            if (lazyItems.itemCount == 0) {
                                item(key = "empty") {
                                    PivloEmptyState(
                                        title       = if (uiState.query.isEmpty())
                                            "Nothing indexed yet"
                                        else "No results",
                                        description = if (uiState.query.isEmpty())
                                            "Grant storage permission and tap the rescan indicator above."
                                        else
                                            "Try different keywords or remove active filters.",
                                        actionLabel = if (uiState.query.isNotEmpty())
                                            "Clear filters" else null,
                                        onActionClick = { viewModel.resetFilters() }
                                    )
                                }
                            } else {
                                item(key = "count_header") {
                                    Text(
                                        text     = "${lazyItems.itemCount} results",
                                        style    = MaterialTheme.typography.labelLarge,
                                        color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // File rows
                    items(
                        count       = lazyItems.itemCount,
                        key         = lazyItems.itemKey { it.id },
                        contentType = lazyItems.itemContentType { "file" }
                    ) { index ->
                        val file = lazyItems[index]
                        if (file == null) {
                            SkeletonFileRow()
                        } else {
                            PivloFileRow(
                                fileName           = file.name,
                                extension          = file.extension,
                                formattedSize      = formatBytes(file.sizeBytes),
                                formattedDate      = formatDate(file.modifiedAt),
                                path               = file.path ?: file.uri,
                                category           = file.category,
                                availabilityStatus = file.availabilityStatus,
                                onClick            = { openFile(context, file) },
                                onOverflowClick    = { viewModel.selectFileDetails(file) }
                            )
                            // Hairline rule
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 18.dp) // offset past the accent rule
                                    .height(0.5.dp)
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                            )
                        }
                    }

                    // Append / next-page loader
                    when (val append = lazyItems.loadState.append) {
                        is LoadState.Loading -> item(key = "load_more") {
                            Box(
                                modifier         = Modifier.fillMaxWidth().padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(18.dp),
                                    strokeWidth = 1.5.dp,
                                    color       = extended.accent.copy(alpha = 0.6f)
                                )
                            }
                        }
                        is LoadState.Error   -> item(key = "err_append") {
                            ErrorItem(append.error.localizedMessage ?: "Error") {
                                lazyItems.retry()
                            }
                        }
                        else -> {}
                    }
                }
            }
        }

        // ── Filter sheet ─────────────────────────────────────────────────────
        if (uiState.showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest  = { viewModel.toggleFilterSheet(false) },
                sheetState        = sheetState,
                containerColor    = MaterialTheme.colorScheme.surface
            ) {
                FilterSheet(
                    currentFilter       = uiState.filter,
                    availableExtensions = extensions.take(30),
                    onCategorySelect    = viewModel::onCategorySelected,
                    onExtensionSelect   = viewModel::onExtensionSelected,
                    onSortSelect        = viewModel::onSortSelected,
                    onReset             = viewModel::resetFilters,
                    onClose             = { viewModel.toggleFilterSheet(false) }
                )
            }
        }

        // ── Deep Clean sheet ──────────────────────────────────────────────────
        if (uiState.showDeepCleanSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.toggleDeepCleanSheet(false) },
                sheetState       = sheetState,
                containerColor   = MaterialTheme.colorScheme.surface
            ) {
                DeepCleanSheet(
                    uiState          = uiState,
                    onToggleCategory = viewModel::toggleDeepCleanCategory,
                    onExecuteClean   = viewModel::executeDeepClean,
                    onClose          = { viewModel.toggleDeepCleanSheet(false) }
                )
            }
        }

        // ── File details sheet ───────────────────────────────────────────────
        uiState.selectedFileForDetails?.let { file ->
            ModalBottomSheet(
                onDismissRequest = { viewModel.selectFileDetails(null) },
                sheetState       = sheetState,
                containerColor   = MaterialTheme.colorScheme.surface
            ) {
                FileDetailsSheet(
                    file     = file,
                    onOpen   = { openFile(context, file) },
                    onShare  = { shareFile(context, file) },
                    onDelete = {
                        viewModel.deleteFile(file)
                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                    },
                    onClose  = { viewModel.selectFileDetails(null) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Filter sheet — disciplined typographic layout
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSheet(
    currentFilter:       com.subhrajyoti.pivlo.data.search.SearchFilter,
    availableExtensions: List<String>,
    onCategorySelect:    (FileCategory) -> Unit,
    onExtensionSelect:   (String?) -> Unit,
    onSortSelect:        (SortOption) -> Unit,
    onReset:             () -> Unit,
    onClose:             () -> Unit
) {
    val extended = LocalPivloExtended.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        // Sheet header
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Bottom
        ) {
            Text(
                text  = "Filter",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text     = "Reset",
                style    = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color    = extended.accent,
                modifier = Modifier.clickable(
                    indication        = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick           = onReset
                )
            )
        }

        // Sort
        PivloSectionLabel(text = "Sort by")
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement   = Arrangement.spacedBy(8.dp)
        ) {
            SortOption.values().forEach { opt ->
                PivloFilterChip(
                    label    = opt.displayName,
                    selected = currentFilter.sortBy == opt,
                    onClick  = { onSortSelect(opt) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Theme
        PivloSectionLabel(text = "Theme")
        Spacer(modifier = Modifier.height(10.dp))
        val currentThemePref by ThemeStore.theme.collectAsState()
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement   = Arrangement.spacedBy(8.dp)
        ) {
            ThemePreference.entries.forEach { pref ->
                PivloFilterChip(
                    label    = pref.label,
                    selected = currentThemePref == pref,
                    onClick  = { ThemeStore.set(pref) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Extensions (only if available)
        if (availableExtensions.isNotEmpty()) {
            PivloSectionLabel(text = "Extensions")
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement   = Arrangement.spacedBy(8.dp)
            ) {
                availableExtensions.forEach { ext ->
                    PivloFilterChip(
                        label    = ".$ext",
                        selected = currentFilter.extension.equals(ext, ignoreCase = true),
                        onClick  = { onExtensionSelect(ext) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Apply — full-width, high-contrast
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.onBackground)
                .clickable(
                    indication        = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick           = onClose
                )
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = "Apply",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.background
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// File details sheet — editorial metadata layout
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FileDetailsSheet(
    file:     FileEntity,
    onOpen:   () -> Unit,
    onShare:  () -> Unit,
    onDelete: () -> Unit,
    onClose:  () -> Unit
) {
    val extended = LocalPivloExtended.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        // ── File identity block ─────────────────────────────────────────────
        Row(verticalAlignment = Alignment.Top) {
            // Category accent rule — matches file row treatment
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(48.dp)
                    .background(file.category.color.copy(alpha = 0.7f))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text     = file.name,
                    style    = MaterialTheme.typography.titleLarge,
                    color    = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text  = file.extension.uppercase().ifEmpty { "File" },
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = file.category.color.copy(alpha = 0.75f)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Metadata grid ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                MetaField("Size",     formatBytes(file.sizeBytes))
                MetaField("Location", file.path ?: file.uri)
                MetaField("Modified", formatDate(file.modifiedAt))
                MetaField("Status",   file.availabilityStatus)
                if (file.contentHash != null) {
                    MetaField("Checksum", file.contentHash.take(20) + "…")
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Actions ─────────────────────────────────────────────────────────
        // Primary: Open
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.onBackground)
                .clickable(
                    indication        = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick           = onOpen
                )
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.background,
                    modifier           = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text  = "Open",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.background
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Secondary: Share + Delete on same row
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(
                        indication        = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick           = onShare
                    )
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Default.Share,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurface,
                        modifier           = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text  = "Share",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFDC2626).copy(alpha = 0.07f))
                    .clickable(
                        indication        = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick           = onDelete
                    )
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Default.Delete,
                        contentDescription = null,
                        tint               = Color(0xFFDC2626).copy(alpha = 0.8f),
                        modifier           = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text  = "Delete",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFFDC2626).copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaField(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.Top
    ) {
        Text(
            text  = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.width(80.dp)
        )
        Text(
            text     = value,
            style    = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color    = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Paging helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun LazyListScope.skeletonItems() {
    items(count = 10, key = { "sk_$it" }, contentType = { "skeleton" }) {
        SkeletonFileRow()
    }
}

@Composable
private fun ErrorItem(message: String, onRetry: () -> Unit) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text  = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(
                    indication        = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick           = onRetry
                )
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text  = "Retry",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Utilities
// ─────────────────────────────────────────────────────────────────────────────

internal fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val group = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceAtMost(units.lastIndex)
    return "%.1f %s".format(bytes / Math.pow(1024.0, group.toDouble()), units[group])
}

internal fun formatDate(timestamp: Long): String =
    SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(timestamp))

internal fun openFile(context: Context, file: FileEntity) {
    try {
        val uri = resolveUri(context, file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, file.mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Open with"))
    } catch (_: Exception) {
        Toast.makeText(context, "Cannot open this file", Toast.LENGTH_SHORT).show()
    }
}

internal fun shareFile(context: Context, file: FileEntity) {
    try {
        val uri = resolveUri(context, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = file.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    } catch (_: Exception) {
        Toast.makeText(context, "Cannot share this file", Toast.LENGTH_SHORT).show()
    }
}

private fun resolveUri(context: Context, file: FileEntity): Uri {
    return if (file.path != null) {
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(file.path))
    } else {
        Uri.parse(file.uri)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Deep Clean Sheet — permanent deletion & storage reclamation
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DeepCleanSheet(
    uiState:          PivloUiState,
    onToggleCategory: (DeepCleanType) -> Unit,
    onExecuteClean:   () -> Unit,
    onClose:          () -> Unit
) {
    val extended = LocalPivloExtended.current
    var showConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        // Sheet Header
        Row(
            modifier              = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text  = "Deep Clean",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text  = "Permanently delete junk & space hogs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text     = "Done",
                style    = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color    = extended.accent,
                modifier = Modifier.clickable(onClick = onClose)
            )
        }

        if (uiState.isDeepCleanScanning) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(28.dp),
                        strokeWidth = 2.dp,
                        color       = extended.accent
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text  = "Analyzing storage for junk and duplicate files…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val result   = uiState.deepCleanScanResult
            val progress = uiState.deepCleanProgress

            if (progress?.isComplete == true) {
                // Success card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF059669).copy(alpha = 0.1f))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text  = "Cleanup Complete",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF059669)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text      = "Permanently deleted ${progress.filesDeleted} files and freed ${formatBytes(progress.bytesFreed)}.",
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            } else if (result != null) {
                val selectedBytes     = result.totalReclaimableBytes
                val selectedFileCount = result.categories.filter { it.isSelected }.sumOf { it.files.size }

                // Hero stats box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(18.dp)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text  = "RECLAIMABLE SPACE",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text  = formatBytes(selectedBytes),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight    = FontWeight.Black,
                                    letterSpacing = (-1).sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text  = "$selectedFileCount files selected",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category checklist
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    result.categories.forEach { cat ->
                        DeepCleanCategoryItem(
                            categoryResult = cat,
                            onToggle       = { onToggleCategory(cat.type) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Permanent deletion warning note
                Text(
                    text  = "⚠️ Selected items will be erased permanently from Android storage.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFDC2626).copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action button
                if (uiState.isDeepCleanCleaning && progress != null) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            progress   = { progress.filesDeleted.toFloat() / progress.totalFiles.coerceAtLeast(1) },
                            modifier   = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color      = Color(0xFFDC2626),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text  = "Deleting permanently: ${progress.filesDeleted} / ${progress.totalFiles} files…",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (selectedFileCount > 0) Color(0xFFDC2626)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable(enabled = selectedFileCount > 0) { showConfirmDialog = true }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = if (selectedFileCount > 0)
                                "Permanently Delete (${formatBytes(selectedBytes)})"
                            else "No Items Selected",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedFileCount > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text  = "Confirm Permanent Deletion",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text  = "Are you sure you want to permanently delete these files? They will be deleted from your Android device storage immediately and cannot be recovered.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        onExecuteClean()
                    }
                ) {
                    Text(
                        text  = "Delete Permanently",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFDC2626)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(
                        text  = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun DeepCleanCategoryItem(
    categoryResult: com.subhrajyoti.pivlo.data.clean.DeepCleanCategoryResult,
    onToggle:       () -> Unit
) {
    val extended = LocalPivloExtended.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (categoryResult.isSelected) MaterialTheme.colorScheme.surfaceVariant
                else Color.Transparent
            )
            .clickable(onClick = onToggle)
            .padding(14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier              = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (categoryResult.isSelected) extended.accent else MaterialTheme.colorScheme.outline
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (categoryResult.isSelected) {
                    Text("✓", style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
            }
            Column {
                Text(
                    text  = categoryResult.type.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text     = categoryResult.type.description,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text  = formatBytes(categoryResult.totalSizeBytes),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text  = "${categoryResult.files.size} files",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
