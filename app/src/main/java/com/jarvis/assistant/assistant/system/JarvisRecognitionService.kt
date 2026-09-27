package com.jarvis.assistant.assistant.system

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.speech.RecognitionListener
import android.speech.RecognitionService
import android.speech.SpeechRecognizer

/**
 * Motor del rol ASSISTANT. Delega en un reconocedor instalado por el sistema y reenvía
 * todos sus eventos, evitando que Jarvis se seleccione recursivamente a sí mismo.
 */
class JarvisRecognitionService : RecognitionService() {
    private var delegate: SpeechRecognizer? = null
    private var activeCallback: Callback? = null

    override fun onStartListening(recognizerIntent: Intent?, listener: Callback) {
        releaseDelegate()
        val component = findSystemRecognizer()
        if (component == null) {
            listener.error(SpeechRecognizer.ERROR_CLIENT)
            return
        }
        activeCallback = listener
        delegate = SpeechRecognizer.createSpeechRecognizer(this, component).also { recognizer ->
            recognizer.setRecognitionListener(ForwardingListener(listener))
            recognizer.startListening(recognizerIntent ?: Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH))
        }
    }

    override fun onCancel(listener: Callback) {
        if (activeCallback === listener) releaseDelegate(cancel = true)
    }

    override fun onStopListening(listener: Callback) {
        if (activeCallback === listener) delegate?.stopListening()
    }

    override fun onDestroy() {
        releaseDelegate(cancel = true)
        super.onDestroy()
    }

    private fun findSystemRecognizer(): ComponentName? {
        val intent = Intent(RecognitionService.SERVICE_INTERFACE)
        val services = if (Build.VERSION.SDK_INT >= 33) {
            packageManager.queryIntentServices(
                intent,
                android.content.pm.PackageManager.ResolveInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentServices(intent, 0)
        }
        return services.asSequence()
            .mapNotNull { info ->
                val service = info.serviceInfo ?: return@mapNotNull null
                ComponentName(service.packageName, service.name)
            }
            .firstOrNull { it.packageName != packageName }
    }

    private fun releaseDelegate(cancel: Boolean = false) {
        val current = delegate
        delegate = null
        activeCallback = null
        if (cancel) current?.cancel()
        current?.destroy()
    }

    private inner class ForwardingListener(private val callback: Callback) : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = callback.readyForSpeech(params ?: Bundle())
        override fun onBeginningOfSpeech() = callback.beginningOfSpeech()
        override fun onRmsChanged(rmsdB: Float) = callback.rmsChanged(rmsdB)
        override fun onBufferReceived(buffer: ByteArray?) = callback.bufferReceived(buffer ?: byteArrayOf())
        override fun onEndOfSpeech() = callback.endOfSpeech()
        override fun onError(error: Int) {
            callback.error(error)
            releaseDelegate()
        }
        override fun onResults(results: Bundle?) {
            callback.results(results ?: Bundle())
            releaseDelegate()
        }
        override fun onPartialResults(partialResults: Bundle?) = callback.partialResults(partialResults ?: Bundle())
        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }
}
