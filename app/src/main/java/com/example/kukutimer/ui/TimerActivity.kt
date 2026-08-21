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
import androidx.compose.foundation.shape.CircleShape
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
        appPreferences = AppPreferences(this)

        val targetPackage = intent.getStringExtra("TARGET_PACKAGE") ?: run {
            finish()
            return
        }

        setContent {
            KukuTimerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BeigeBackground)
    ) {
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
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section: Philosophy & Japanese Calligraphy
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Text(
                    text = "一炊の夢", // "Dream during the cooking of a pot of rice"
                    fontSize = 13.sp,
                    color = ShuIro,
                    letterSpacing = 6.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "KUKU TIMER",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 11.sp,
                        letterSpacing = 4.sp,
                        color = InkSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isWindowOpen) "Котел закипел. Рис готов" else "Котел с рисом закипает...",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = InkPrimary,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 1.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isWindowOpen)
                        "«Врата открыты — осознанность подтверждена»"
                    else
                        "«Человеку даётся шанс на спасение, пока варится рис»",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = KinGold,
                        fontFamily = FontFamily.Serif,
                        fontSize = 12.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }

            // Central Zen Dial with Animated Rice Grains
            RiceGrainDial(
                modifier = Modifier.size(280.dp),
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
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, BeigeBorder, RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Text(
                        text = appName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = InkPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (!isWindowOpen) {
                        val minutes = remainingSeconds / 60
                        val seconds = remainingSeconds % 60
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Light,
                                letterSpacing = 2.sp,
                                color = InkPrimary
                            )
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = KinGoldLight,
                            border = BorderStroke(1.dp, KinGold.copy(alpha = 0.6f)),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "2 МИНУТЫ НА ВХОД",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = KinGold,
                                    fontWeight = FontWeight.Bold,
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
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = BeigeSurface),
                    border = BorderStroke(1.dp, BeigeBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = if (!isWindowOpen)
                            "Ограничение активно. Сделайте вдох и вернитесь к своим делам. Когда рис сварится, вы получите уведомление."
                        else
                            "Окно доступа открыто! Войдите в приложение сейчас, чтобы пользоваться им свободно до выключения экрана.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = InkSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
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
                            .height(52.dp),
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
                                fontSize = 15.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedButton(
                    onClick = onExit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
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
