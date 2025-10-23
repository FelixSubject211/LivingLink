package felix.projekt.livinglink.composeApp.ui.core.view

import androidx.compose.ui.graphics.Color
import kotlin.math.pow

fun colorFromName(name: String): Color {
    if (name.isBlank()) return Color.Gray

    val lowercase = name.lowercase()
    val weightedSum = lowercase.mapIndexed { index, c ->
        val weight = 1.0 / (index + 1.0).pow(0.1)
        ((c.code + index * 37) % 256) * weight
    }.sum()
    val totalWeight = lowercase.mapIndexed { i, _ -> 1.0 / (i + 1.0).pow(0.8) }.sum()
    val hue = ((weightedSum / totalWeight) * 10 % 360).toFloat()
    return Color.hsl(hue, 0.65f, 0.55f)
}