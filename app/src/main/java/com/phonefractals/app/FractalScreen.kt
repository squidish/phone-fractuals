package com.phonefractals.app

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * One screen: pick a fractal, watch it animate, drag the slider to change detail.
 * State lives here; the math lives in Fractals.kt so each half stays easy to read.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FractalScreen() {
    var fractalType by remember { mutableStateOf(FractalType.TREE) }
    var depth by remember { mutableFloatStateOf(FractalType.TREE.defaultDepth.toFloat()) }

    val infiniteTransition = rememberInfiniteTransition(label = "fractal-animation")

    // Cycles the whole color palette through the hue wheel once every 12s.
    val hueShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 12_000, easing = LinearEasing)),
        label = "hue",
    )

    // Drives the gentle sway/pulse seen across all three fractals.
    val sway by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "sway",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FractalType.entries.forEach { type ->
                FilterChip(
                    selected = fractalType == type,
                    onClick = {
                        fractalType = type
                        depth = type.defaultDepth.toFloat()
                    },
                    label = { Text(type.label) },
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .semantics {
                    contentDescription = "Animated colorful ${fractalType.label} fractal"
                },
        ) {
            drawFractal(type = fractalType, depth = depth.toInt(), hueShift = hueShift, sway = sway)
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("Detail: ${depth.toInt()}", color = Color.White)
            Slider(
                value = depth,
                onValueChange = { depth = it },
                valueRange = fractalType.depthRange.first.toFloat()..fractalType.depthRange.last.toFloat(),
                steps = (fractalType.depthRange.last - fractalType.depthRange.first - 1)
                    .coerceAtLeast(0),
            )
        }
    }
}
