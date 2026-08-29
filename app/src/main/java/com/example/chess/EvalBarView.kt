package com.example.chess

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

class EvalBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // -10 = Black advantage
    //  0  = Equal
    // +10 = White advantage
    private var evaluation = 0.0

    fun setEvaluation(value: Double) {
        evaluation = max(-10.0, min(10.0, value))
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        if (width <= 0 || height <= 0) return

        // BLACK side
        paint.color = Color.rgb(40, 40, 40)

        canvas.drawRect(
            0f,
            0f,
            width,
            height,
            paint
        )

        // Evaluation ko 0..1 me convert
        val whiteRatio =
            ((evaluation + 10.0) / 20.0)
                .coerceIn(0.0, 1.0)

        // White bar kitna upar tak jayega
        val whiteHeight =
            height * whiteRatio.toFloat()

        // WHITE side
        paint.color = Color.WHITE

        canvas.drawRect(
            0f,
            height - whiteHeight,
            width,
            height,
            paint
        )
    }
}