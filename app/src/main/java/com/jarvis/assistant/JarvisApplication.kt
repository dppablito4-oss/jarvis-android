package com.jarvis.assistant

import android.app.Application
import com.jarvis.assistant.assistant.voice.OpenAiAssistantGateway
import com.jarvis.assistant.runtime.JarvisRuntime

class JarvisApplication : Application() {
    val runtime: JarvisRuntime by lazy {
        val gateway = BuildConfig.OPENAI_API_KEY
            .takeIf(String::isNotBlank)
            ?.let { key -> OpenAiAssistantGateway(key, BuildConfig.OPENAI_MODEL) }
        JarvisRuntime(this, gateway)
    }
}
