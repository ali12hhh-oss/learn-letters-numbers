package com.learnlettersnumbers.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min

/**
 * Lightweight finger-tracing lesson.
 * The child starts at the green point and moves a finger directly over the glyph.
 * No animation, skeleton generation or expensive path processing is used.
 */
class WritingTraceView(context: Context) : View(context) {
    private val glyphPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(80, 96, 118)
        alpha = 62
        style = Paint.Style.FILL
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    private val guidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(38, 142, 235)
        alpha = 210
        style = Paint.Style.STROKE
        strokeWidth = 9f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val startPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(28, 184, 91)
        style = Paint.Style.FILL
    }
    private val startInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(30, 125, 82)
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    private val fingerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(245, 125, 55)
        alpha = 235
        style = Paint.Style.STROKE
        strokeWidth = 14f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private var symbol = "A"
    private var lastKey = ""
    private var glyphPath = Path()
    private var glyphBounds = RectF()
    private var startX = 0f
    private var startY = 0f
    private var tracing = false
    private var startedCorrectly = false
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private val fingerPath = Path()

    fun setLesson(newSymbol: String, replay: Int = 0) {
        val key = "$newSymbol|$replay|$width|$height"
        if (key == lastKey) return
        lastKey = key
        symbol = newSymbol
        tracing = false
        startedCorrectly = false
        fingerPath.reset()
        rebuildGlyph()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        lastKey = ""
        rebuildGlyph()
    }

    private fun isArabic(): Boolean = symbol.any { it in '\u0600'..'\u06FF' || it in '\uFB50'..'\uFDFF' }

    private fun rebuildGlyph() {
        if (width < 20 || height < 20) return
        glyphPath = Path()
        glyphPaint.textSize = min(width.toFloat(), height.toFloat()) * 0.78f
        val baseline = height * 0.68f
        glyphPaint.getTextPath(symbol, 0, symbol.length, width / 2f, baseline, glyphPath)
        glyphBounds = RectF()
        glyphPath.computeBounds(glyphBounds, true)
        val start = startPointFor(symbol, width.toFloat(), height.toFloat(), glyphBounds)
        startX = start.first
        startY = start.second
    }

    /**
     * Start points are intentionally specified per letter family rather than one generic point.
     * They are placed on the first stroke area of each glyph, not outside the character.
     */
    private fun startPointFor(s: String, w: Float, h: Float, b: RectF): Pair<Float, Float> {
        val key = arabicBase(s)
        if (key != null) {
            return when (key) {
                "ا" -> b.centerX() to b.top + b.height() * .08f
                "ب", "ت", "ث", "ن", "ي" -> b.right - b.width() * .12f to b.centerY() + b.height() * .05f
                "ج", "ح", "خ" -> b.right - b.width() * .12f to b.top + b.height() * .43f
                "د", "ذ" -> b.right - b.width() * .18f to b.top + b.height() * .18f
                "ر", "ز" -> b.right - b.width() * .20f to b.top + b.height() * .18f
                "س", "ش" -> b.right - b.width() * .10f to b.top + b.height() * .42f
                "ص", "ض" -> b.right - b.width() * .13f to b.top + b.height() * .22f
                "ط", "ظ" -> b.right - b.width() * .20f to b.top + b.height() * .10f
                "ع", "غ" -> b.right - b.width() * .12f to b.top + b.height() * .35f
                "ف", "ق" -> b.right - b.width() * .15f to b.top + b.height() * .32f
                "ك" -> b.right - b.width() * .12f to b.top + b.height() * .12f
                "ل" -> b.centerX() to b.top + b.height() * .06f
                "م" -> b.right - b.width() * .10f to b.top + b.height() * .38f
                "ه" -> b.right - b.width() * .10f to b.top + b.height() * .35f
                "و" -> b.right - b.width() * .15f to b.top + b.height() * .30f
                else -> b.centerX() to b.centerY()
            }
        }

        // English: each letter gets a distinct beginning area.
        return when (s.lowercase()) {
            "a" -> b.left + b.width() * .50f to b.bottom - b.height() * .05f
            "b" -> b.left + b.width() * .42f to b.top + b.height() * .04f
            "c" -> b.right - b.width() * .06f to b.top + b.height() * .18f
            "d" -> b.left + b.width() * .42f to b.top + b.height() * .04f
            "e" -> b.left + b.width() * .10f to b.top + b.height() * .42f
            "f" -> b.left + b.width() * .52f to b.top + b.height() * .03f
            "g" -> b.right - b.width() * .05f to b.top + b.height() * .35f
            "h" -> b.left + b.width() * .35f to b.top + b.height() * .05f
            "i" -> b.left + b.width() * .50f to b.top + b.height() * .34f
            "j" -> b.left + b.width() * .55f to b.top + b.height() * .32f
            "k" -> b.left + b.width() * .35f to b.top + b.height() * .05f
            "l" -> b.left + b.width() * .50f to b.top + b.height() * .04f
            "m" -> b.left + b.width() * .08f to b.top + b.height() * .08f
            "n" -> b.left + b.width() * .10f to b.top + b.height() * .08f
            "o" -> b.centerX() to b.top + b.height() * .05f
            "p" -> b.left + b.width() * .35f to b.top + b.height() * .05f
            "q" -> b.centerX() to b.top + b.height() * .05f
            "r" -> b.left + b.width() * .10f to b.top + b.height() * .10f
            "s" -> b.right - b.width() * .08f to b.top + b.height() * .16f
            "t" -> b.left + b.width() * .55f to b.top + b.height() * .02f
            "u" -> b.left + b.width() * .08f to b.top + b.height() * .08f
            "v" -> b.left + b.width() * .08f to b.top + b.height() * .05f
            "w" -> b.left + b.width() * .06f to b.top + b.height() * .05f
            "x" -> b.left + b.width() * .08f to b.top + b.height() * .08f
            "y" -> b.left + b.width() * .08f to b.top + b.height() * .06f
            "z" -> b.left + b.width() * .08f to b.top + b.height() * .08f
            else -> b.centerX() to b.centerY()
        }
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

    private fun drawArrow(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float) {
        canvas.drawLine(x1, y1, x2, y2, guidePaint)
        val angle = kotlin.math.atan2((y2 - y1).toDouble(), (x2 - x1).toDouble())
        val head = 20f
        val a1 = angle + Math.PI * .80
        val a2 = angle - Math.PI * .80
        canvas.drawLine(x2, y2, x2 + kotlin.math.cos(a1).toFloat() * head, y2 + kotlin.math.sin(a1).toFloat() * head, guidePaint)
        canvas.drawLine(x2, y2, x2 + kotlin.math.cos(a2).toFloat() * head, y2 + kotlin.math.sin(a2).toFloat() * head, guidePaint)
    }

    private fun insideGlyph(x: Float, y: Float): Boolean {
        val near = 22f
        val r = RectF(glyphBounds)
        r.inset(-near, -near)
        return r.contains(x, y)
    }

    private fun nearStart(x: Float, y: Float): Boolean = hypot(x - startX, y - startY) <= 48f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (!nearStart(event.x, event.y)) {
                    tracing = false
                    startedCorrectly = false
                    fingerPath.reset()
                    invalidate()
                    return true
                }
                tracing = true
                startedCorrectly = true
                lastTouchX = event.x
                lastTouchY = event.y
                fingerPath.reset()
                fingerPath.moveTo(event.x, event.y)
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> if (tracing) {
                if (insideGlyph(event.x, event.y)) {
                    fingerPath.lineTo(event.x, event.y)
                    lastTouchX = event.x
                    lastTouchY = event.y
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                tracing = false
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
        canvas.drawPath(glyphPath, glyphPaint)

        // Fixed directional hints stay on the letter and never animate.
        val arrowY = glyphBounds.centerY()
        if (isArabic()) {
            drawArrow(canvas, glyphBounds.right + 12f, arrowY, glyphBounds.right - glyphBounds.width() * .18f, arrowY)
            drawArrow(canvas, glyphBounds.right - glyphBounds.width() * .22f, arrowY, glyphBounds.right - glyphBounds.width() * .43f, arrowY)
        } else {
            drawArrow(canvas, glyphBounds.left - 12f, arrowY, glyphBounds.left + glyphBounds.width() * .18f, arrowY)
            drawArrow(canvas, glyphBounds.left + glyphBounds.width() * .22f, arrowY, glyphBounds.left + glyphBounds.width() * .43f, arrowY)
        }

        canvas.drawCircle(startX, startY, 20f, startPaint)
        canvas.drawCircle(startX, startY, 8f, startInnerPaint)
        labelPaint.textSize = min(width.toFloat(), height.toFloat()) * .043f
        canvas.drawText("ابدأ من هنا", startX, (startY - 30f).coerceAtLeast(30f), labelPaint)

        if (!fingerPath.isEmpty) canvas.drawPath(fingerPath, fingerPaint)
    }
}
