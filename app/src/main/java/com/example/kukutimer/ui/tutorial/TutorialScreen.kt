package com.example.kukutimer.ui.tutorial

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kukutimer.theme.*
import com.example.kukutimer.ui.components.BonsaiWatermarkBackground

data class TutorialSlideData(
    val kanji: String,
    val subtitle: String,
    val title: String,
    val description: String,
    val takeaway: String,
    val illustrationType: Int
)

@Composable
fun TutorialScreen(
    onFinish: () -> Unit,
    onSkip: () -> Unit,
    isModal: Boolean = false
) {
    var currentSlide by remember { mutableStateOf(0) }

    val slides = remember {
        listOf(
            TutorialSlideData(
                kanji = "一炊の夢",
                subtitle = "ФИЛОСОФИЯ И ПСИХОЛОГИЯ",
                title = "Пауза в котел риса",
                description = "В японском фольклоре и дзен-притчах время, пока варится котел с рисом — это шанс на спасение и озарение.\n\nВместо жестких запретов Kuku Timer вводит осознанное отложенное вознаграждение (delayed gratification): импульсивное желание рассеивается за 10 минут ожидания.",
                takeaway = "💡 90% импульсов зайти в соцсети исчезают за 10 минут спокойной паузы.",
                illustrationType = 0
            ),
            TutorialSlideData(
                kanji = "選択と封印",
                subtitle = "ШАГ 1: ВЫБОР ПРИЛОЖЕНИЙ",
                title = "Выбираем цифровые соблазны",
                description = "В списке приложений выберите те, к которым у вас есть навязчивая привычка тянуться (TikTok, Instagram, игры, браузер или даже само это приложение).\n\nВключите переключатель напротив нужных программ — они будут взяты под защиту Kuku Timer.",
                takeaway = "📱 Вы сами определяете свои правила и уровень цифрового детокса.",
                illustrationType = 1
            ),
            TutorialSlideData(
                kanji = "十息一炊",
                subtitle = "ШАГ 2: ПЕРЕХВАТ И ОЖИДАНИЕ",
                title = "10 минут: «Рис закипает»",
                description = "Когда вы попытаетесь открыть заблокированное приложение, Kuku Timer выведет экран со спокойным кругом времени.\n\nПоложите телефон экраном вниз, сделайте глубокий вдох и вернитесь в реальный мир. Таймер продолжит идти в фоне.",
                takeaway = "⏳ Время не сбрасывается при выходе — 10 минут отсчитываются честно.",
                illustrationType = 2
            ),
            TutorialSlideData(
                kanji = "開門得道",
                subtitle = "ШАГ 3: ОКНО ВОЗМОЖНОСТЕЙ",
                title = "2 минуты на осознанный вход",
                description = "Ровно через 10 минут котел с рисом сварится — вы получите уведомление и круг озарится золотым свечением.\n\nУ вас есть 2 минуты: если вы войдете сейчас — приложение разблокируется на весь текущий сеанс до выключения экрана. Если пропустите — таймер перезапустится.",
                takeaway = "✨ Если приложение вам действительно нужно — вы зайдете. Если нет — вы спасли свое время.",
                illustrationType = 3
            )
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(BeigeBackground)
    ) {
        val screenHeight = maxHeight
        val isCompactScreen = screenHeight < 700.dp

        // Ambient Japanese Bonsai
        BonsaiWatermarkBackground(alpha = 0.06f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 24.dp,
                    vertical = if (isCompactScreen) 16.dp else 28.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Bar with Kanji & Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (isCompactScreen) 4.dp else 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ククターマー",
                        fontSize = 12.sp,
                        color = ShuIro,
                        letterSpacing = 4.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• 指南", // Guidance
                        fontSize = 12.sp,
                        color = KinGold,
                        letterSpacing = 2.sp
                    )
                }

                TextButton(
                    onClick = onSkip,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isModal) "Закрыть ✕" else "Пропустить →",
                        color = InkSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Interactive Slide Body
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = BeigeSurface),
                border = BorderStroke(1.dp, BeigeBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = if (isCompactScreen) 10.dp else 16.dp)
            ) {
                AnimatedContent(
                    targetState = currentSlide,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "SlideTransition",
                    modifier = Modifier.fillMaxSize()
                ) { slideIdx ->
                    val slide = slides[slideIdx]

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 22.dp, vertical = if (isCompactScreen) 14.dp else 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Kanji & Subtitle
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = slide.kanji,
                                fontSize = 14.sp,
                                color = ShuIro,
                                letterSpacing = 6.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = slide.subtitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = InkTertiary,
                                    fontSize = 10.sp,
                                    letterSpacing = 2.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = slide.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = InkPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = if (isCompactScreen) 18.sp else 21.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }

                        // Infographic Visual Canvas (Dynamic size based on screen)
                        Box(
                            modifier = Modifier
                                .size(if (isCompactScreen) 130.dp else 160.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            InfographicCanvas(type = slide.illustrationType)
                        }

                        // Description Text
                        Text(
                            text = slide.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = InkSecondary,
                                fontSize = if (isCompactScreen) 12.sp else 13.sp,
                                lineHeight = if (isCompactScreen) 17.sp else 19.sp,
                                textAlign = TextAlign.Center
                            )
                        )

                        // Takeaway Banner
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = KinGoldLight,
                            border = BorderStroke(1.dp, KinGold.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = slide.takeaway,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = InkPrimary,
                                    fontSize = if (isCompactScreen) 11.sp else 11.5.sp,
                                    lineHeight = 15.sp,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Navigation & Rice Grain Page Indicators
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Rice Grain Page Dots (米粒インジケーター)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = if (isCompactScreen) 10.dp else 16.dp)
                ) {
                    for (i in slides.indices) {
                        RiceGrainPageDot(isSelected = i == currentSlide) {
                            currentSlide = i
                        }
                    }
                }

                // Next / Complete Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (currentSlide > 0) {
                        OutlinedButton(
                            onClick = { currentSlide-- },
                            modifier = Modifier
                                .weight(1f)
                                .height(if (isCompactScreen) 46.dp else 50.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, BeigeBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = InkSecondary)
                        ) {
                            Text("← Назад", fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (currentSlide < slides.size - 1) {
                                currentSlide++
                            } else {
                                onFinish()
                            }
                        },
                        modifier = Modifier
                            .weight(if (currentSlide > 0) 1.6f else 1f)
                            .height(if (isCompactScreen) 46.dp else 50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentSlide == slides.size - 1) ShuIro else InkPrimary,
                            contentColor = BeigeSurface
                        )
                    ) {
                        Text(
                            text = if (currentSlide < slides.size - 1) "Далее →" else "Понятно, к настройке 🍙",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RiceGrainPageDot(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = if (isSelected) 22.dp else 12.dp, height = 10.dp)
            .clip(CircleShape)
            .background(if (isSelected) ShuIro else BeigeCard)
            .border(1.dp, if (isSelected) ShuIro else BeigeBorder, CircleShape)
            .clickable(onClick = onClick)
    )
}

