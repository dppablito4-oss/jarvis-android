package com.jarvis.assistant.assistant.system

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

class JarvisSession(context: Context) : VoiceInteractionSession(context) {
    override fun onCreateContentView(): View {
        val padding = (24 * context.resources.displayMetrics.density).toInt()
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding, padding, padding)
            setBackgroundColor(Color.rgb(7, 17, 31))
            addView(TextView(context).apply {
                text = "JARVIS"
                textSize = 28f
                setTextColor(Color.rgb(93, 228, 199))
            })
            addView(TextView(context).apply {
                text = "Sesión del asistente activa"
                textSize = 16f
                setTextColor(Color.WHITE)
            }, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
        window?.window?.decorView?.announceForAccessibility("Jarvis activo")
    }
}
