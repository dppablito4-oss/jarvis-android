package com.jarvis.assistant.runtime

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.jarvis.assistant.agent.tools.ToolDefinition
import com.jarvis.assistant.assistant.voice.AssistantGateway
import com.jarvis.assistant.assistant.voice.GatewayEvent
import com.jarvis.assistant.assistant.voice.GatewayMessage
import com.jarvis.assistant.assistant.voice.GatewayToolOutput
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
class JarvisRuntimeTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `returns all tool outputs to the model and produces final answer`() = runBlocking {
        val gateway = RecordingGateway()
        val result = JarvisRuntime(context, gateway).execute("realiza una tarea compleja")

        assertTrue(result.isSuccess)
        assertEquals("tarea finalizada", result.outputMessage)
        assertEquals(2, gateway.receivedOutputs.size)
        assertEquals(setOf("call-1", "call-2"), gateway.receivedOutputs.map { it.callId }.toSet())
    }

    @Test
    fun `does not expose screen tool without explicit consent`() = runBlocking {
        val gateway = RecordingGateway(finalOnFirstRound = true)
        JarvisRuntime(context, gateway).execute("cuéntame una historia")
        assertFalse(gateway.firstTools.any { it.name == "inspect_screen" })

        val consentGateway = RecordingGateway(finalOnFirstRound = true)
        JarvisRuntime(context, consentGateway).execute("analiza lo que ves en pantalla y explícamelo")
        assertTrue(consentGateway.firstTools.any { it.name == "inspect_screen" })
    }

    @Test
    fun `serializes concurrent commands`() = runBlocking {
        val gateway = RecordingGateway(finalOnFirstRound = true, delayMillis = 100)
        val runtime = JarvisRuntime(context, gateway)
        val first = async { runtime.execute("primera orden remota") }
        val second = async { runtime.execute("segunda orden remota") }
        first.await()
        second.await()
        assertEquals(1, gateway.maxConcurrent.get())
    }

    @Test
    fun `continues the same conversation on following voice turns`() = runBlocking {
        val gateway = RecordingGateway(finalOnFirstRound = true)
        val runtime = JarvisRuntime(context, gateway)
        runtime.execute("envía un mensaje a Elena")
        runtime.execute("dile que llegaré en diez minutos")
        assertEquals(listOf(null, "response-1"), gateway.previousResponseIds)
        assertTrue(gateway.firstTools.any { it.name == "inspect_screen" })
        assertTrue(gateway.firstTools.any { it.name == "click_node" })
        assertTrue(gateway.firstTools.any { it.name == "type_text" })
    }
}

private class RecordingGateway(
    private val finalOnFirstRound: Boolean = false,
    private val delayMillis: Long = 0
) : AssistantGateway {
    override val providerId = "fake"
    var firstTools: List<ToolDefinition> = emptyList()
    var receivedOutputs: List<GatewayToolOutput> = emptyList()
    val maxConcurrent = AtomicInteger(0)
    val previousResponseIds = mutableListOf<String?>()
    private val active = AtomicInteger(0)

    override suspend fun streamCompletion(
        userPrompt: String,
        conversationHistory: List<GatewayMessage>,
        availableTools: List<ToolDefinition>,
        previousResponseId: String?
    ): Flow<GatewayEvent> = flow {
        previousResponseIds += previousResponseId
        firstTools = availableTools
        val now = active.incrementAndGet()
        maxConcurrent.updateAndGet { previous -> maxOf(previous, now) }
        try {
            if (delayMillis > 0) delay(delayMillis)
            if (finalOnFirstRound) {
                emit(GatewayEvent.ContentDelta("respuesta"))
            } else {
                emit(GatewayEvent.ToolCallRequested("call-1", "go_home", "{}"))
                emit(GatewayEvent.ToolCallRequested("call-2", "recent_apps", "{}"))
            }
            emit(GatewayEvent.Completed("response-1", if (finalOnFirstRound) "completed" else "tool_call"))
        } finally {
            active.decrementAndGet()
        }
    }

    override suspend fun continueCompletion(
        previousResponseId: String,
        toolOutputs: List<GatewayToolOutput>,
        availableTools: List<ToolDefinition>
    ): Flow<GatewayEvent> {
        receivedOutputs = toolOutputs
        return flowOf(
            GatewayEvent.ContentDelta("tarea finalizada"),
            GatewayEvent.Completed("response-2", "completed")
        )
    }
}
