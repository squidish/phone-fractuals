package com.phonefractals.app

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Each fractal type owns the "detail" range a user can dial in with the slider,
 * since a recursive tree and a Koch snowflake explode in line count at very
 * different rates.
 */
enum class FractalType(val label: String, val depthRange: IntRange, val defaultDepth: Int) {
    TREE("Tree", 3..12, 9),
    SIERPINSKI("Sierpinski", 1..8, 5),
    KOCH("Snowflake", 0..5, 3),
}

/** Entry point: draws the selected fractal centered in the current canvas. */
fun DrawScope.drawFractal(type: FractalType, depth: Int, hueShift: Float, sway: Float) {
    when (type) {
        FractalType.TREE -> drawTree(
            start = Offset(size.width / 2f, size.height * 0.95f),
            length = size.height * 0.28f,
            angleDeg = 90f,
            depth = depth,
            hueShift = hueShift,
            sway = sway,
        )

        FractalType.SIERPINSKI -> {
            val side = size.minDimension * 0.85f
            val top = Offset(size.width / 2f, size.height / 2f - side / 2f)
            val left = Offset(size.width / 2f - side / 2f, size.height / 2f + side / 2f)
            val right = Offset(size.width / 2f + side / 2f, size.height / 2f + side / 2f)
            drawSierpinski(top, left, right, depth, hueShift)
        }

        FractalType.KOCH -> drawKochSnowflake(
            center = Offset(size.width / 2f, size.height / 2f),
            radius = size.minDimension * 0.4f,
            depth = depth,
            hueShift = hueShift,
            sway = sway,
        )
    }
}

private fun DrawScope.drawTree(
    start: Offset,
    length: Float,
    angleDeg: Float,
    depth: Int,
    hueShift: Float,
    sway: Float,
) {
    if (depth == 0 || length < 2f) return

    val angleRad = angleDeg * PI.toFloat() / 180f
    val end = Offset(start.x + length * cos(angleRad), start.y - length * sin(angleRad))

    val hue = (hueShift + depth * 22f) % 360f
    drawLine(
        color = Color.hsv(hue, 0.85f, 1f),
        start = start,
        end = end,
        strokeWidth = (depth * 1.6f).coerceAtLeast(1.5f),
    )

    // The sway term (driven by an animated -1..1 value) makes the branch
    // angle breathe over time so the whole tree gently sways.
    val branchAngle = 26f + sway * 6f
    drawTree(end, length * 0.74f, angleDeg + branchAngle, depth - 1, hueShift, sway)
    drawTree(end, length * 0.74f, angleDeg - branchAngle, depth - 1, hueShift, sway)
}

private fun DrawScope.drawSierpinski(
    top: Offset,
    left: Offset,
    right: Offset,
    depth: Int,
    hueShift: Float,
) {
    if (depth == 0) {
        val hue = (hueShift + (top.x + top.y) % 360f) % 360f
        val triangle = Path().apply {
            moveTo(top.x, top.y)
            lineTo(left.x, left.y)
            lineTo(right.x, right.y)
            close()
        }
        drawPath(triangle, color = Color.hsv(hue, 0.9f, 1f))
        return
    }

    val topLeft = midpoint(top, left)
    val topRight = midpoint(top, right)
    val bottomMid = midpoint(left, right)
    drawSierpinski(top, topLeft, topRight, depth - 1, hueShift)
    drawSierpinski(topLeft, left, bottomMid, depth - 1, hueShift)
    drawSierpinski(topRight, bottomMid, right, depth - 1, hueShift)
}

private fun midpoint(a: Offset, b: Offset) = Offset((a.x + b.x) / 2f, (a.y + b.y) / 2f)

private fun DrawScope.drawKochSnowflake(
    center: Offset,
    radius: Float,
    depth: Int,
    hueShift: Float,
    sway: Float,
) {
    val p1 = Offset(center.x, center.y - radius)
    val p2 = Offset(center.x - radius * 0.866f, center.y + radius * 0.5f)
    val p3 = Offset(center.x + radius * 0.866f, center.y + radius * 0.5f)

    // Subtle pulsing scale, also driven by the shared sway value.
    val pulse = 1f + sway * 0.03f
    listOf(p1 to p2, p2 to p3, p3 to p1).forEachIndexed { index, (a, b) ->
        val hue = (hueShift + index * 120f) % 360f
        drawKochSegment(a, b, depth, hue, pulse, center)
    }
}

private fun DrawScope.drawKochSegment(
    a: Offset,
    b: Offset,
    depth: Int,
    hue: Float,
    pulse: Float,
    center: Offset,
) {
    if (depth == 0) {
        drawLine(
            color = Color.hsv(hue, 0.9f, 1f),
            start = scaleFrom(center, a, pulse),
            end = scaleFrom(center, b, pulse),
            strokeWidth = 3f,
        )
        return
    }

    val dx = (b.x - a.x) / 3f
    val dy = (b.y - a.y) / 3f
    val p1 = Offset(a.x + dx, a.y + dy)
    val p3 = Offset(a.x + 2 * dx, a.y + 2 * dy)

    val segmentLength = hypot(dx, dy)
    val angle = atan2(dy, dx) - PI.toFloat() / 3f
    val p2 = Offset(p1.x + segmentLength * cos(angle), p1.y + segmentLength * sin(angle))

    drawKochSegment(a, p1, depth - 1, hue, pulse, center)
    drawKochSegment(p1, p2, depth - 1, hue, pulse, center)
    drawKochSegment(p2, p3, depth - 1, hue, pulse, center)
    drawKochSegment(p3, b, depth - 1, hue, pulse, center)
}

private fun scaleFrom(center: Offset, point: Offset, factor: Float) = Offset(
    center.x + (point.x - center.x) * factor,
    center.y + (point.y - center.y) * factor,
)
