package com.jarvis.assistant

import android.app.Application
import com.jarvis.assistant.assistant.voice.OpenAiAssistantGateway
import com.jarvis.assistant.runtime.JarvisRuntime
import com.jarvis.assistant.security.OpenAiKeyStore

class JarvisApplication : Application() {
    private val keyStore by lazy { OpenAiKeyStore(this) }
    @Volatile private var runtimeInstance: JarvisRuntime? = null

    val runtime: JarvisRuntime
        get() = runtimeInstance ?: synchronized(this) {
            runtimeInstance ?: createRuntime().also { runtimeInstance = it }
        }

    fun configureOpenAiKey(apiKey: String): JarvisRuntime = synchronized(this) {
        if (apiKey.isBlank()) keyStore.clear() else keyStore.save(apiKey)
        createRuntime().also { runtimeInstance = it }
    }

    private fun createRuntime(): JarvisRuntime {
        val gateway = keyStore.read()
            ?.let { key -> OpenAiAssistantGateway(key, BuildConfig.OPENAI_MODEL) }
        return JarvisRuntime(this, gateway)
    }
}
