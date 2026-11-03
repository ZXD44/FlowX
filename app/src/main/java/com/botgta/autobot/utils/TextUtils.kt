package com.botgta.autobot.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * Utility functions for text processing and styling.
 */
object TextUtils {
    /**
     * Enhanced Markdown parser for basic formatting:
     * - Bold: **text**
     * - Italic: *text* or _text_
     * - Bullet points: - text
     */
    fun parseMarkdown(text: String): AnnotatedString {
        return buildAnnotatedString {
            val lines = text.split("\n")
            lines.forEachIndexed { index, line ->
                val isBullet = line.trimStart().startsWith("- ")
                val processedLine = if (isBullet) {
                    val indent = line.substringBefore("- ")
                    append(indent)
                    append("• ")
                    line.substringAfter("- ")
                } else {
                    line
                }

                renderInlineStyles(processedLine)
                
                if (index < lines.size - 1) {
                    append("\n")
                }
            }
        }
    }

    private fun AnnotatedString.Builder.renderInlineStyles(text: String) {
        var currentIndex = 0
        // Combined regex for bold (**...**) and italic (*...* or _..._)
        // Note: This is a simple implementation and doesn't handle nested styles perfectly,
        // but covers the requested ** * _ -
        val pattern = """(\*\*.*?\*\*|\*.*?\*|_.*?_)""".toRegex()
        
        pattern.findAll(text).forEach { match ->
            val matchValue = match.value
            // Append text before the match
            append(text.substring(currentIndex, match.range.first))
            
            when {
                matchValue.startsWith("**") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Black)) {
                        append(matchValue.substring(2, matchValue.length - 2))
                    }
                }
                matchValue.startsWith("*") -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(matchValue.substring(1, matchValue.length - 1))
                    }
                }
                matchValue.startsWith("_") -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(matchValue.substring(1, matchValue.length - 1))
                    }
                }
            }
            currentIndex = match.range.last + 1
        }
        // Append remaining text
        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}
