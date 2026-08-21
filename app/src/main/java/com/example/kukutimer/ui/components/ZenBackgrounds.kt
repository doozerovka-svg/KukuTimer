package com.example.kukutimer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.kukutimer.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Semi-transparent, subtle Japanese Bonsai Tree watermark for MainActivity
 */
@Composable
fun BonsaiWatermarkBackground(
    modifier: Modifier = Modifier,
    alpha: Float = 0.06f
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val inkColor = InkPrimary.copy(alpha = alpha)
        val greenColor = MatsuGreen.copy(alpha = alpha * 1.2f)

        // Center bonsai at the bottom right quadrant
        val originX = w * 0.65f
        val originY = h * 0.82f
        val scale = w * 0.0022f

        // Pot
        val potPath = Path().apply {
            moveTo(originX - 70f * scale, originY + 15f * scale)
            lineTo(originX + 70f * scale, originY + 15f * scale)
            lineTo(originX + 55f * scale, originY + 38f * scale)
            lineTo(originX - 55f * scale, originY + 38f * scale)
            close()
        }
        drawPath(potPath, color = inkColor, style = Fill)
        drawPath(potPath, color = inkColor, style = Stroke(width = 1.5f))

        // Pot feet
        drawRect(inkColor, Offset(originX - 50f * scale, originY + 38f * scale), Size(12f * scale, 5f * scale))
        drawRect(inkColor, Offset(originX + 38f * scale, originY + 38f * scale), Size(12f * scale, 5f * scale))

        // Trunk (Gnarled Bonsai curves)
        val trunkPath = Path().apply {
            moveTo(originX - 18f * scale, originY + 14f * scale)
            // S-curve trunk
            cubicTo(
                originX - 35f * scale, originY - 30f * scale,
                originX + 25f * scale, originY - 70f * scale,
                originX - 10f * scale, originY - 120f * scale
            )
            cubicTo(
                originX - 20f * scale, originY - 145f * scale,
                originX - 5f * scale, originY - 165f * scale,
                originX + 5f * scale, originY - 180f * scale
            )
            // Right branch
            cubicTo(
                originX + 30f * scale, originY - 150f * scale,
                originX + 60f * scale, originY - 130f * scale,
                originX + 75f * scale, originY - 125f * scale
            )
            lineTo(originX + 70f * scale, originY - 118f * scale)
            // Trunk back down
            cubicTo(
                originX + 45f * scale, originY - 125f * scale,
                originX + 15f * scale, originY - 140f * scale,
                originX + 8f * scale, originY - 110f * scale
            )
            cubicTo(
                originX + 38f * scale, originY - 60f * scale,
                originX - 12f * scale, originY - 20f * scale,
                originX + 14f * scale, originY + 14f * scale
            )
            close()
        }
        drawPath(trunkPath, color = inkColor, style = Fill)

        // Pine Foliage Clouds (松の葉雲)
        drawPineCloud(originX + 5f * scale, originY - 190f * scale, 55f * scale, 28f * scale, greenColor)
        drawPineCloud(originX + 80f * scale, originY - 130f * scale, 48f * scale, 24f * scale, greenColor)
        drawPineCloud(originX - 45f * scale, originY - 135f * scale, 50f * scale, 25f * scale, greenColor)
        drawPineCloud(originX - 65f * scale, originY - 80f * scale, 42f * scale, 22f * scale, greenColor)
        drawPineCloud(originX + 40f * scale, originY - 75f * scale, 38f * scale, 20f * scale, greenColor)
    }
}

private fun DrawScope.drawPineCloud(cx: Float, cy: Float, rx: Float, ry: Float, color: Color) {
    drawOval(
        color = color,
        topLeft = Offset(cx - rx, cy - ry),
        size = Size(rx * 2, ry * 2)
    )
    drawOval(
        color = color,
        topLeft = Offset(cx - rx * 0.7f, cy - ry * 1.25f),
        size = Size(rx * 1.4f, ry * 1.6f)
    )
}

/**
 * Semi-transparent, subtle Japanese Rice Bowl (茶碗 / 丼) watermark with steam for Lock Screen
 */