/**
 * Custom Japanese Vector Infographics for each slide
 */
@Composable
private fun InfographicCanvas(type: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        when (type) {
            0 -> drawPhilosophyInfographic(cx, cy)
            1 -> drawAppSelectionInfographic(cx, cy)
            2 -> drawCookingWaitInfographic(cx, cy)
            3 -> drawOpportunityWindowInfographic(cx, cy)
        }
    }
}

// 1. Philosophy Infographic: Steaming Rice Bowl + Lotus Enso Circle
private fun DrawScope.drawPhilosophyInfographic(cx: Float, cy: Float) {
    val ensoRadius = 55.dp.toPx()

    // Ensō ring
    drawCircle(
        color = BeigeBorder,
        radius = ensoRadius,
        center = Offset(cx, cy),
        style = Stroke(width = 2.dp.toPx())
    )
    drawArc(
        color = ShuIro,
        startAngle = -30f,
        sweepAngle = 280f,
        useCenter = false,
        topLeft = Offset(cx - ensoRadius, cy - ensoRadius),
        size = Size(ensoRadius * 2, ensoRadius * 2),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )

    // Rice bowl silhouette in center
    val bowlScale = 0.55f
    val bPath = Path().apply {
        moveTo(cx - 30f * bowlScale, cy + 25f * bowlScale)
        lineTo(cx + 30f * bowlScale, cy + 25f * bowlScale)
        cubicTo(cx + 45f * bowlScale, cy + 5f * bowlScale, cx + 45f * bowlScale, cy - 15f * bowlScale, cx + 45f * bowlScale, cy - 15f * bowlScale)
        lineTo(cx - 45f * bowlScale, cy - 15f * bowlScale)
        cubicTo(cx - 45f * bowlScale, cy + 5f * bowlScale, cx - 30f * bowlScale, cy + 25f * bowlScale, cx - 30f * bowlScale, cy + 25f * bowlScale)
        close()
    }
    drawPath(bPath, color = InkPrimary, style = Stroke(width = 2.dp.toPx()))

    // Rice mound
    val rPath = Path().apply {
        moveTo(cx - 40f * bowlScale, cy - 15f * bowlScale)
        cubicTo(cx - 30f * bowlScale, cy - 35f * bowlScale, cx + 30f * bowlScale, cy - 35f * bowlScale, cx + 40f * bowlScale, cy - 15f * bowlScale)
        close()
    }
    drawPath(rPath, color = KinGold, style = Fill)

    // Steam curls
    drawCircle(ShuIro, radius = 3.dp.toPx(), center = Offset(cx - 10.dp.toPx(), cy - 26.dp.toPx()))
    drawCircle(ShuIro, radius = 4.dp.toPx(), center = Offset(cx, cy - 32.dp.toPx()))
    drawCircle(ShuIro, radius = 3.dp.toPx(), center = Offset(cx + 10.dp.toPx(), cy - 26.dp.toPx()))
}

