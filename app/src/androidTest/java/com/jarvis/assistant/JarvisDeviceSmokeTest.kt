package com.jarvis.assistant

import android.Manifest
import android.content.pm.PackageManager
import android.speech.SpeechRecognizer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.jarvis.assistant.assistant.system.SpeechInputController
import com.jarvis.assistant.assistant.system.TextToSpeechController
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JarvisDeviceSmokeTest {
    @get:Rule
    val microphonePermission: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO)

    private val app: JarvisApplication
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun openAiRespondsAndCompletesToolLoop() = runBlocking {
        app.runtime.resetConversation()
        assertTrue("La API key no fue aprovisionada", app.runtime.hasAssistantGateway)
        val result = app.runtime.execute(
            "Usa obligatoriamente open_app con packageName com.android.settings y después responde incluyendo TOOL_LOOP_OK"
        )
        assertTrue(result.errorMessage ?: result.outputMessage, result.isSuccess)
        assertTrue(result.outputMessage, result.outputMessage.contains("TOOL_LOOP_OK", ignoreCase = true))
    }

    @Test
    fun openAiRemembersThePreviousVoiceTurn() = runBlocking {
        app.runtime.resetConversation()
        val first = app.runtime.execute("Recuerda que el contacto pendiente se llama Elena y responde entendido")
        assertTrue(first.errorMessage ?: first.outputMessage, first.isSuccess)
        val second = app.runtime.execute(
            "Responde exactamente MEMORY_OK si recuerdas el nombre del contacto pendiente"
        )
        assertTrue(second.errorMessage ?: second.outputMessage, second.isSuccess)
        assertTrue(second.outputMessage, second.outputMessage.contains("MEMORY_OK"))
    }

    @Test
    fun systemSpeechRecognizerStartsListening() = runBlocking {
        assertTrue(SpeechRecognizer.isRecognitionAvailable(app))
        val controller = SpeechInputController(app) { }
        try {
            controller.startListening()
            val state = withTimeout(8_000) {
                controller.state.first { it.isListening || it.error != null }
            }
            assertTrue(state.error ?: "El reconocedor no inició", state.isListening)
        } finally {
            controller.destroy()
        }
    }

    @Test
    fun textToSpeechSpeaksAndCompletes() = runBlocking {
        val controller = TextToSpeechController(app)
        try {
            val ready = withTimeout(8_000) {
                controller.state.first { it.isReady || it.error != null }
            }
            assertTrue(ready.error ?: "TTS no inició", ready.isReady)
            val completed = CompletableDeferred<Unit>()
            controller.speak("Prueba de voz Jarvis") { completed.complete(Unit) }
            withTimeout(10_000) { completed.await() }
        } finally {
            controller.shutdown()
        }
    }

    @Test
    fun assistantOverlayIsNotExported() {
        val component = android.content.ComponentName(
            app,
            "com.jarvis.assistant.assistant.system.AssistantActivity"
        )
        val info = app.packageManager.getActivityInfo(component, PackageManager.ComponentInfoFlags.of(0))
        assertFalse(info.exported)
    }
}
