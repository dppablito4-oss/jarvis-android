package com.jarvis.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.assistant.core.model.DeviceCapability
import com.jarvis.assistant.runtime.JarvisRuntime
import com.jarvis.assistant.runtime.RuntimeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class CapabilitiesState(
    val hasAssistantRole: Boolean = false,
    val hasAccessibility: Boolean = false,
    val hasMicrophone: Boolean = false,
    val hasNetwork: Boolean = false,
    val hasAssistantGateway: Boolean = false
)

class MainViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(RuntimeState())
    val state: StateFlow<RuntimeState> = mutableState.asStateFlow()

    private val _capabilities = MutableStateFlow(CapabilitiesState())
    val capabilities: StateFlow<CapabilitiesState> = _capabilities.asStateFlow()

    private var runtime: JarvisRuntime? = null

    fun attach(runtime: JarvisRuntime) {
        if (this.runtime === runtime) return
        this.runtime = runtime
        viewModelScope.launch {
            runtime.state.collectLatest { mutableState.value = it }
        }
        refreshCapabilities()
    }

    fun refreshCapabilities() {
        val rt = runtime ?: return
        viewModelScope.launch {
            val provider = rt.capabilityProvider
            _capabilities.value = CapabilitiesState(
                hasAssistantRole = provider.hasCapability(DeviceCapability.ASSISTANT_ROLE),
                hasAccessibility = provider.hasCapability(DeviceCapability.ACCESSIBILITY),
                hasMicrophone = provider.hasCapability(DeviceCapability.MICROPHONE),
                hasNetwork = provider.hasCapability(DeviceCapability.NETWORK),
                hasAssistantGateway = rt.hasAssistantGateway
            )
        }
    }

    fun execute(command: String) {
        viewModelScope.launch {
            runtime?.execute(command)
            refreshCapabilities()
        }
    }
}