// 2. App Selection Infographic: Phone Screen with Toggle Seal
private fun DrawScope.drawAppSelectionInfographic(cx: Float, cy: Float) {
    // Phone frame
    val pw = 65.dp.toPx()
    val ph = 95.dp.toPx()
    drawRoundRect(
        color = BeigeSurface,
        topLeft = Offset(cx - pw / 2, cy - ph / 2),
        size = Size(pw, ph),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
    )
    drawRoundRect(
        color = InkPrimary,
        topLeft = Offset(cx - pw / 2, cy - ph / 2),
        size = Size(pw, ph),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
        style = Stroke(width = 2.dp.toPx())
    )

    // App rows
    for (i in 0..2) {
        val rowY = cy - 25.dp.toPx() + (i * 24.dp.toPx())
        // App icon box
        drawRoundRect(
            color = if (i == 1) ShuIroLight else BeigeCard,
            topLeft = Offset(cx - pw / 2 + 8.dp.toPx(), rowY - 6.dp.toPx()),
            size = Size(12.dp.toPx(), 12.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
        )
        // Text line
        drawLine(
            color = if (i == 1) InkPrimary else InkTertiary,
            start = Offset(cx - pw / 2 + 24.dp.toPx(), rowY),
            end = Offset(cx + 5.dp.toPx(), rowY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        // Switch pill
        drawRoundRect(
            color = if (i == 1) ShuIro else BeigeBorder,
            topLeft = Offset(cx + pw / 2 - 18.dp.toPx(), rowY - 4.dp.toPx()),
            size = Size(12.dp.toPx(), 8.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
        )
    }
}

// 3. 10-Minute Cook Time Infographic: Stopwatch + Rice Grains
private fun DrawScope.drawCookingWaitInfographic(cx: Float, cy: Float) {
    val dialRadius = 50.dp.toPx()

    // Outer circle
    drawCircle(
        color = BeigeBorder,
        radius = dialRadius,
        center = Offset(cx, cy),
        style = Stroke(width = 2.dp.toPx())
    )

    // 10 Min Arc (10 of 12 = ~300 deg)
    drawArc(
        color = ShuIro,
        startAngle = -90f,
        sweepAngle = 270f,
        useCenter = false,
        topLeft = Offset(cx - dialRadius, cy - dialRadius),
        size = Size(dialRadius * 2, dialRadius * 2),
        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
    )

    // Center "10m" text marker
    drawCircle(BeigeSurface, radius = 28.dp.toPx(), center = Offset(cx, cy))
    drawCircle(ShuIro.copy(alpha = 0.2f), radius = 28.dp.toPx(), center = Offset(cx, cy), style = Stroke(1.dp.toPx()))

    // Clock hands
    drawLine(
        color = ShuIro,
        start = Offset(cx, cy),
        end = Offset(cx, cy - 16.dp.toPx()),
        strokeWidth = 2.5.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = ShuIro,
        start = Offset(cx, cy),
        end = Offset(cx + 12.dp.toPx(), cy),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )
}

// 4. Opportunity Window Infographic: Golden Gates & 2m Access
private fun DrawScope.drawOpportunityWindowInfographic(cx: Float, cy: Float) {
    val radius = 50.dp.toPx()

    // Golden Halo Circle
    drawCircle(
        color = KinGoldLight,
        radius = radius,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = KinGold,
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = 2.5.dp.toPx())
    )

    // Gateway / Torii outline
    val tw = 40.dp.toPx()
    val th = 40.dp.toPx()

    // Top beam
    drawLine(
        color = ShuIro,
        start = Offset(cx - tw / 2 - 6.dp.toPx(), cy - th / 2),
        end = Offset(cx + tw / 2 + 6.dp.toPx(), cy - th / 2),
        strokeWidth = 3.5.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = ShuIro,
        start = Offset(cx - tw / 2, cy - th / 2 + 8.dp.toPx()),
        end = Offset(cx + tw / 2, cy - th / 2 + 8.dp.toPx()),
        strokeWidth = 2.5.dp.toPx()
    )

    // Pillars
    drawLine(
        color = ShuIro,
        start = Offset(cx - tw / 2 + 5.dp.toPx(), cy - th / 2),
        end = Offset(cx - tw / 2 + 5.dp.toPx(), cy + th / 2 + 5.dp.toPx()),
        strokeWidth = 3.dp.toPx()
    )
    drawLine(
        color = ShuIro,
        start = Offset(cx + tw / 2 - 5.dp.toPx(), cy - th / 2),
        end = Offset(cx + tw / 2 - 5.dp.toPx(), cy + th / 2 + 5.dp.toPx()),
        strokeWidth = 3.dp.toPx()
    )

    // Central Sparkle
    drawCircle(KinGold, radius = 5.dp.toPx(), center = Offset(cx, cy + 5.dp.toPx()))
}
