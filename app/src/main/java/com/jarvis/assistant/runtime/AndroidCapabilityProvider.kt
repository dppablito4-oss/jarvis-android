package com.jarvis.assistant.runtime

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.provider.Settings
import com.jarvis.assistant.automation.accessibility.JarvisAccessibilityService
import com.jarvis.assistant.core.model.CapabilityProvider
import com.jarvis.assistant.core.model.DeviceCapability

class AndroidCapabilityProvider(
    private val context: Context
) : CapabilityProvider {
    override suspend fun hasCapability(capability: DeviceCapability): Boolean = when (capability) {
        DeviceCapability.NORMAL_ANDROID -> true
        DeviceCapability.ACCESSIBILITY -> JarvisAccessibilityService.isConnected
        DeviceCapability.NOTIFICATION_ACCESS -> false
        DeviceCapability.ASSISTANT_ROLE -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getSystemService(RoleManager::class.java)
                .isRoleHeld(RoleManager.ROLE_ASSISTANT)
        } else {
            false
        }
        DeviceCapability.SHIZUKU -> false
        DeviceCapability.ROOT -> false
        DeviceCapability.NETWORK -> context.getSystemService(ConnectivityManager::class.java)
            .activeNetwork != null
        DeviceCapability.MICROPHONE -> context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        DeviceCapability.OVERLAY -> Settings.canDrawOverlays(context)
    }

    override suspend fun getAvailableCapabilities(): Set<DeviceCapability> =
        DeviceCapability.entries.filterTo(linkedSetOf()) { hasCapability(it) }
}
