package com.jarvis.assistant.assistant.system

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class TextToSpeechState(
    val isReady: Boolean = false,
    val isSpeaking: Boolean = false,
    val error: String? = null
)

/** Síntesis local con el motor TTS instalado en Android. */
class TextToSpeechController(context: Context) : TextToSpeech.OnInitListener {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val callbacks = ConcurrentHashMap<String, () -> Unit>()
    private val mutableState = MutableStateFlow(TextToSpeechState())
    val state: StateFlow<TextToSpeechState> = mutableState.asStateFlow()
    private var pendingSpeech: Pair<String, (() -> Unit)?>? = null
    private var engine: TextToSpeech? = TextToSpeech(context.applicationContext, this)

    override fun onInit(status: Int) {
        val tts = engine ?: return
        if (status != TextToSpeech.SUCCESS) {
            mutableState.value = TextToSpeechState(error = "No se pudo iniciar el motor TTS")
            return
        }
        val preferred = Locale.getDefault()
        val languageStatus = tts.setLanguage(preferred)
        if (languageStatus == TextToSpeech.LANG_MISSING_DATA || languageStatus == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts.setLanguage(Locale.forLanguageTag("es-ES"))
        }
        tts.setSpeechRate(1.05f)
        tts.setPitch(1.0f)
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                mutableState.value = TextToSpeechState(isReady = true, isSpeaking = true)
            }

            override fun onDone(utteranceId: String?) = finishUtterance(utteranceId)

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) = finishUtterance(utteranceId, "Error de síntesis de voz")

            override fun onError(utteranceId: String?, errorCode: Int) =
                finishUtterance(utteranceId, "Error TTS ($errorCode)")
        })
        mutableState.value = TextToSpeechState(isReady = true)
        pendingSpeech?.also { (text, callback) ->
            pendingSpeech = null
            speak(text, callback)
        }
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        val normalized = text.trim().take(MAX_SPOKEN_CHARS)
        if (normalized.isBlank()) {
            onDone?.invoke()
            return
        }
        val tts = engine
        if (tts == null || !mutableState.value.isReady) {
            pendingSpeech = normalized to onDone
            return
        }
        val utteranceId = UUID.randomUUID().toString()
        onDone?.let { callbacks[utteranceId] = it }
        val result = tts.speak(normalized, TextToSpeech.QUEUE_FLUSH, Bundle(), utteranceId)
        if (result == TextToSpeech.ERROR) {
            callbacks.remove(utteranceId)
            mutableState.value = TextToSpeechState(isReady = true, error = "No se pudo reproducir la respuesta")
            onDone?.invoke()
        }
    }

    fun stop() {
        pendingSpeech = null
        callbacks.clear()
        engine?.stop()
        mutableState.value = mutableState.value.copy(isSpeaking = false)
    }

    fun shutdown() {
        stop()
        engine?.shutdown()
        engine = null
        mutableState.value = TextToSpeechState()
    }

    private fun finishUtterance(utteranceId: String?, error: String? = null) {
        val callback = utteranceId?.let(callbacks::remove)
        mutableState.value = TextToSpeechState(isReady = true, error = error)
        callback?.let { mainHandler.post(it) }
    }

    private companion object {
        const val MAX_SPOKEN_CHARS = 1_500
    }
}
