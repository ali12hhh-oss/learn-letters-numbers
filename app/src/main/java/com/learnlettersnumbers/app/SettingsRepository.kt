package com.learnlettersnumbers.app

import android.content.Context

class SettingsRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    fun soundsEnabled(): Boolean = prefs.getBoolean("sounds_enabled", true)
    fun effectsEnabled(): Boolean = prefs.getBoolean("effects_enabled", true)
    fun darkMode(): Boolean = prefs.getBoolean("dark_mode", false)

    fun setSoundsEnabled(value: Boolean) = prefs.edit().putBoolean("sounds_enabled", value).apply()
    fun setEffectsEnabled(value: Boolean) = prefs.edit().putBoolean("effects_enabled", value).apply()
    fun setDarkMode(value: Boolean) = prefs.edit().putBoolean("dark_mode", value).apply()

    fun clearChildData() {
        context.getSharedPreferences("child_profile", Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences("child_progress_v1", Context.MODE_PRIVATE).edit().clear().apply()
    }
}
