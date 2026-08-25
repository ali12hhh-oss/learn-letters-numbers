package com.learnlettersnumbers.app

import android.content.Context

/** Compatibility wrapper; SettingsRepository is the single source of truth. */
class ThemeRepository(context: Context) {
    private val settings = SettingsRepository(context)
    fun isDark(): Boolean = settings.darkMode()
    fun setDark(value: Boolean) = settings.setDarkMode(value)
}
