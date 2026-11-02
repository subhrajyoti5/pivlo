package com.subhrajyoti.pivlo.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// PIVLO Design Tokens — Editorial Minimalism × Swiss Design × Understated Luxury
//
// Philosophy: Near-monochrome base. One precise accent.
//             Colour communicates meaning, never decoration.
// ─────────────────────────────────────────────────────────────────────────────

// Light palette
val LightBackground         = Color(0xFFF8F7F5)  // warm off-white, never pure white
val LightSurface            = Color(0xFFFFFFFF)
val LightSurfaceVariant     = Color(0xFFF1F0EE)
val LightOnBackground       = Color(0xFF111111)  // near-black, not true black
val LightOnSurface          = Color(0xFF1A1A1A)
val LightOnSurfaceVariant   = Color(0xFF888887)  // single muted grey
val LightPrimary            = Color(0xFF1A1A1A)  // primary = near-black (not violet)
val LightOnPrimary          = Color(0xFFF8F7F5)
val LightPrimaryContainer   = Color(0xFFF1F0EE)
val LightOnPrimaryContainer = Color(0xFF111111)
val LightOutline            = Color(0xFFE8E7E5)
val LightOutlineVariant     = Color(0xFFF1F0EE)
val LightAccent             = Color(0xFF4A4AFF)  // single accent — precise indigo

// Dark palette
val DarkBackground          = Color(0xFF0E0E0F)  // near-black, not cool grey
val DarkSurface             = Color(0xFF161618)
val DarkSurfaceVariant      = Color(0xFF1F1F22)
val DarkOnBackground        = Color(0xFFF2F2F0)
val DarkOnSurface           = Color(0xFFEAEAE8)
val DarkOnSurfaceVariant    = Color(0xFF808080)
val DarkPrimary             = Color(0xFFF2F2F0)  // primary = near-white
val DarkOnPrimary           = Color(0xFF0E0E0F)
val DarkPrimaryContainer    = Color(0xFF1F1F22)
val DarkOnPrimaryContainer  = Color(0xFFF2F2F0)
val DarkOutline             = Color(0xFF282829)
val DarkOutlineVariant      = Color(0xFF1F1F22)
val DarkAccent              = Color(0xFF6B6BFF)  // accent lifted for dark context

// ─────────────────────────────────────────────────────────────────────────────
// File category accent marks — restrained, purposeful
// Used only as thin left-border rules, NOT as icon background fills
// ─────────────────────────────────────────────────────────────────────────────
val CategoryDocumentColor   = Color(0xFF2563EB)  // blue
val CategoryImageColor      = Color(0xFFDC2626)  // red
val CategoryAudioColor      = Color(0xFF059669)  // green
val CategoryVideoColor      = Color(0xFF7C3AED)  // violet
val CategoryArchiveColor    = Color(0xFFD97706)  // amber
val CategoryAppColor        = Color(0xFF0891B2)  // cyan
val CategoryDevColor        = Color(0xFFEA580C)  // orange
val CategoryOtherColor      = Color(0xFF6B7280)  // neutral
