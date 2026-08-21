package com.example.kukutimer.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kukutimer.data.AppPreferences
import com.example.kukutimer.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

class TimerActivity : ComponentActivity() {
    private lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appPreferences = AppPreferences(this)

        val targetPackage = intent.getStringExtra("TARGET_PACKAGE") ?: run {
            finish()
            return
        }

        setContent {
            KukuTimerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SumiDark
                ) {
                    ZenTimerScreen(
                        targetPackage = targetPackage,
                        appPreferences = appPreferences,
                        packageManager = packageManager,
                        onExit = { goHome() },
                        onOpenApp = { openTargetApp(targetPackage) }
                    )
                }
            }
        }
    }

    private fun goHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    private fun openTargetApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launchIntent)
        }
        finish()
    }
}

@Composable
fun ZenTimerScreen(
    targetPackage: String,
    appPreferences: AppPreferences,
    packageManager: PackageManager,
    onExit: () -> Unit,
    onOpenApp: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var endTime by remember { mutableStateOf<Long?>(null) }
    var remainingSeconds by remember { mutableStateOf(0L) }
    var isWindowOpen by remember { mutableStateOf(false) }

    val appInfo = remember {
        try {
            val info = packageManager.getApplicationInfo(targetPackage, 0)
            val label = packageManager.getApplicationLabel(info).toString()
            val icon = packageManager.getApplicationIcon(info)
            Triple(label, icon, true)
        } catch (e: Exception) {
            Triple(targetPackage, null, false)
        }
    }

    val appName = appInfo.first
    val appIcon = appInfo.second

    LaunchedEffect(targetPackage) {
        appPreferences.getTimerEndTime(targetPackage).collect { savedEndTime ->
            endTime = savedEndTime
        }
    }

    LaunchedEffect(endTime) {
        while (true) {
            val now = System.currentTimeMillis()
            if (endTime != null) {
                val diff = endTime!! - now
                if (diff > 0) {
                    remainingSeconds = diff / 1000
                    isWindowOpen = false
                } else if (diff > -(2 * 60 * 1000)) {
                    remainingSeconds = 0
                    isWindowOpen = true
                } else {
                    remainingSeconds = 0
                    isWindowOpen = false
                }
            } else {
                remainingSeconds = 0
                isWindowOpen = false
            }
            delay(500)
        }
    }

    // Meditative Breathing Animation
    val infiniteTransition = rememberInfiniteTransition(label = "ZenBreathing")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SumiDark)
    ) {
        // Decorative Traditional Sumi-e Top & Bottom Border Pattern
        WagaraHeaderDecoration(modifier = Modifier.align(Alignment.TopCenter))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section: Japanese Motifs
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 28.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "一炊の夢", // "Dream while cooking a single pot of rice" (Zen idiom)
                        fontSize = 13.sp,
                        color = ShuIro,
                        letterSpacing = 6.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "KUKU TIMER",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 11.sp,
                        letterSpacing = 5.sp,
                        color = InkTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isWindowOpen) "Котел закипел. Рис готов" else "Котел с рисом закипает",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Shirayuri,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 1.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isWindowOpen)
                        "«Врата открыты — осознанность достигнута»"
                    else
                        "«Шанс на спасение, пока варится котел с рисом»",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = KinGold,
                        fontFamily = FontFamily.Serif,
                        fontSize = 12.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }

            // Central Zen Ensō Dial & Target App Info
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(280.dp)
                    .padding(8.dp)
            ) {
                // Zen Ensō Circle Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 3.dp.toPx()
                    val radius = (size.minDimension - strokeWidth * 4) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Outer faint stone ring
                    drawCircle(
                        color = SumiBorder.copy(alpha = 0.4f),
                        radius = radius + 12.dp.toPx(),
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // Breathing Ensō Ring
                    val ringColor = if (isWindowOpen) KinGold else ShuIro
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(
                                ringColor.copy(alpha = 0.1f),
                                ringColor.copy(alpha = 0.85f),
                                ringColor.copy(alpha = 0.2f),
                                ringColor
                            ),
                            center = center
                        ),
                        radius = radius * pulseScale,
                        center = center,
                        style = Stroke(
                            width = if (isWindowOpen) 4.dp.toPx() else 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                }

                // Center Content: App Icon & Digital Timer
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (appIcon != null) {
                        val bitmap = remember(appIcon) { appIcon.toComposeBitmap() }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = appName,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, SumiBorder, RoundedCornerShape(14.dp))
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    Text(
                        text = appName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Shirayuri,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!isWindowOpen) {
                        val minutes = remainingSeconds / 60
                        val seconds = remainingSeconds % 60
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 52.sp,
                                fontWeight = FontWeight.Light,
                                letterSpacing = 2.sp,
                                color = Shirayuri
                            )
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = KinGold.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, KinGold.copy(alpha = 0.6f)),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "2 МИНУТЫ НА ВХОД",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = KinGold,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Actions & Guidance Card
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SumiCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SumiBorder.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                ) {
                    Text(
                        text = if (!isWindowOpen)
                            "Ограничение активно. Сделайте вдох и вернитесь к своим делам. Когда рис сварится, вы получите уведомление."
                        else
                            "Окно доступа открыто! Войдите в приложение сейчас, чтобы пользоваться им свободно до выключения экрана.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = InkTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                if (isWindowOpen) {
                    Button(
                        onClick = {
                            scope.launch {
                                appPreferences.setSessionActive(targetPackage, true)
                                onOpenApp()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ShuIro,
                            contentColor = Shirayuri
                        )
                    ) {
                        Text(
                            text = "🌸 Войти в $appName",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedButton(
                    onClick = onExit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SumiBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = InkTextSecondary)
                ) {
                    Text(
                        text = "Вернуться на главный экран",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun WagaraHeaderDecoration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        ShuIro.copy(alpha = 0.8f),
                        KinGold.copy(alpha = 0.8f),
                        Color.Transparent
                    )
                )
            )
    )
}

private fun Drawable.toComposeBitmap(): ImageBitmap? {
    return try {
        if (this is BitmapDrawable && this.bitmap != null) {
            this.bitmap.asImageBitmap()
        } else {
            val width = if (intrinsicWidth > 0) intrinsicWidth else 96
            val height = if (intrinsicHeight > 0) intrinsicHeight else 96
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
            bitmap.asImageBitmap()
        }
    } catch (e: Exception) {
        null
    }
}
