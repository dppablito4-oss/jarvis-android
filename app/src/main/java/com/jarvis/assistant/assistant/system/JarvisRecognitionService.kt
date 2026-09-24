package com.jarvis.assistant.assistant.system

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionService
import android.speech.SpeechRecognizer

/**
 * Servicio declarado para completar la integración con ROLE_ASSISTANT.
 * El motor streaming se conectará aquí; por ahora falla de forma explícita y segura.
 */
class JarvisRecognitionService : RecognitionService() {
    override fun onStartListening(recognizerIntent: Intent?, listener: Callback) {
        listener.error(SpeechRecognizer.ERROR_CLIENT)
    }

    override fun onCancel(listener: Callback) = Unit

    override fun onStopListening(listener: Callback) {
        listener.results(Bundle().apply {
            putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, arrayListOf())
        })
    }
}
