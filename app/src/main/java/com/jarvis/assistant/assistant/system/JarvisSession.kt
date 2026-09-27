package com.jarvis.assistant.assistant.system

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.jarvis.assistant.JarvisApplication
import com.jarvis.assistant.R
import com.jarvis.assistant.runtime.JarvisRuntime
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicReference

private val SessionCyan = Color(0xFF00E5FF)
private val SessionElectricBlue = Color(0xFF2979FF)
private val SessionPurple = Color(0xFF9D4EDD)
private val SessionEmerald = Color(0xFF00E676)
private val SessionTextPrimary = Color(0xFFF0F9FF)
private val SessionTextSecondary = Color(0xFF8AA8C7)
private val SessionTextMuted = Color(0xFF4A6888)

class JarvisSession(context: Context) :
    VoiceInteractionSession(context),
    LifecycleOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val runtime: JarvisRuntime?
        get() = (context.applicationContext as? JarvisApplication)?.runtime

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onCreateContentView(): View {
        // Estilizar ventana como bottom sheet flotante con fondo atenuado
        window?.window?.apply {
            setGravity(Gravity.BOTTOM)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.5f)
        }

        return ComposeView(context).apply {
            setViewTreeLifecycleOwner(this@JarvisSession)
            setViewTreeSavedStateRegistryOwner(this@JarvisSession)
            setContent {
                JarvisAssistantOverlay(
                    runtime = runtime,
                    onDismiss = { finish() }
                )
            }
        }
    }

    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        window?.window?.decorView?.announceForAccessibility("Jarvis activo")
    }

    override fun onHide() {
        super.onHide()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}

