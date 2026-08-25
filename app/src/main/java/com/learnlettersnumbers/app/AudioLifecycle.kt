package com.learnlettersnumbers.app

/** Lifecycle helper for the local-only audio engine. */
object AudioLifecycle {
    fun stop(audio: LocalAudioManager) = audio.stop()
}
