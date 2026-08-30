package com.learnlettersnumbers.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import kotlin.math.min

/** Lightweight writing guide: complete glyph, clear start point and fixed direction arrows. */
class WritingTraceView(context: Context) : View(context) {
    private val glyphPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(120, 132, 148)
        alpha = 75
        style = Paint.Style.FILL
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    private val startPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(35, 175, 92)
        style = Paint.Style.FILL
    }
    private val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(30, 115, 225)
        style = Paint.Style.STROKE
        strokeWidth = 7f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(35, 120, 85)
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    private var symbol = "A"
    private var lastKey = ""

    fun setLesson(newSymbol: String, replay: Int = 0) {
        val key = "$newSymbol|$replay|$width|$height"
        if (key == lastKey) return
        lastKey = key
        symbol = newSymbol
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        lastKey = ""
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    private fun isArabic(): Boolean = symbol.any { it in '\u0600'..'\u06FF' }

    private fun drawArrow(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float) {
        canvas.drawLine(x1, y1, x2, y2, arrowPaint)
        val angle = kotlin.math.atan2((y2 - y1).toDouble(), (x2 - x1).toDouble())
        val head = 24f
        val a1 = angle + Math.PI * 0.80
        val a2 = angle - Math.PI * 0.80
        canvas.drawLine(x2, y2, x2 + kotlin.math.cos(a1).toFloat() * head, y2 + kotlin.math.sin(a1).toFloat() * head, arrowPaint)
        canvas.drawLine(x2, y2, x2 + kotlin.math.cos(a2).toFloat() * head, y2 + kotlin.math.sin(a2).toFloat() * head, arrowPaint)
    }

    private fun startPoint(w: Float, h: Float): Pair<Float, Float> {
        if (!isArabic()) return w * 0.30f to h * 0.68f
        return when (symbol.firstOrNull()) {
            'ا' -> w * 0.58f to h * 0.25f
            'ب','ت','ث','ي' -> w * 0.78f to h * 0.60f
            'ج','ح','خ' -> w * 0.76f to h * 0.38f
            'د','ذ','ر','ز' -> w * 0.72f to h * 0.35f
            'س','ش' -> w * 0.78f to h * 0.48f
            'ص','ض','ط','ظ' -> w * 0.70f to h * 0.32f
            'ع','غ','ف','ق' -> w * 0.76f to h * 0.40f
            'ك','ل' -> w * 0.62f to h * 0.28f
            'م','ن','ه','و' -> w * 0.75f to h * 0.42f
            else -> w * 0.72f to h * 0.40f
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w < 20f || h < 20f) return

        canvas.drawColor(Color.rgb(242, 247, 255))
        glyphPaint.textSize = min(w, h) * 0.78f
        val baseline = h * 0.68f
        canvas.drawText(symbol, w / 2f, baseline, glyphPaint)

        val (sx, sy) = startPoint(w, h)
        canvas.drawCircle(sx, sy, 18f, startPaint)
        canvas.drawCircle(sx, sy, 7f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE })
        labelPaint.textSize = min(w, h) * 0.045f
        canvas.drawText("ابدأ من هنا", sx, (sy - 27f).coerceAtLeast(30f), labelPaint)

        // Fixed arrows only. No animated tracing and no skeleton calculation.
        if (isArabic()) {
            val y = h * 0.57f
            drawArrow(canvas, w * 0.82f, y, w * 0.67f, y)
            drawArrow(canvas, w * 0.63f, y, w * 0.48f, y)
            drawArrow(canvas, w * 0.44f, y, w * 0.34f, y + h * 0.05f)
        } else {
            val y = h * 0.63f
            drawArrow(canvas, w * 0.27f, y, w * 0.41f, y)
            drawArrow(canvas, w * 0.46f, y, w * 0.60f, y)
            drawArrow(canvas, w * 0.65f, y, w * 0.74f, y - h * 0.04f)
        }
    }
}
