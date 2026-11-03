package com.botgta.autobot.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Doodle / Neobrutalism modifier.
 * Creates a thick black border and a solid offset shadow.
 */
fun Modifier.neumorphic(
    radius: Dp = 8.dp, // Use smaller radius for doodle style
    elevation: Dp = 6.dp, // This is now the solid shadow offset
    isDarkMode: Boolean = false
): Modifier = drawBehind {
    val borderColor = if (isDarkMode) Color.White else Color(0xFF1A1A1A)
    val shadowColor = if (isDarkMode) Color.Black else Color(0xFF1A1A1A)
    val strokeWidth = 3.dp.toPx()
    val offset = elevation.toPx()

    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        
        // 1. Solid Drop Shadow
        frameworkPaint.color = shadowColor.toArgb()
        frameworkPaint.style = android.graphics.Paint.Style.FILL
        canvas.drawRoundRect(
            offset, offset, size.width + offset, size.height + offset,
            radius.toPx(), radius.toPx(),
            paint
        )
        
        // 2. Thick Border (Doodle Line)
        frameworkPaint.color = borderColor.toArgb()
        frameworkPaint.style = android.graphics.Paint.Style.STROKE
        frameworkPaint.strokeWidth = strokeWidth
        frameworkPaint.strokeJoin = android.graphics.Paint.Join.ROUND
        
        canvas.drawRoundRect(
            0f, 0f, size.width, size.height,
            radius.toPx(), radius.toPx(),
            paint
        )
    }
}
