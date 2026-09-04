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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.kukutimer.data.AppPreferences
import com.example.kukutimer.theme.*
import com.example.kukutimer.ui.components.RiceBowlWatermarkBackground
import com.example.kukutimer.ui.components.RiceGrainDial
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerActivity : ComponentActivity() {
    private lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()
        appPreferences = AppPreferences(this)

        val targetPackage = intent.getStringExtra("TARGET_PACKAGE") ?: run {
            finish()
            return
        }

        setContent {
            KukuTimerTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    color = BeigeBackground
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

    override fun onResume() {
        super.onResume()
        isTop = true
        hideSystemBars()
    }

    override fun onPause() {
        super.onPause()
        isTop = false
        lastClosedTime = System.currentTimeMillis()
    }

    companion object {
        @Volatile
        var isTop: Boolean = false
        @Volatile
        var lastClosedTime: Long = 0L
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
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

    val windowMinutes by appPreferences.windowTimeMinutes.collectAsState(initial = 2)

    LaunchedEffect(targetPackage) {
        appPreferences.getTimerEndTime(targetPackage).collect { savedEndTime ->
            endTime = savedEndTime
        }
    }

    LaunchedEffect(endTime, windowMinutes) {
        val windowDurationMs = windowMinutes * 60 * 1000L
        while (true) {
            val now = System.currentTimeMillis()
            if (endTime != null) {
                val diff = endTime!! - now
                if (diff > 0) {
                    remainingSeconds = diff / 1000
                    isWindowOpen = false
                } else if (diff > -windowDurationMs) {
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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(BeigeBackground)
    ) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth
        val isCompact = screenHeight < 680.dp

        // Subtle Semi-transparent Japanese Rice Bowl Background Watermark
        RiceBowlWatermarkBackground(alpha = 0.08f)

        // Top Vermilion Ribbon
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, ShuIro, KinGold, Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = if (screenWidth < 360.dp) 16.dp else 24.dp,
                    vertical = if (isCompact) 18.dp else 28.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section: Philosophy & Japanese Calligraphy
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = if (isCompact) 4.dp else 12.dp)
            ) {
                Text(
                    text = "一炊の夢", // "Dream during the cooking of a pot of rice"
                    fontSize = if (isCompact) 12.sp else 14.sp,
                    color = ShuIro,
                    letterSpacing = 5.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "KUKU TIMER",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 10.sp,
                        letterSpacing = 4.sp,
                        color = InkSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(if (isCompact) 6.dp else 10.dp))

                Text(
                    text = if (isWindowOpen) "Котел закипел. Рис готов!" else "Котел с рисом закипает...",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = InkPrimary,
                        fontWeight = FontWeight.Light,
                        fontSize = if (isCompact) 18.sp else 22.sp,
                        letterSpacing = 0.5.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = if (isWindowOpen)
                        "«Врата открыты — осознанность подтверждена»"
                    else
                        "«Человеку даётся шанс на спасение, пока варится рис»",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = KinGold,
                        fontFamily = FontFamily.Serif,
                        fontSize = if (isCompact) 11.sp else 12.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }

            // Central Zen Dial with Animated Rice Grains (Responsive size)
            val dialSize = (screenHeight * 0.36f).coerceIn(200.dp, 280.dp)

            RiceGrainDial(
                modifier = Modifier.size(dialSize),
                isReady = isWindowOpen
            ) {
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
                                    .size(if (isCompact) 40.dp else 48.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .border(1.dp, BeigeBorder, RoundedCornerShape(11.dp))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    Text(
                        text = appName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = InkPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = if (isCompact) 14.sp else 16.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (!isWindowOpen) {
                        val minutes = remainingSeconds / 60
                        val seconds = remainingSeconds % 60
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = if (isCompact) 38.sp else 46.sp,
                                fontWeight = FontWeight.Light,
                                letterSpacing = 2.sp,
                                color = InkPrimary
                            )
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = KinGoldLight,
                            border = BorderStroke(1.dp, KinGold.copy(alpha = 0.6f)),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$windowMinutes МИН НА ВХОД",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = KinGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BeigeSurface),
                    border = BorderStroke(1.dp, BeigeBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (isCompact) 10.dp else 14.dp)
                ) {
                    Text(
                        text = if (!isWindowOpen)
                            "Ограничение активно. Сделайте вдох и вернитесь к делам. Когда рис сварится, вы получите уведомление."
                        else
                            "Окно доступа открыто! Войдите в приложение сейчас, чтобы пользоваться им свободно до выключения экрана.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = InkSecondary,
                            fontSize = if (isCompact) 11.5.sp else 12.sp,
                            lineHeight = if (isCompact) 16.sp else 18.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = if (isCompact) 10.dp else 12.dp
                        )
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
                            .height(if (isCompact) 46.dp else 52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ShuIro,
                            contentColor = BeigeSurface
                        )
                    ) {
                        Text(
                            text = "🌸 Войти в $appName",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (!isWindowOpen) {
                    Button(
                        onClick = {
                            scope.launch {
                                appPreferences.incrementAvoidedImpulses()
                                appPreferences.setTimerEndTime(targetPackage, 0L)
                                onExit()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isCompact) 46.dp else 50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MatsuGreen,
                            contentColor = BeigeSurface
                        )
                    ) {
                        Text(
                            text = "🕊️ Я выбираю осознанность (передумал)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedButton(
                    onClick = onExit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isCompact) 46.dp else 50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BeigeBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = InkSecondary)
                ) {
                    Text(
                        text = "Вернуться на главный экран",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
            }
        }
    }
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