@Composable
internal fun JarvisAssistantOverlay(
    runtime: JarvisRuntime?,
    onDismiss: () -> Unit
) {
    val state = runtime?.state?.collectAsState()?.value
    var command by remember { mutableStateOf("") }
    var handsFree by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val localContext = LocalContext.current
    val ttsController = remember(localContext) { TextToSpeechController(localContext) }
    val speechReference = remember { AtomicReference<SpeechInputController>() }
    val speechController = remember(localContext, runtime) {
        SpeechInputController(
            context = localContext,
            onFinalText = { recognized ->
                command = recognized
                scope.launch {
                    val result = runtime?.execute(recognized)
                    val response = result?.errorMessage ?: result?.outputMessage ?: "Motor no disponible"
                    ttsController.speak(response) {
                        if (handsFree) speechReference.get()?.startListening()
                    }
                }
            },
            onListeningError = {
                if (handsFree) {
                    scope.launch {
                        delay(600)
                        if (handsFree) speechReference.get()?.startListening()
                    }
                }
            }
        )
    }
    speechReference.set(speechController)
    val speechState = speechController.state.collectAsState().value
    val ttsState = ttsController.state.collectAsState().value
    DisposableEffect(speechController, ttsController) {
        onDispose {
            handsFree = false
            speechController.destroy()
            ttsController.shutdown()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xF80A1528),
                        Color(0xF8060D1A),
                        Color(0xFF040810)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        SessionCyan.copy(alpha = 0.6f),
                        SessionPurple.copy(alpha = 0.5f),
                        SessionElectricBlue.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Barra de agarre superior
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF233B58))
                    .clickable(onClick = onDismiss)
            )

            // Cabecera del Asistente con Logo 3D y botón cerrar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Logo 3D con halo luminoso
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(SessionCyan.copy(alpha = 0.35f), Color.Transparent)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.jarvis_logo),
                            contentDescription = "Logo Jarvis",
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "J.A.R.V.I.S.",
                                color = SessionCyan,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SessionCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "MODO ASISTENTE",
                                    color = SessionCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }
                        Text(
                            text = "Control por voz y asistencia en pantalla",
                            color = SessionTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F1E33))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = SessionTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Visualizador de ondas de audio (Soundwave)
            SoundwaveVisualizer(isBusy = state?.isBusy == true || speechState.isListening || ttsState.isSpeaking)

            // Indicador de estado
            Text(
                text = when {
                    speechState.isListening -> "Escuchando..."
                    ttsState.isSpeaking -> "Respondiendo por voz..."
                    speechState.error != null -> speechState.error
                    state?.isBusy == true -> "Procesando directiva cognitiva..."
                    state?.lastMessage?.isNotBlank() == true && state.lastMessage != "Listo" ->
                        state.lastMessage
                    else -> "¿En qué puedo ayudarte hoy?"
                },
                color = if (state?.isBusy == true) SessionCyan else SessionTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Acciones Rápidas (Chips de asistencia)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistantChip("🔍 ¿Qué hay en pantalla?", state?.isBusy != true) {
                    scope.launch { runtime?.execute("inspecciona pantalla") }
                }
                AssistantChip("🏠 Ir a Inicio", state?.isBusy != true) {
                    scope.launch {
                        runtime?.execute("inicio")
                        onDismiss()
                    }
                }
                AssistantChip("🔙 Volver Atrás", state?.isBusy != true) {
                    scope.launch {
                        runtime?.execute("atrás")
                        onDismiss()
                    }
                }
                AssistantChip("📑 Apps Recientes", state?.isBusy != true) {
                    scope.launch {
                        runtime?.execute("recientes")
                        onDismiss()
                    }
                }
                AssistantChip("🎵 Abrir Spotify", state?.isBusy != true) {
                    scope.launch {
                        runtime?.execute("abre spotify")
                        onDismiss()
                    }
                }
            }

            // Consola de entrada del Asistente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF081324))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(SessionCyan.copy(alpha = 0.4f), SessionPurple.copy(alpha = 0.3f))
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(8.dp)
            ) {
                Column {
                    OutlinedTextField(
                        value = command,
                        onValueChange = { command = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Escribe o dicta una instrucción...",
                                color = SessionTextMuted,
                                fontSize = 12.sp
                            )
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (command.isNotBlank() && state?.isBusy != true) {
                                    val toExec = command
                                    command = ""
                                    scope.launch { runtime?.execute(toExec) }
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SessionTextPrimary,
                            unfocusedTextColor = SessionTextPrimary,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = SessionCyan
                        ),
                        minLines = 1,
                        maxLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Micrófono con pulso
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (handsFree) SessionCyan.copy(alpha = 0.25f) else Color(0xFF0F223D))
                                .border(1.dp, SessionCyan.copy(alpha = 0.35f), CircleShape)
                                .clickable(enabled = handsFree || state?.isBusy != true) {
                                    handsFree = !handsFree
                                    if (handsFree) {
                                        ttsController.stop()
                                        speechController.startListening()
                                    } else {
                                        speechController.cancel()
                                        ttsController.stop()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = if (handsFree) "Desactivar modo manos libres" else "Activar modo manos libres",
                                tint = SessionCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Botón de Transmitir
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (command.isNotBlank() && state?.isBusy != true) {
                                        Brush.horizontalGradient(
                                            listOf(SessionCyan, SessionElectricBlue)
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF16253B), Color(0xFF121E30))
                                        )
                                    }
                                )
                                .clickable(enabled = command.isNotBlank() && state?.isBusy != true) {
                                    val toExec = command
                                    command = ""
                                    scope.launch { runtime?.execute(toExec) }
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (state?.isBusy == true) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Text(
                                        text = "TRANSMITIR",
                                        color = if (command.isNotBlank()) Color(0xFF020D1A) else SessionTextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null,
                                        tint = if (command.isNotBlank()) Color(0xFF020D1A) else SessionTextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Radar de percepción si se inspeccionó pantalla
            AnimatedVisibility(
                visible = state?.lastObservation?.isNotBlank() == true,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF040A14))
                        .border(1.dp, SessionCyan.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(8.dp)
                ) {
                    Text(
                        text = state?.lastObservation.orEmpty(),
                        color = SessionTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// VISUALIZADOR DE ONDAS DE SONIDO / AUDIO (SOUNDWAVE)
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun SoundwaveVisualizer(isBusy: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "soundwave")
    val heights = listOf(
        infiniteTransition.animateFloat(
            initialValue = 8f, targetValue = if (isBusy) 32f else 18f,
            animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "bar1"
        ),
        infiniteTransition.animateFloat(
            initialValue = 14f, targetValue = if (isBusy) 40f else 26f,
            animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "bar2"
        ),
        infiniteTransition.animateFloat(
            initialValue = 20f, targetValue = if (isBusy) 48f else 34f,
            animationSpec = infiniteRepeatable(tween(290, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "bar3"
        ),
        infiniteTransition.animateFloat(
            initialValue = 14f, targetValue = if (isBusy) 40f else 24f,
            animationSpec = infiniteRepeatable(tween(480, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "bar4"
        ),
        infiniteTransition.animateFloat(
            initialValue = 8f, targetValue = if (isBusy) 30f else 16f,
            animationSpec = infiniteRepeatable(tween(380, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "bar5"
        )
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        heights.forEachIndexed { index, animHeight ->
            val color = when (index) {
                0, 4 -> SessionCyan
                1, 3 -> SessionElectricBlue
                else -> SessionPurple
            }
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(animHeight.value.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun AssistantChip(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0C192E))
            .border(1.dp, SessionCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (enabled) SessionCyan else SessionTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