@Composable
fun RiceBowlWatermarkBackground(
    modifier: Modifier = Modifier,
    alpha: Float = 0.07f
) {
    val transition = rememberInfiniteTransition(label = "SteamAnim")
    val steamOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Steam"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val inkColor = InkPrimary.copy(alpha = alpha)
        val riceColor = KinGold.copy(alpha = alpha * 1.5f)

        val cx = w * 0.5f
        val cy = h * 0.52f
        val scale = w * 0.0028f

        // 1. Bowl Base Ring (高台)
        drawRoundRect(
            color = inkColor,
            topLeft = Offset(cx - 35f * scale, cy + 95f * scale),
            size = Size(70f * scale, 12f * scale),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * scale)
        )

        // 2. Bowl Body (Donburi Ceramic Silhouette)
        val bowlPath = Path().apply {
            moveTo(cx - 110f * scale, cy)
            cubicTo(
                cx - 105f * scale, cy + 65f * scale,
                cx - 60f * scale, cy + 95f * scale,
                cx - 35f * scale, cy + 95f * scale
            )
            lineTo(cx + 35f * scale, cy + 95f * scale)
            cubicTo(
                cx + 60f * scale, cy + 95f * scale,
                cx + 105f * scale, cy + 65f * scale,
                cx + 110f * scale, cy
            )
            cubicTo(
                cx + 70f * scale, cy - 10f * scale,
                cx - 70f * scale, cy - 10f * scale,
                cx - 110f * scale, cy
            )
            close()
        }
        drawPath(bowlPath, color = inkColor, style = Stroke(width = 2f * scale))

        // 3. Steaming Mound of Rice (ふっくらご飯)
        val riceMoundPath = Path().apply {
            moveTo(cx - 100f * scale, cy - 2f * scale)
            cubicTo(
                cx - 85f * scale, cy - 55f * scale,
                cx - 30f * scale, cy - 75f * scale,
                cx, cy - 75f * scale
            )
            cubicTo(
                cx + 30f * scale, cy - 75f * scale,
                cx + 85f * scale, cy - 55f * scale,
                cx + 100f * scale, cy - 2f * scale
            )
            close()
        }
        drawPath(riceMoundPath, color = riceColor, style = Fill)
        drawPath(riceMoundPath, color = inkColor, style = Stroke(width = 1.5f * scale))

        // 4. Subtle Rising Steam Curls (湯気)
        drawSteamCurve(cx - 35f * scale, cy - 85f * scale - steamOffset, 45f * scale, inkColor)
        drawSteamCurve(cx, cy - 95f * scale - steamOffset * 1.2f, 55f * scale, inkColor)
        drawSteamCurve(cx + 35f * scale, cy - 85f * scale - steamOffset, 45f * scale, inkColor)
    }
}

private fun DrawScope.drawSteamCurve(startX: Float, startY: Float, height: Float, color: Color) {
    val path = Path().apply {
        moveTo(startX, startY)
        cubicTo(
            startX - 12f, startY - height * 0.35f,
            startX + 12f, startY - height * 0.7f,
            startX, startY - height
        )
    }
    drawPath(path, color = color, style = Stroke(width = 2f, cap = StrokeCap.Round))
}

/**
 * Unique Rice Grain Zen Dial Spinner (Крутилка с рисовыми зернами)
 */
@Composable
fun RiceGrainDial(
    modifier: Modifier = Modifier,
    isReady: Boolean = false,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RiceOrbit")
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbitAngle"
    )

    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Breathing"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = (size.minDimension / 2f) - 24.dp.toPx()
            val center = Offset(size.width / 2f, size.height / 2f)

            // Outer delicate Zen stone ring
            drawCircle(
                color = BeigeBorder,
                radius = radius * breathingScale + 12.dp.toPx(),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Inner orbit track
            drawCircle(
                color = if (isReady) KinGold.copy(alpha = 0.4f) else ShuIro.copy(alpha = 0.25f),
                radius = radius * breathingScale,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 12 Animated Rice Grains (米粒) orbiting around the dial
            val grainCount = 12
            for (i in 0 until grainCount) {
                val angleDeg = orbitAngle + (i * (360f / grainCount))
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val gx = center.x + (radius * breathingScale * cos(angleRad)).toFloat()
                val gy = center.y + (radius * breathingScale * sin(angleRad)).toFloat()

                // Draw each oval rice grain tilted along orbit tangent
                drawRiceGrain(
                    center = Offset(gx, gy),
                    angleDeg = angleDeg.toFloat() + 45f,
                    length = 13.dp.toPx(),
                    width = 6.dp.toPx(),
                    fillColor = if (isReady) KinGold else (if (i % 2 == 0) ShuIro else RiceGrainColor),
                    borderColor = if (isReady) KinGold else (if (i % 2 == 0) ShuIro else RiceGrainBorder)
                )
            }
        }

        // Inner Content
        content()
    }
}

private fun DrawScope.drawRiceGrain(
    center: Offset,
    angleDeg: Float,
    length: Float,
    width: Float,
    fillColor: Color,
    borderColor: Color
) {
    val rad = Math.toRadians(angleDeg.toDouble())
    val cosA = cos(rad).toFloat()
    val sinA = sin(rad).toFloat()

    val path = Path().apply {
        // Pointed oval rice seed
        val halfL = length / 2f
        val halfW = width / 2f

        val p1 = Offset(center.x - halfL * cosA, center.y - halfL * sinA)
        val p2 = Offset(center.x + halfL * cosA, center.y + halfL * sinA)

        val cp1 = Offset(center.x - halfW * sinA, center.y + halfW * cosA)
        val cp2 = Offset(center.x + halfW * sinA, center.y - halfW * cosA)

        moveTo(p1.x, p1.y)
        quadraticBezierTo(cp1.x, cp1.y, p2.x, p2.y)
        quadraticBezierTo(cp2.x, cp2.y, p1.x, p1.y)
        close()
    }

    drawPath(path, color = fillColor, style = Fill)
    drawPath(path, color = borderColor, style = Stroke(width = 1.2f))
}
