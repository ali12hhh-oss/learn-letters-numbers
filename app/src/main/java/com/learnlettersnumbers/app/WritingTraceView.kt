package com.learnlettersnumbers.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot
import kotlin.math.min

/**
 * Simple writing guide.
 * - No animated hand/pointer.
 * - A fixed start point is shown on the beginning of each character.
 * - Fixed arrows show the suggested writing direction.
 * - The child writes directly on top of the faint character with a finger.
 */
class WritingTraceView(context: Context) : View(context) {
    private val glyphPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(70, 86, 110)
        alpha = 58
        style = Paint.Style.FILL
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    private val guidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(38, 126, 220)
        alpha = 210
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val startPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(24, 180, 88)
        style = Paint.Style.FILL
    }
    private val startInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
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
    private var startX = 0f
    private var startY = 0f
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
        val start = startPointFor(symbol, glyphBounds)
        startX = start.first
        startY = start.second
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

    /** Start locations are deliberately different for the individual letter shapes. */
    private fun startPointFor(s: String, b: RectF): Pair<Float, Float> {
        val a = arabicBase(s)
        if (a != null) {
            return when (a) {
                "ا" -> b.centerX() to b.top + b.height() * .05f
                "ب", "ت", "ث", "ن", "ي" -> b.right - b.width() * .08f to b.centerY()
                "ج", "ح", "خ" -> b.right - b.width() * .10f to b.top + b.height() * .40f
                "د", "ذ" -> b.right - b.width() * .18f to b.top + b.height() * .12f
                "ر", "ز" -> b.right - b.width() * .16f to b.top + b.height() * .18f
                "س", "ش" -> b.right - b.width() * .08f to b.top + b.height() * .42f
                "ص", "ض" -> b.right - b.width() * .10f to b.top + b.height() * .22f
                "ط", "ظ" -> b.right - b.width() * .16f to b.top + b.height() * .08f
                "ع", "غ" -> b.right - b.width() * .10f to b.top + b.height() * .34f
                "ف", "ق" -> b.right - b.width() * .10f to b.top + b.height() * .30f
                "ك" -> b.right - b.width() * .10f to b.top + b.height() * .10f
                "ل" -> b.centerX() to b.top + b.height() * .04f
                "م" -> b.right - b.width() * .08f to b.top + b.height() * .36f
                "ه" -> b.right - b.width() * .08f to b.top + b.height() * .34f
                "و" -> b.right - b.width() * .12f to b.top + b.height() * .28f
                else -> b.centerX() to b.centerY()
            }
        }

        return when (s.lowercase()) {
            "a" -> b.left + b.width() * .50f to b.bottom - b.height() * .04f
            "b" -> b.left + b.width() * .42f to b.top + b.height() * .04f
            "c" -> b.right - b.width() * .05f to b.top + b.height() * .18f
            "d" -> b.left + b.width() * .42f to b.top + b.height() * .04f
            "e" -> b.left + b.width() * .08f to b.top + b.height() * .42f
            "f" -> b.left + b.width() * .52f to b.top + b.height() * .03f
            "g" -> b.right - b.width() * .05f to b.top + b.height() * .34f
            "h" -> b.left + b.width() * .35f to b.top + b.height() * .05f
            "i" -> b.left + b.width() * .50f to b.top + b.height() * .34f
            "j" -> b.left + b.width() * .55f to b.top + b.height() * .32f
            "k" -> b.left + b.width() * .35f to b.top + b.height() * .05f
            "l" -> b.left + b.width() * .50f to b.top + b.height() * .04f
            "m", "n" -> b.left + b.width() * .08f to b.top + b.height() * .08f
            "o" -> b.centerX() to b.top + b.height() * .05f
            "p" -> b.left + b.width() * .35f to b.top + b.height() * .05f
            "q" -> b.centerX() to b.top + b.height() * .05f
            "r" -> b.left + b.width() * .10f to b.top + b.height() * .10f
            "s" -> b.right - b.width() * .08f to b.top + b.height() * .16f
            "t" -> b.left + b.width() * .55f to b.top + b.height() * .02f
            "u" -> b.left + b.width() * .08f to b.top + b.height() * .08f
            "v", "w", "x", "y", "z" -> b.left + b.width() * .07f to b.top + b.height() * .06f
            else -> b.centerX() to b.centerY()
        }
    }

    private fun nearStart(x: Float, y: Float): Boolean = hypot(x - startX, y - startY) <= 52f

    private fun insideGuide(x: Float, y: Float): Boolean {
        val r = RectF(glyphBounds)
        r.inset(-30f, -30f)
        return r.contains(x, y)
    }

    private fun drawArrow(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float) {
        canvas.drawLine(x1, y1, x2, y2, guidePaint)
        val angle = kotlin.math.atan2((y2 - y1).toDouble(), (x2 - x1).toDouble())
        val head = 18f
        val a1 = angle + Math.PI * .78
        val a2 = angle - Math.PI * .78
        canvas.drawLine(x2, y2, x2 + kotlin.math.cos(a1).toFloat() * head, y2 + kotlin.math.sin(a1).toFloat() * head, guidePaint)
        canvas.drawLine(x2, y2, x2 + kotlin.math.cos(a2).toFloat() * head, y2 + kotlin.math.sin(a2).toFloat() * head, guidePaint)
    }

    /** Fixed arrows: no animation and no moving hand. */
    private fun drawFixedDirection(canvas: Canvas) {
        val a = arabicBase(symbol)
        if (a != null) {
            // Arabic writing generally progresses right-to-left; the arrow stays fixed beside the guide.
            val y = startY
            drawArrow(canvas, startX + 70f, y, startX + 12f, y)
            return
        }
        val y = startY
        drawArrow(canvas, startX - 55f, y, startX - 5f, y)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (!nearStart(event.x, event.y)) return true
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

        // Large, faint character: the child writes directly on this shape with a finger.
        canvas.drawPath(glyphPath, glyphPaint)

        // One fixed start point and fixed directional arrows only.
        drawFixedDirection(canvas)
        canvas.drawCircle(startX, startY, 21f, startPaint)
        canvas.drawCircle(startX, startY, 8f, startInnerPaint)

        // Only the child's own finger stroke is rendered. There is no hand/pointer animation.
        if (!writingPath.isEmpty) canvas.drawPath(writingPath, writingPaint)
    }
}
