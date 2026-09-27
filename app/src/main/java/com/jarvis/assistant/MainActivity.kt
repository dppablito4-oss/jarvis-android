package com.jarvis.assistant

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jarvis.assistant.assistant.system.SpeechInputController
import com.jarvis.assistant.assistant.system.TextToSpeechController

// ══════════════════════════════════════════════════════════════════════
// PALETA DE COLORES JARVIS CYBERPUNK / HIGH-TECH HUD
// ══════════════════════════════════════════════════════════════════════
private val JarvisBg = Color(0xFF040913)
private val JarvisSurface = Color(0xFF0A1526)
private val JarvisSurfaceGlow = Color(0xFF0E1E36)
private val JarvisCyan = Color(0xFF00E5FF)
private val JarvisTeal = Color(0xFF5DE4C7)
private val JarvisElectricBlue = Color(0xFF2979FF)
private val JarvisNeonPurple = Color(0xFF9D4EDD)
private val JarvisEmerald = Color(0xFF00E676)
private val JarvisAmber = Color(0xFFFFB300)
private val JarvisCrimson = Color(0xFFFF5252)
private val JarvisTextPrimary = Color(0xFFF0F9FF)
private val JarvisTextSecondary = Color(0xFF8AA8C7)
private val JarvisTextMuted = Color(0xFF4A6888)

private val JarvisThemeColors = darkColorScheme(
    primary = JarvisCyan,
    secondary = JarvisElectricBlue,
    tertiary = JarvisTeal,
    background = JarvisBg,
    surface = JarvisSurface,
    onPrimary = Color(0xFF001F29),
    onBackground = JarvisTextPrimary,
    onSurface = JarvisTextPrimary
)

class MainActivity : ComponentActivity() {
    private val model: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model.attach((application as JarvisApplication).runtime)
        setContent {
            MaterialTheme(colorScheme = JarvisThemeColors) {
                JarvisScreen(
                    model = model,
                    requestAssistantRole = ::requestAssistantRole,
                    openAccessibilitySettings = ::openAccessibilitySettings
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        model.refreshCapabilities()
    }

    private fun requestAssistantRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            startActivity(roleManager.createRequestRoleIntent(RoleManager.ROLE_ASSISTANT))
        } else {
            startActivity(Intent(Settings.ACTION_VOICE_INPUT_SETTINGS))
        }
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }
}

