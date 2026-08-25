package com.learnlettersnumbers.app

import android.content.Context

object ChildProfileRepository {
    private lateinit var appContext: Context

    fun init(context: Context) { appContext = context.applicationContext }
    private val prefs get() = appContext.getSharedPreferences("child_profile", Context.MODE_PRIVATE)
    fun loadName(): String = if (::appContext.isInitialized) prefs.getString("name", "") ?: "" else ""
    fun loadAvatar(): String = if (::appContext.isInitialized) prefs.getString("avatar", "") ?: "" else ""
    fun saveName(name: String) { if (::appContext.isInitialized) prefs.edit().putString("name", name).putBoolean("prompt_seen", true).apply() }
    fun saveAvatar(value: String) { if (::appContext.isInitialized) prefs.edit().putString("avatar", value).apply() }
    fun promptSeen(): Boolean = if (::appContext.isInitialized) prefs.getBoolean("prompt_seen", false) else false
    fun markPromptSeen() { if (::appContext.isInitialized) prefs.edit().putBoolean("prompt_seen", true).apply() }
    fun clear() { if (::appContext.isInitialized) prefs.edit().clear().apply() }
}
