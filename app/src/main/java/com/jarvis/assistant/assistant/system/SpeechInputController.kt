package com.jarvis.assistant.assistant.system

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class SpeechInputState(
    val isListening: Boolean = false,
    val partialText: String = "",
    val error: String? = null
)

/** Adaptador de voz basado en el motor de reconocimiento configurado por Android. */
class SpeechInputController(
    context: Context,
    private val onListeningError: (String) -> Unit = {},
    private val onFinalText: (String) -> Unit
) : RecognitionListener {
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private val mutableState = MutableStateFlow(SpeechInputState())
    val state: StateFlow<SpeechInputState> = mutableState.asStateFlow()

    fun startListening() {
        mainHandler.post {
            if (appContext.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                mutableState.value = SpeechInputState(error = "Falta permiso de micrófono")
                return@post
            }
            if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
                mutableState.value = SpeechInputState(error = "Android no encontró un motor de reconocimiento")
                return@post
            }
            val engine = recognizer ?: SpeechRecognizer.createSpeechRecognizer(appContext).also {
                recognizer = it
                it.setRecognitionListener(this)
            }
            mutableState.value = SpeechInputState(isListening = true)
            engine.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            })
        }
    }

    fun cancel() {
        mainHandler.post {
            recognizer?.cancel()
            mutableState.value = SpeechInputState()
        }
    }

    fun destroy() {
        mainHandler.post {
            recognizer?.destroy()
            recognizer = null
            mutableState.value = SpeechInputState()
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        mutableState.value = mutableState.value.copy(isListening = true, error = null)
    }

    override fun onBeginningOfSpeech() = Unit
    override fun onRmsChanged(rmsdB: Float) = Unit
    override fun onBufferReceived(buffer: ByteArray?) = Unit
    override fun onEndOfSpeech() {
        mutableState.value = mutableState.value.copy(isListening = false)
    }

    override fun onError(error: Int) {
        val message = errorMessage(error)
        mutableState.value = SpeechInputState(error = message)
        onListeningError(message)
    }

    override fun onResults(results: Bundle?) {
        val text = results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            .orEmpty()
        mutableState.value = SpeechInputState(partialText = text)
        if (text.isNotBlank()) onFinalText(text)
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val text = partialResults
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            .orEmpty()
        mutableState.value = mutableState.value.copy(partialText = text)
    }

    override fun onEvent(eventType: Int, params: Bundle?) = Unit

    private fun errorMessage(code: Int): String = when (code) {
        SpeechRecognizer.ERROR_AUDIO -> "No se pudo capturar el audio"
        SpeechRecognizer.ERROR_CLIENT -> "El reconocimiento fue cancelado"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Falta permiso de micrófono"
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Error de red al reconocer la voz"
        SpeechRecognizer.ERROR_NO_MATCH -> "No entendí la frase"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "El reconocedor está ocupado"
        SpeechRecognizer.ERROR_SERVER, SpeechRecognizer.ERROR_SERVER_DISCONNECTED -> "El servicio de voz no está disponible"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó voz"
        else -> "Error de reconocimiento ($code)"
    }
}