@Composable
private fun JarvisScreen(
    model: MainViewModel,
    requestAssistantRole: () -> Unit,
    openAccessibilitySettings: () -> Unit
) {
    val state by model.state.collectAsStateWithLifecycle()
    val capabilities by model.capabilities.collectAsStateWithLifecycle()
    var command by remember { mutableStateOf("") }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val ttsController = remember(context) { TextToSpeechController(context) }
    val speechController = remember(context, model) {
        SpeechInputController(context) { recognized ->
            command = recognized
            model.execute(recognized) { result ->
                ttsController.speak(result.errorMessage ?: result.outputMessage)
            }
        }
    }
    DisposableEffect(speechController, ttsController) {
        onDispose {
            speechController.destroy()
            ttsController.shutdown()
        }
    }

    val microphonePermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        model.refreshCapabilities()
        val text = if (granted) "Micrófono habilitado" else "Permiso de micrófono denegado"
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        if (granted) speechController.startListening()
    }
    val requestVoiceInput = {
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            speechController.startListening()
        } else {
            microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = JarvisBg
    ) {
        // Fondo ambiental con degradado profundo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF040A17),
                            Color(0xFF071224),
                            Color(0xFF03070E)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // ── CABECERA HUD ──────────────────────────────────────────
                JarvisHeader(isBusy = state.isBusy)

                // ── NÚCLEO REACTIVO / ARC REACTOR ─────────────────────────
                JarvisArcReactor(
                    isBusy = state.isBusy,
                    lastSuccess = state.lastSuccess,
                    lastRequest = state.lastRequest,
                    onTap = {
                        model.execute("inspecciona pantalla")
                    }
                )

                // ── MATRIZ DE TELEMETRÍA Y CAPACIDADES ───────────────────
                TelemetryCapabilitiesSection(
                    capabilities = capabilities,
                    requestAssistantRole = requestAssistantRole,
                    openAccessibilitySettings = openAccessibilitySettings,
                    requestMicPermission = {
                        requestVoiceInput()
                    },
                    configureGateway = { showApiKeyDialog = true },
                    onRefresh = { model.refreshCapabilities() }
                )

                // ── ACCIONES RÁPIDAS (CHIPS DINÁMICOS) ───────────────────
                QuickActionChips(
                    isBusy = state.isBusy,
                    onExecuteQuick = { quickCommand ->
                        command = quickCommand
                        model.execute(quickCommand)
                    }
                )

                // ── CONSOLA DE ENTRADA TERMINAL ──────────────────────────
                JarvisCommandConsole(
                    command = command,
                    isBusy = state.isBusy,
                    onCommandChange = { command = it },
                    onExecute = {
                        if (command.isNotBlank() && !state.isBusy) {
                            model.execute(command)
                        }
                    },
                    onRequestMic = {
                        requestVoiceInput()
                    }
                )

                // ── MONITOR DE RESPUESTA Y TELEMETRÍA ────────────────────
                JarvisResponseHud(
                    lastRequest = state.lastRequest,
                    lastMessage = state.lastMessage,
                    lastSuccess = state.lastSuccess,
                    isBusy = state.isBusy
                )

                // ── RADAR DE PERCEPCIÓN DE PANTALLA (SI EXISTE) ──────────
                if (state.lastObservation.isNotBlank()) {
                    PerceptionRadarCard(
                        observation = state.lastObservation
                    )
                }

                // ── FOOTER DE SEGURIDAD LOCAL ────────────────────────────
                JarvisSecurityBanner(hasGateway = capabilities.hasAssistantGateway)

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    if (showApiKeyDialog) {
        OpenAiKeyDialog(
            onDismiss = { showApiKeyDialog = false },
            onSave = { key ->
                val app = context.applicationContext as JarvisApplication
                model.attach(app.configureOpenAiKey(key))
                showApiKeyDialog = false
                Toast.makeText(context, "Configuración de OpenAI actualizada", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun OpenAiKeyDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var key by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Motor OpenAI") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("La clave se cifra con Android Keystore y no se incluye en el APK.")
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it.trim() },
                    label = { Text("API key") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = {
            TextButton(enabled = key.isNotBlank(), onClick = { onSave(key) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// ══════════════════════════════════════════════════════════════════════
// 1. CABECERA HUD CON ESTADO DINÁMICO
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun JarvisHeader(isBusy: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.jarvis_logo),
                    contentDescription = "Logo Jarvis",
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "J.A.R.V.I.S.",
                    color = JarvisCyan,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(JarvisCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "OS-AGENT",
                        color = JarvisCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
            Text(
                text = "Autonomous System Core · Control Local Seguro",
                color = JarvisTextSecondary,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp
            )
        }

        // Indicador de pulso de estado
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(if (isBusy) 400 else 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_alpha"
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF08192E))
                .border(1.dp, JarvisCyan.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isBusy) JarvisAmber.copy(alpha = alpha)
                        else JarvisEmerald.copy(alpha = alpha)
                    )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isBusy) "CALCULANDO" else "EN LÍNEA",
                color = if (isBusy) JarvisAmber else JarvisEmerald,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// 2. NÚCLEO REACTIVO / ARC REACTOR ANIMADO
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun JarvisArcReactor(
    isBusy: Boolean,
    lastSuccess: Boolean?,
    lastRequest: String,
    onTap: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "reactor")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isBusy) 2200 else 12000, easing = LinearEasing)
        ),
        label = "rotation"
    )
    val counterRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isBusy) 3500 else 18000, easing = LinearEasing)
        ),
        label = "counter_rotation"
    )
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isBusy) 500 else 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    val coreGlowColor by animateColorAsState(
        targetValue = when {
            isBusy -> JarvisCyan
            lastSuccess == true -> JarvisEmerald
            lastSuccess == false -> JarvisCrimson
            else -> JarvisTeal
        },
        animationSpec = tween(600),
        label = "glow_color"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        // Lienzo del Arc Reactor con órbitas y anillos
        Canvas(
            modifier = Modifier
                .size(170.dp)
                .scale(breathScale)
                .clickable(onClick = onTap)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f

            // Halo exterior radial
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        coreGlowColor.copy(alpha = if (isBusy) 0.35f else 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius
                ),
                radius = maxRadius,
                center = center
            )

            // Anillo exterior segmentado (órbita 1)
            val outerRadius = maxRadius * 0.88f
            for (i in 0 until 6) {
                val startAngle = rotation + (i * 60f) + 8f
                drawArc(
                    color = coreGlowColor.copy(alpha = 0.7f),
                    startAngle = startAngle,
                    sweepAngle = 44f,
                    useCenter = false,
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                    size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Anillo intermedio con ticks en contra-rotación (órbita 2)
            val midRadius = maxRadius * 0.65f
            for (i in 0 until 12) {
                val startAngle = counterRotation + (i * 30f) + 4f
                drawArc(
                    color = JarvisElectricBlue.copy(alpha = 0.55f),
                    startAngle = startAngle,
                    sweepAngle = 18f,
                    useCenter = false,
                    topLeft = Offset(center.x - midRadius, center.y - midRadius),
                    size = androidx.compose.ui.geometry.Size(midRadius * 2, midRadius * 2),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Anillo interior de energía (órbita 3)
            val innerRadius = maxRadius * 0.42f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        coreGlowColor,
                        coreGlowColor.copy(alpha = 0.5f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = innerRadius
                ),
                radius = innerRadius,
                center = center
            )

            // Núcleo central luminoso
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = maxRadius * 0.22f,
                center = center
            )
        }

        // Logo 3D en el corazón del reactor con respiración
        Image(
            painter = painterResource(R.drawable.jarvis_logo),
            contentDescription = "Núcleo Jarvis",
            modifier = Modifier
                .size(52.dp)
                .scale(breathScale)
                .clip(CircleShape)
        )

        // Estado del núcleo sobreimpreso al pie
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(top = 180.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when {
                    isBusy -> "PROCESANDO PROTOCOLO..."
                    lastRequest.isNotBlank() -> "ÚLTIMA ORDEN: \"${lastRequest.take(28)}\""
                    else -> "NÚCLEO NEURAL PREPARADO"
                },
                color = if (isBusy) JarvisCyan else JarvisTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// 3. MATRIZ DE TELEMETRÍA Y CAPACIDADES DEL DISPOSITIVO
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun TelemetryCapabilitiesSection(
    capabilities: CapabilitiesState,
    requestAssistantRole: () -> Unit,
    openAccessibilitySettings: () -> Unit,
    requestMicPermission: () -> Unit,
    configureGateway: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SUBSISTEMAS & PRIVILEGIOS",
                color = JarvisCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            IconButton(onClick = onRefresh, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Actualizar estado",
                    tint = JarvisTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fila 1: Asistente + Accesibilidad
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CapabilityBadge(
                modifier = Modifier.weight(1f),
                title = "Rol Asistente",
                subtitle = if (capabilities.hasAssistantRole) "PREDETERMINADO" else "NO ASIGNADO",
                isActive = capabilities.hasAssistantRole,
                icon = Icons.Default.Shield,
                onClick = requestAssistantRole
            )
            CapabilityBadge(
                modifier = Modifier.weight(1f),
                title = "Accesibilidad",
                subtitle = if (capabilities.hasAccessibility) "ENLACE ACTIVO" else "SIN CONEXIÓN",
                isActive = capabilities.hasAccessibility,
                icon = Icons.Default.Visibility,
                onClick = openAccessibilitySettings
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fila 2: Micrófono + Motor IA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CapabilityBadge(
                modifier = Modifier.weight(1f),
                title = "Voz / Micro",
                subtitle = if (capabilities.hasMicrophone) "AUTORIZADO" else "HABILITAR",
                isActive = capabilities.hasMicrophone,
                icon = Icons.Default.Mic,
                onClick = requestMicPermission
            )
            CapabilityBadge(
                modifier = Modifier.weight(1f),
                title = "Motor IA",
                subtitle = if (capabilities.hasAssistantGateway) "GPT-6 ASTRA" else "LOCAL DIRECTO",
                isActive = capabilities.hasAssistantGateway,
                icon = Icons.Default.Psychology,
                accentColor = if (capabilities.hasAssistantGateway) JarvisNeonPurple else JarvisElectricBlue,
                onClick = configureGateway
            )
        }
    }
}

@Composable
private fun CapabilityBadge(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    isActive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color? = null,
    onClick: () -> Unit
) {
    val activeColor = accentColor ?: if (isActive) JarvisEmerald else JarvisAmber
    val cardBg = Color(0xFF091424)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(
                1.dp,
                if (isActive) activeColor.copy(alpha = 0.35f) else Color(0xFF1E314D),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(activeColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = activeColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = JarvisTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(activeColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = subtitle,
                    color = activeColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// 4. ACCIONES RÁPIDAS (CHIPS DINÁMICOS)
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun QuickActionChips(
    isBusy: Boolean,
    onExecuteQuick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "ACCIONES RÁPIDAS",
            color = JarvisCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickChip("🔍 Inspeccionar UI", "inspecciona pantalla", isBusy, onExecuteQuick)
            QuickChip("🏠 Ir a Inicio", "inicio", isBusy, onExecuteQuick)
            QuickChip("🔙 Volver Atrás", "atrás", isBusy, onExecuteQuick)
            QuickChip("📑 Apps Recientes", "recientes", isBusy, onExecuteQuick)
            QuickChip("🎵 Abrir Spotify", "abre spotify", isBusy, onExecuteQuick)
            QuickChip("🌐 Abrir Chrome", "abre chrome", isBusy, onExecuteQuick)
        }
    }
}

@Composable
private fun QuickChip(
    label: String,
    command: String,
    isBusy: Boolean,
    onExecute: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0C192E))
            .border(1.dp, JarvisCyan.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .clickable(enabled = !isBusy) { onExecute(command) }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (isBusy) JarvisTextMuted else JarvisCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
// 5. CONSOLA DE ENTRADA TERMINAL
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun JarvisCommandConsole(
    command: String,
    isBusy: Boolean,
    onCommandChange: (String) -> Unit,
    onExecute: () -> Unit,
    onRequestMic: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "CONSOLA DE CONTROL",
            color = JarvisCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF091424))
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            JarvisCyan.copy(alpha = 0.5f),
                            JarvisElectricBlue.copy(alpha = 0.3f),
                            JarvisNeonPurple.copy(alpha = 0.4f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "JARVIS://>",
                        color = JarvisTeal,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ingrese comando local o solicitud cognitiva",
                        color = JarvisTextMuted,
                        fontSize = 11.sp
                    )
                }

                OutlinedTextField(
                    value = command,
                    onValueChange = onCommandChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Ej.: abre Spotify, ve a inicio, qué hay en pantalla...",
                            color = JarvisTextMuted,
                            fontSize = 13.sp
                        )
                    },
                    trailingIcon = {
                        if (command.isNotBlank()) {
                            IconButton(onClick = { onCommandChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar",
                                    tint = JarvisTextSecondary
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onExecute() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = JarvisCyan
                    ),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón de micrófono
                    IconButton(
                        onClick = onRequestMic,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F223D))
                            .border(1.dp, JarvisCyan.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voz",
                            tint = JarvisCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Botón de ejecución futurista
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (command.isNotBlank() && !isBusy) {
                                    Brush.horizontalGradient(
                                        colors = listOf(JarvisCyan, JarvisElectricBlue)
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF16253B), Color(0xFF121E30))
                                    )
                                }
                            )
                            .clickable(enabled = command.isNotBlank() && !isBusy) { onExecute() }
                            .padding(horizontal = 22.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isBusy) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Text(
                                    text = "EJECUTANDO...",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            } else {
                                Text(
                                    text = "TRANSMITIR",
                                    color = if (command.isNotBlank()) Color(0xFF020D1A) else JarvisTextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Ejecutar",
                                    tint = if (command.isNotBlank()) Color(0xFF020D1A) else JarvisTextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// 6. MONITOR DE RESPUESTA Y TELEMETRÍA (HUD CARD)
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun JarvisResponseHud(
    lastRequest: String,
    lastMessage: String,
    lastSuccess: Boolean?,
    isBusy: Boolean
) {
    val statusColor = when {
        isBusy -> JarvisAmber
        lastSuccess == true -> JarvisEmerald
        lastSuccess == false -> JarvisCrimson
        else -> JarvisCyan
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF091424))
            .border(1.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Text(
                    text = "TERMINAL DE SALIDA",
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            if (lastSuccess != null) {
                Text(
                    text = if (lastSuccess) "ESTADO: ÉXITO (200)" else "ESTADO: FALLO / RECHAZADO",
                    color = if (lastSuccess) JarvisEmerald else JarvisCrimson,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF050B14))
                .padding(12.dp)
        ) {
            Text(
                text = lastMessage.ifBlank { "Sistema listo para recibir directivas." },
                color = JarvisTextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// 7. RADAR DE PERCEPCIÓN DE PANTALLA EXPANDIBLE
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun PerceptionRadarCard(observation: String) {
    var expanded by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val lines = remember(observation) { observation.lines().filter(String::isNotBlank) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF07111F))
            .border(1.dp, JarvisTeal.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = JarvisTeal,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "RADAR DE PERCEPCIÓN VISUAL",
                    color = JarvisTeal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(JarvisTeal.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${lines.size} NODOS",
                        color = JarvisTeal,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Row {
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
                        clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("Observation", observation))
                        Toast.makeText(context, "Telemetría visual copiada", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar telemetría",
                        tint = JarvisTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expandir/Colapsar",
                        tint = JarvisTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF03070E))
                        .border(1.dp, Color(0xFF132338), RoundedCornerShape(8.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        lines.forEach { line ->
                            val isClick = line.contains("(click)")
                            val isInput = line.contains("(input)")

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF07111F))
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = line.replace("(click)", "").replace("(input)", "").trim(),
                                    color = JarvisTextPrimary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (isClick) {
                                        Text(
                                            text = "CLICK",
                                            color = JarvisCyan,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier
                                                .background(JarvisCyan.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                    if (isInput) {
                                        Text(
                                            text = "INPUT",
                                            color = JarvisNeonPurple,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier
                                                .background(JarvisNeonPurple.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// 8. BANNER DE SEGURIDAD LOCAL
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun JarvisSecurityBanner(hasGateway: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF06101D))
            .border(1.dp, Color(0xFF132742), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = "Seguridad",
            tint = JarvisCyan,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = if (hasGateway) "MODO HÍBRIDO (OPENAI + CAPACIDAD LOCAL)" else "MODO SEGURO LOCAL (OFFLINE)",
                color = JarvisCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Router blindado por DefaultSafetyInterceptor. Cero ejecución arbitraria de shell sin validación.",
                color = JarvisTextMuted,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}
