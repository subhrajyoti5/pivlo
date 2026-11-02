package com.subhrajyoti.pivlo.ui.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ─────────────────────────────────────────────────────────────────────────────
// ThemePreference — the three possible states
// ─────────────────────────────────────────────────────────────────────────────

enum class ThemePreference {
    SYSTEM,  // follow the OS setting (default)
    LIGHT,
    DARK;

    fun next(): ThemePreference = when (this) {
        SYSTEM -> LIGHT
        LIGHT  -> DARK
        DARK   -> SYSTEM
    }

    val label: String get() = when (this) {
        SYSTEM -> "System"
        LIGHT  -> "Light"
        DARK   -> "Dark"
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ThemeStore — thin SharedPreferences wrapper
// Exposes a StateFlow so the Compose tree reacts instantly on change.
// ─────────────────────────────────────────────────────────────────────────────

object ThemeStore {

    private const val PREFS_NAME = "pivlo_prefs"
    private const val KEY_THEME  = "theme_preference"

    private lateinit var prefs: SharedPreferences
    private val _theme = MutableStateFlow(ThemePreference.SYSTEM)

    /** Call once from Application or MainActivity.onCreate() */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_THEME, ThemePreference.SYSTEM.name)
        _theme.value = try {
            ThemePreference.valueOf(saved ?: ThemePreference.SYSTEM.name)
        } catch (_: Exception) {
            ThemePreference.SYSTEM
        }
    }

    val theme: StateFlow<ThemePreference> = _theme.asStateFlow()

    fun set(preference: ThemePreference) {
        _theme.value = preference
        prefs.edit().putString(KEY_THEME, preference.name).apply()
    }

    fun cycle() = set(_theme.value.next())
}

