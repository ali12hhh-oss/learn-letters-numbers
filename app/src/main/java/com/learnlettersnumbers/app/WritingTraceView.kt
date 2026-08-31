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

/**
 * Lightweight finger-writing guide.
 * The character/number remains visible as a faint guide and the child can
 * write directly over it with a finger. No animated hand, start marker,
 * directional arrow, or artificial starting-point restriction is shown.
 */
class WritingTraceView(context: Context) : View(context) {
    private val glyphPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(70, 86, 110)
        alpha = 58
        style = Paint.Style.FILL
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }

    private val writingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(245, 105, 55)
        alpha = 235
        style = Paint.Style.STROKE
        strokeWidth = 15f
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

    private fun arabicBase(s: String): String? {
        val forms = mapOf(
            "ا" to "ا", "ﺑ" to "ب", "ﺒ" to "ب", "ﺐ" to "ب",
            "ﺗ" to "ت", "ﺘ" to "ت", "ﺖ" to "ت", "ﺛ" to "ث", "ﺜ" to "ث", "ﺚ" to "ث",
            "ﺟ" to "ج", "ﺠ" to "ج", "ﺞ" to "ج", "ﺣ" to "ح", "ﺤ" to "ح", "ﺢ" to "ح",
            "ﺧ" to "خ", "ﺨ" to "خ", "ﺦ" to "خ", "د" to "د", "ﺪ" to "د", "ذ" to "ذ", "ﺬ" to "ذ",
            "ر" to "ر", "ﺮ" to "ر", "ز" to "ز", "ﺰ" to "ز", "ﺳ" to "س", "ﺴ" to "س", "ﺲ" to "س",
            "ﺷ" to "ش", "ﺸ" to "ش", "ﺶ" to "ش", "ﺻ" to "ص", "ﺼ" to "ص", "ﺺ" to "ص",
            "ﺿ" to "ض", "ﻀ" to "ض", "ﺾ" to "ض", "ﻃ" to "ط", "ﻄ" to "ط", "ﻂ" to "ط",
            "ﻇ" to "ظ", "ﻈ" to "ظ", "ﻆ" to "ظ", "ﻋ" to "ع", "ﻌ" to "ع", "ﻊ" to "ع",
            "ﻏ" to "غ", "ﻐ" to "غ", "ﻎ" to "غ", "ﻓ" to "ف", "ﻔ" to "ف", "ﻒ" to "ف",
            "ﻗ" to "ق", "ﻘ" to "ق", "ﻖ" to "ق", "ﻛ" to "ك", "ﻜ" to "ك", "ﻚ" to "ك",
            "ﻟ" to "ل", "ﻠ" to "ل", "ﻞ" to "ل", "ﻣ" to "م", "ﻤ" to "م", "ﻢ" to "م",
            "ﻧ" to "ن", "ﻨ" to "ن", "ﻦ" to "ن", "ﻫ" to "ه", "ﻬ" to "ه", "ﻪ" to "ه",
            "و" to "و", "ﻮ" to "و", "ﻳ" to "ي", "ﻴ" to "ي", "ﻲ" to "ي"
        )
        return forms[s]
    }

    private fun insideGuide(x: Float, y: Float): Boolean {
        val r = RectF(glyphBounds)
        r.inset(-45f, -45f)
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

        // Large, faint character/number used as the writing guide.
        canvas.drawPath(glyphPath, glyphPaint)

        // Only the child's finger stroke is rendered.
        if (!writingPath.isEmpty) canvas.drawPath(writingPath, writingPaint)
    }
}
