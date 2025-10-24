package felix.projekt.livinglink.composeApp.ui.core.view

import androidx.compose.ui.graphics.Color

fun colorFromName(name: String): Color {
    if (name.isBlank()) return Color(0xFF9CAAB6)
    val normalized = name.lowercase().trim()

    val similarityValue = normalized.foldIndexed(0) { index, acc, char ->
        acc + (char.code * (index + 1))
    }
    val palette = listOf(
        Color(0xFF4E6E81),
        Color(0xFF6B7A8F),
        Color(0xFF8298A5),
        Color(0xFF374B58),
        Color(0xFF9FA8B2),
        Color(0xFF8B9EA8),
        Color(0xFF557A95),
        Color(0xFF3E5C76)
    )
    val index = (similarityValue % palette.size).coerceAtLeast(0)
    return palette[index]
}