package com.jarvis.assistant.agent.tools

import com.jarvis.assistant.agent.safety.DefaultSafetyInterceptor
import com.jarvis.assistant.agent.safety.RiskLevel
import com.jarvis.assistant.agent.safety.ToolSafetyMetadata
import com.jarvis.assistant.core.model.CapabilityProvider
import com.jarvis.assistant.core.model.DeviceCapability
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SecureToolRouterTest {
    @Test
    fun `denies execution when capability is unavailable`() = kotlinx.coroutines.test.runTest {
        var executions = 0
        val router = router(capabilities = emptySet(), confirmation = true)
        router.registerTool(fakeTool { executions++ })

        val result = router.route("test_action", emptyMap())

        assertFalse(result.isSuccess)
        assertEquals(0, executions)
    }

    @Test
    fun `executes after explicit confirmation`() = kotlinx.coroutines.test.runTest {
        var executions = 0
        val router = router(setOf(DeviceCapability.NORMAL_ANDROID), confirmation = true)
        router.registerTool(fakeTool(requiresConfirmation = true) { executions++ })

        val result = router.route("test_action", emptyMap())

        assertTrue(result.isSuccess)
        assertEquals(1, executions)
    }

    private fun router(capabilities: Set<DeviceCapability>, confirmation: Boolean): SecureToolRouter {
        val provider = object : CapabilityProvider {
            override suspend fun hasCapability(capability: DeviceCapability) = capability in capabilities
            override suspend fun getAvailableCapabilities() = capabilities
        }
        return SecureToolRouter(
            DefaultSafetyInterceptor(provider),
            ConfirmationHandler { _, _, _ -> confirmation }
        )
    }

    private fun fakeTool(
        requiresConfirmation: Boolean = false,
        onExecute: () -> Unit
    ) = object : Tool {
        override val definition = ToolDefinition(
            name = "test_action",
            description = "test",
            parametersSchemaJson = "{}",
            safetyMetadata = ToolSafetyMetadata(
                RiskLevel.MEDIUM,
                requiresConfirmation,
                DeviceCapability.NORMAL_ANDROID
            )
        )

        override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
            onExecute()
            return ToolResult(definition.name, true, "ok")
        }
    }
}
