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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kukutimer.theme.*
import com.example.kukutimer.ui.components.BonsaiWatermarkBackground
import com.example.kukutimer.ui.tutorial.TutorialScreen

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
    var showTutorial by remember { mutableStateOf(true) }
    var currentStep by remember { mutableStateOf(1) }

    if (showTutorial) {
        TutorialScreen(
            onFinish = { showTutorial = false },
            onSkip = { showTutorial = false }
        )
        return
    }

    // Auto-advance step if permission was granted
    LaunchedEffect(usageAccessGranted, overlayGranted, notificationGranted) {
        if (currentStep == 1 && usageAccessGranted) {
            currentStep = 2
        }
        if (currentStep == 2 && overlayGranted) {
            currentStep = 3
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(BeigeBackground)
    ) {
        val screenHeight = maxHeight
        val isCompact = screenHeight < 680.dp

        // Watermark Bonsai
        BonsaiWatermarkBackground(alpha = 0.08f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 24.dp,
                    vertical = if (isCompact) 20.dp else 36.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = if (isCompact) 4.dp else 12.dp)
            ) {
                Text(
                    text = "白百合 • 導き", // Shirayuri • Guidance
                    fontSize = 12.sp,
                    color = ShuIro,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Начало пути",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = InkPrimary,
                        fontWeight = FontWeight.Light,
                        fontSize = if (isCompact) 20.sp else 24.sp,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Поэтапная настройка для осознанного использования",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = InkSecondary,
                        fontSize = 12.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(if (isCompact) 14.dp else 20.dp))

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
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = BeigeSurface),
                border = BorderStroke(1.dp, BeigeBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = if (isCompact) 8.dp else 14.dp)
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                    label = "StepContent"
                ) { step ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = if (isCompact) 18.dp else 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (step) {
                            1 -> {
                                Text("🌾", fontSize = if (isCompact) 36.sp else 44.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Этап 1: Доступ к использованию",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = InkPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Позволяет Kuku Timer определять момент запуска выбранных приложений, чтобы вовремя активировать 10-минутное ожидание.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = InkSecondary,
                                        fontSize = 12.5.sp,
                                        lineHeight = 18.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                if (!usageAccessGranted) {
                                    Button(
                                        onClick = onRequestUsageAccess,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
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
                                            fontSize = 12.5.sp,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                            2 -> {
                                Text("🪟", fontSize = if (isCompact) 36.sp else 44.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Этап 2: Показ поверх других окон",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = InkPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Необходимо для отображения экрана блокировки с таймером прямо поверх выбранного приложения.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = InkSecondary,
                                        fontSize = 12.5.sp,
                                        lineHeight = 18.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                if (!overlayGranted) {
                                    Button(
                                        onClick = onRequestOverlay,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
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
                                            fontSize = 12.5.sp,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                            3 -> {
                                Text("🔔", fontSize = if (isCompact) 36.sp else 44.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Этап 3: Оповещение о готовности",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = InkPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Система уведомит вас ровно через 10 минут, когда «рис сварится», чтобы вы успели войти в приложение в 2-минутное окно.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = InkSecondary,
                                        fontSize = 12.5.sp,
                                        lineHeight = 18.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                if (!notificationGranted) {
                                    Button(
                                        onClick = onRequestNotifications,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
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
                                            fontSize = 12.5.sp,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
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
                            .height(if (isCompact) 46.dp else 50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = InkPrimary, contentColor = BeigeSurface)
                    ) {
                        Text("Следующий этап →", fontSize = 13.sp)
                    }
                } else {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isCompact) 46.dp else 50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ShuIro, contentColor = BeigeSurface)
                    ) {
                        Text("🍙 Начать использование Kuku Timer", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }

                if (currentStep > 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(
                        onClick = { currentStep-- },
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text("← Назад", color = InkSecondary, fontSize = 12.sp)
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
            .size(30.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, if (isCurrent) ShuIro else BeigeBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isDone) "✓" else "$step",
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StepDivider(isDone: Boolean) {
    Box(
        modifier = Modifier
            .width(24.dp)
            .height(2.dp)
            .background(if (isDone) MatsuGreen else BeigeBorder)
    )
}
