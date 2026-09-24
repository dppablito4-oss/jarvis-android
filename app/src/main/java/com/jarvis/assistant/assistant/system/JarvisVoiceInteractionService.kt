package com.jarvis.assistant.assistant.system

import android.os.Bundle
import android.service.voice.VoiceInteractionService
import android.service.voice.VoiceInteractionSession

/** Punto de entrada del rol de asistente del sistema. */
class JarvisVoiceInteractionService : VoiceInteractionService() {
    companion object {
        var activeService: JarvisVoiceInteractionService? = null
            private set

        fun showCurrentSession() {
            activeService?.showSession(
                Bundle(),
                VoiceInteractionSession.SHOW_WITH_ASSIST or VoiceInteractionSession.SHOW_WITH_SCREENSHOT
            )
        }
    }

    override fun onReady() {
        super.onReady()
        activeService = this
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activeService === this) activeService = null
    }
}
