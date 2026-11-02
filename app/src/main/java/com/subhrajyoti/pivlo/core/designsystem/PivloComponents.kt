package com.subhrajyoti.pivlo.core.designsystem

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subhrajyoti.pivlo.core.storage.FileCategory
import com.subhrajyoti.pivlo.ui.theme.LocalPivloExtended
import com.subhrajyoti.pivlo.ui.theme.ThemePreference
import com.subhrajyoti.pivlo.ui.theme.ThemeStore

// ─────────────────────────────────────────────────────────────────────────────
// PivloWordmark — editorial logo lockup
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PivloWordmark(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text  = "pivlo.",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight    = FontWeight.Black,
                letterSpacing = (-1).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PivloThemeToggle — opens theme selection dialog (Light, Dark, System)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PivloThemeToggle(modifier: Modifier = Modifier) {
    val pref by ThemeStore.theme.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    val icon: ImageVector = when (pref) {
        ThemePreference.SYSTEM -> Icons.Default.BrightnessAuto
        ThemePreference.LIGHT  -> Icons.Default.LightMode
        ThemePreference.DARK   -> Icons.Default.DarkMode
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick           = { showDialog = true }
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = "Theme: ${pref.label}",
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(15.dp)
            )
            Text(
                text  = pref.label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showDialog) {
        PivloThemeSelectionDialog(
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun PivloThemeSelectionDialog(onDismiss: () -> Unit) {
    val currentPref by ThemeStore.theme.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text  = "Theme",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ThemeOptionItem(
                    label    = "System default",
                    icon     = Icons.Default.BrightnessAuto,
                    selected = currentPref == ThemePreference.SYSTEM,
                    onClick  = {
                        ThemeStore.set(ThemePreference.SYSTEM)
                        onDismiss()
                    }
                )
                ThemeOptionItem(
                    label    = "Light theme",
                    icon     = Icons.Default.LightMode,
                    selected = currentPref == ThemePreference.LIGHT,
                    onClick  = {
                        ThemeStore.set(ThemePreference.LIGHT)
                        onDismiss()
                    }
                )
                ThemeOptionItem(
                    label    = "Dark theme",
                    icon     = Icons.Default.DarkMode,
                    selected = currentPref == ThemePreference.DARK,
                    onClick  = {
                        ThemeStore.set(ThemePreference.DARK)
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
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

@Composable
private fun ThemeOptionItem(
    label:    String,
    icon:     ImageVector,
    selected: Boolean,
    onClick:  () -> Unit
) {
    val extended = LocalPivloExtended.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.surfaceVariant
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = if (selected) extended.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(18.dp)
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(extended.accent)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PivloHeader — wordmark + optional trailing action
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PivloHeader(
    modifier:        Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        PivloWordmark()
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            PivloThemeToggle()
            trailingContent?.invoke()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PivloSearchBar — the hero element.
//
// Design decisions:
//   · No outlined box — the search field IS the surface
//   · Monospaced-feeling cursor on dark underline rule only
//   · Filter affordance integrated, not bolted on
//   · Status text flows inline — no separate indicator card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PivloSearchBar(
    query:              String,
    onQueryChange:      (String) -> Unit,
    onSearch:           (String) -> Unit,
    modifier:           Modifier = Modifier,
    activeFilterCount:  Int = 0,
    onFilterClick:      () -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val extended           = LocalPivloExtended.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Default.Search,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text  = "Search files…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                BasicTextField(
                    value             = query,
                    onValueChange     = onQueryChange,
                    textStyle         = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    cursorBrush       = SolidColor(extended.accent),
                    singleLine        = true,
                    keyboardOptions   = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions   = KeyboardActions(onSearch = {
                        keyboardController?.hide()
                        onSearch(query)
                    }),
                    modifier          = Modifier.fillMaxWidth()
                )
            }

            AnimatedVisibility(visible = query.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                IconButton(
                    onClick  = { onQueryChange("") },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(16.dp)
                    )
                }
            }

            // Filter pill — shows count badge when active
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (activeFilterCount > 0)
                            extended.accent.copy(alpha = 0.08f)
                        else Color.Transparent
                    )
                    .clickable(
                        indication            = null,
                        interactionSource     = remember { MutableInteractionSource() },
                        onClick               = onFilterClick
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Default.Tune,
                        contentDescription = "Filters",
                        tint               = if (activeFilterCount > 0) extended.accent
                                             else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(17.dp)
                    )
                    if (activeFilterCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text  = activeFilterCount.toString(),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = extended.accent
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Hairline rule — the only border on the search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PivloStatusStrip — replaces the noisy IndexingIndicator card
//
// One quiet line of status beneath the search rule.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PivloStatusStrip(
    isIndexing:   Boolean,
    indexedCount: Int,
    statusText:   String,
    modifier:     Modifier = Modifier
) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AnimatedContent(
            targetState = statusText,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "status"
        ) { text ->
            Text(
                text  = if (isIndexing) text else "$indexedCount files indexed",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        if (isIndexing) {
            // Animated dot-pulse instead of a progress bar
            PulsingDots()
        }
    }
}

@Composable
private fun PulsingDots() {
    val transition = rememberInfiniteTransition(label = "dots")
    val alpha by transition.animateFloat(
        initialValue  = 0.3f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label         = "pulse"
    )
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(3) { i ->
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        LocalPivloExtended.current.accent.copy(
                            alpha = (alpha - i * 0.25f).coerceIn(0.1f, 1f)
                        )
                    )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PivloFilterChip — refined, restrained
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PivloFilterChip(
    label:    String,
    selected: Boolean,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LocalPivloExtended.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(
                when {
                    selected -> extended.accent.copy(alpha = 0.09f)
                    else     -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick           = onClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text  = label.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            ),
            color = when {
                selected -> extended.accent
                else     -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PivloFileRow — Swiss grid.
//
// Design decisions:
//   · Left accent rule (2dp) carries the category colour — not an icon box
//   · One row of text per information tier, not three stacked bubbles
//   · Extension uppercase, category colour, right-aligned — acts as a label
//   · No icon boxes — typography does the visual work
//   · Availability status is communicated via text colour + label, not badge
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PivloFileRow(
    fileName:           String,
    extension:          String,
    formattedSize:      String,
    formattedDate:      String,
    path:               String,
    category:           FileCategory,
    availabilityStatus: String,
    onClick:            () -> Unit,
    onOverflowClick:    () -> Unit,
    modifier:           Modifier = Modifier
) {
    val isMissing = availabilityStatus.equals("MISSING", ignoreCase = true)
    val isTrashed = availabilityStatus.equals("TRASHED", ignoreCase = true)
    val isUnavailable = isMissing || isTrashed

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick           = onClick
            )
    ) {
        // Category accent rule — 2dp left edge
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(64.dp)
                .background(
                    if (isUnavailable) category.color.copy(alpha = 0.25f)
                    else category.color.copy(alpha = 0.7f)
                )
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 0.dp)
        ) {
            // ── Row 1: filename + availability label ──────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text     = fileName,
                    style    = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color    = if (isUnavailable)
                                   MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                               else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (isMissing || isTrashed) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text  = if (isMissing) "MISSING" else "TRASHED",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isMissing) Color(0xFFDC2626).copy(alpha = 0.8f)
                                else Color(0xFFD97706).copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // ── Row 2: path / location ────────────────────────────────────
            Text(
                text     = path,
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ── Row 3: metadata baseline ──────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text  = formattedSize,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                if (extension.isNotBlank()) {
                    MetaDot()
                    Text(
                        text  = extension.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = if (isUnavailable) category.color.copy(alpha = 0.35f)
                                else category.color.copy(alpha = 0.8f)
                    )
                }
                MetaDot()
                Text(
                    text  = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }

        // Overflow tap target — ghost, no visible icon until needed
        Box(
            modifier          = Modifier
                .size(52.dp)
                .clickable(
                    indication        = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick           = onOverflowClick
                ),
            contentAlignment  = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaDot() {
    Text(
        text  = "  ·  ",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// PivloCategoryIcon — kept for details screen only, refined
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PivloCategoryIcon(
    category: FileCategory,
    size:     Int = 40,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when (category) {
        FileCategory.DOCUMENTS    -> Icons.Default.Description
        FileCategory.IMAGES       -> Icons.Default.Image
        FileCategory.AUDIO        -> Icons.Default.AudioFile
        FileCategory.VIDEO        -> Icons.Default.Movie
        FileCategory.ARCHIVES     -> Icons.Default.FolderZip
        FileCategory.APPLICATIONS -> Icons.Default.Android
        FileCategory.DEVELOPMENT  -> Icons.Default.Code
        else                      -> Icons.Default.Description
    }

    Box(
        modifier         = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(category.color.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = category.displayName,
            tint               = category.color.copy(alpha = 0.75f),
            modifier           = Modifier.size((size * 0.5f).dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PivloEmptyState — editorial, not illustrative
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PivloEmptyState(
    title:          String,
    description:    String,
    modifier:       Modifier = Modifier,
    actionLabel:    String? = null,
    onActionClick:  (() -> Unit)? = null
) {
    Column(
        modifier            = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 64.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Accent rule above the heading — Swiss editorial device
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(2.dp)
                .background(LocalPivloExtended.current.accent.copy(alpha = 0.4f))
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text  = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text  = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(28.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.onBackground)
                    .clickable(
                        indication        = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick           = onActionClick
                    )
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text  = actionLabel,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.background
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SkeletonFileRow — refined loading placeholder
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SkeletonFileRow() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue  = -600f,
        targetValue   = 600f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart),
        label         = "shimmerX"
    )
    val shimmer = MaterialTheme.colorScheme.surfaceVariant

    val brush = Brush.linearGradient(
        colors = listOf(
            shimmer,
            shimmer.copy(alpha = 0.3f),
            shimmer
        ),
        start = Offset(x, 0f),
        end   = Offset(x + 300f, 100f)
    )

    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.width(2.dp).height(64.dp).background(shimmer))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, top = 14.dp, bottom = 14.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth(0.6f).height(13.dp).clip(RoundedCornerShape(2.dp)).background(brush))
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.85f).height(10.dp).clip(RoundedCornerShape(2.dp)).background(brush))
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.4f).height(10.dp).clip(RoundedCornerShape(2.dp)).background(brush))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Section label helper
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PivloSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text     = text.uppercase(),
        style    = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = modifier
    )
}
