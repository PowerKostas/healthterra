package com.healthterra.helpers

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.graphics.Shape
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun drawCardShape(): Shape = GenericShape { size, _ ->
    val height = size.height
    val width = size.width

    moveTo(width / 2f, 0f)

    quadraticTo(width * 0.9f, height * 0.05f, width, height * 0.15f)
    lineTo(width, height * 0.85f)

    quadraticTo(width * 0.9f, height * 0.95f, width / 2f, height)
    quadraticTo(width * 0.1f, height * 0.95f, 0f, height * 0.85f)

    lineTo(0f, height * 0.15f)
    quadraticTo(width * 0.1f, height * 0.05f, width / 2f, 0f)

    close()
}

fun drawShieldShape(): Shape = GenericShape { size, _ ->
    val left = size.width * 0.04f
    val right = size.width - left

    moveTo((left + right) / 2f, 0f)
    lineTo(right, size.height * 0.15f)
    lineTo(right, size.height * 0.5f)
    quadraticTo(right, size.height * 0.82f, (left + right) / 2f, size.height)
    quadraticTo(left, size.height * 0.82f, left, size.height * 0.5f)
    lineTo(left, size.height * 0.15f)
    close()
}

fun drawStarburstShape(): Shape = GenericShape { size, _ ->
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val outerRadius = size.width / 2f
    val innerRadius = size.width * 0.35f
    val points = 8

    for (i in 0 until points * 2) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = (PI * i) / points - (PI / 2)
        val x = centerX + (radius * cos(angle)).toFloat()
        val y = centerY + (radius * sin(angle)).toFloat()

        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }

    close()
}
