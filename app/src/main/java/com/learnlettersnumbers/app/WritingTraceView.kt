package com.learnlettersnumbers.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import kotlin.math.min

/** Lightweight finger-writing board. The complete glyph remains visible, including dots. */
class WritingTraceView(context: Context) : View(context) {
    private val glyphPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(70, 86, 110)
        alpha = 82
        style = Paint.Style.FILL
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }

    private val writingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(245, 105, 55)
        alpha = 245
        style = Paint.Style.STROKE
        strokeWidth = 36f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private var symbol = "A"
    private var lastKey = ""
    private var glyphPath = Path()
    private var glyphBounds = RectF()
    private val writingPath = Path()
    private var writing = false

    fun setLesson(newSymbol: String, replay: Int = 0) {
        val key = "$newSymbol|$replay|$width|$height"
        if (key == lastKey) return
        lastKey = key
        symbol = newSymbol
        writing = false
        writingPath.reset()
        rebuildGlyph()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        lastKey = ""
        rebuildGlyph()
    }

    private fun rebuildGlyph() {
        if (width < 20 || height < 20) return
        glyphPath = Path()
        glyphPaint.textSize = min(width.toFloat(), height.toFloat()) * 0.80f
        val baseline = height * 0.69f
        glyphPaint.getTextPath(symbol, 0, symbol.length, width / 2f, baseline, glyphPath)
        glyphBounds = RectF()
        glyphPath.computeBounds(glyphBounds, true)
    }

    private fun insideGuide(x: Float, y: Float): Boolean {
        val r = RectF(glyphBounds)
        r.inset(-55f, -55f)
        return r.contains(x, y)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (!insideGuide(event.x, event.y)) return true
                writing = true
                writingPath.reset()
                writingPath.moveTo(event.x, event.y)
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (writing && insideGuide(event.x, event.y)) {
                    writingPath.lineTo(event.x, event.y)
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                writing = false
                invalidate()
                return true
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width < 20 || height < 20) return
        canvas.drawColor(Color.rgb(242, 247, 255))
        rebuildGlyph()

        // Draw the complete guide first so the Arabic dots are always present.
        canvas.drawPath(glyphPath, glyphPaint)

        // Draw the child's finger stroke on top and keep it visible while dragging.
        if (!writingPath.isEmpty) canvas.drawPath(writingPath, writingPaint)
    }
}
