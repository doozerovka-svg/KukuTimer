package com.example.kukutimer.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kukutimer.theme.*
import com.example.kukutimer.ui.components.BonsaiWatermarkBackground

@Composable
fun OnboardingScreen(
    usageAccessGranted: Boolean,
    overlayGranted: Boolean,
    notificationGranted: Boolean,
    onRequestUsageAccess: () -> Unit,
    onRequestOverlay: () -> Unit,
    onRequestNotifications: () -> Unit,
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }

    // Auto-advance step if permission was granted
    LaunchedEffect(usageAccessGranted, overlayGranted, notificationGranted) {
        if (currentStep == 1 && usageAccessGranted) {
            currentStep = 2
        }
        if (currentStep == 2 && overlayGranted) {
            currentStep = 3
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BeigeBackground)
    ) {
        // Watermark Bonsai
        BonsaiWatermarkBackground(alpha = 0.08f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "白百合 • 導き", // Shirayuri • Guidance
                    fontSize = 12.sp,
                    color = ShuIro,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Начало пути",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = InkPrimary,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Поэтапная настройка для осознанного использования",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = InkSecondary,
                        fontSize = 12.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Progress Step Indicators (1, 2, 3)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StepIndicator(step = 1, currentStep = currentStep, isDone = usageAccessGranted)
                    StepDivider(isDone = usageAccessGranted)
                    StepIndicator(step = 2, currentStep = currentStep, isDone = overlayGranted)
                    StepDivider(isDone = overlayGranted)
                    StepIndicator(step = 3, currentStep = currentStep, isDone = notificationGranted)
                }
            }

            // Step Content Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BeigeSurface),
                border = BorderStroke(1.dp, BeigeBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                    label = "StepContent"
                ) { step ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (step) {
                            1 -> {
                                Text("🌾", fontSize = 44.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Этап 1: Доступ к использованию",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = InkPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Позволяет Kuku Timer определять момент запуска выбранных приложений, чтобы вовремя активировать 10-минутное ожидание.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = InkSecondary,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                if (!usageAccessGranted) {
                                    Button(
                                        onClick = onRequestUsageAccess,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ShuIro, contentColor = BeigeSurface)
                                    ) {
                                        Text("Предоставить доступ к истории", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    }
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MatsuGreenLight,
                                        border = BorderStroke(1.dp, MatsuGreen.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = "✓ Разрешение предоставлено",
                                            color = MatsuGreen,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                        )
                                    }
                                }
                            }
                            2 -> {
                                Text("🪟", fontSize = 44.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Этап 2: Показ поверх других окон",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = InkPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Необходимо для отображения экрана блокировки с таймером и дышащим кругом прямо поверх выбранного приложения.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = InkSecondary,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                if (!overlayGranted) {
                                    Button(
                                        onClick = onRequestOverlay,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ShuIro, contentColor = BeigeSurface)
                                    ) {
                                        Text("Разрешить показ поверх окон", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    }
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MatsuGreenLight,
                                        border = BorderStroke(1.dp, MatsuGreen.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = "✓ Разрешение предоставлено",
                                            color = MatsuGreen,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                        )
                                    }
                                }
                            }
                            3 -> {
                                Text("🔔", fontSize = 44.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Этап 3: Оповещение о готовности",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = InkPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Система уведомит вас ровно через 10 минут, когда «рис сварится», чтобы вы успели войти в приложение в 2-минутное окно.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = InkSecondary,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                if (!notificationGranted) {
                                    Button(
                                        onClick = onRequestNotifications,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ShuIro, contentColor = BeigeSurface)
                                    ) {
                                        Text("Включить уведомления", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    }
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MatsuGreenLight,
                                        border = BorderStroke(1.dp, MatsuGreen.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = "✓ Уведомления включены",
                                            color = MatsuGreen,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentStep < 3) {
                    Button(
                        onClick = { currentStep++ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = InkPrimary, contentColor = BeigeSurface)
                    ) {
                        Text("Следующий этап →", fontSize = 14.sp)
                    }
                } else {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ShuIro, contentColor = BeigeSurface)
                    ) {
                        Text("🍙 Начать использование Kuku Timer", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }

                if (currentStep > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { currentStep-- }) {
                        Text("← Назад", color = InkSecondary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(step: Int, currentStep: Int, isDone: Boolean) {
    val isCurrent = step == currentStep
    val bgColor = when {
        isDone -> MatsuGreen
        isCurrent -> ShuIro
        else -> BeigeCard
    }
    val textColor = when {
        isDone || isCurrent -> BeigeSurface
        else -> InkTertiary
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, if (isCurrent) ShuIro else BeigeBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isDone) "✓" else "$step",
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StepDivider(isDone: Boolean) {
    Box(
        modifier = Modifier
            .width(28.dp)
            .height(2.dp)
            .background(if (isDone) MatsuGreen else BeigeBorder)
    )
}
