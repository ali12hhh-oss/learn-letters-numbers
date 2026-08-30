package com.learnlettersnumbers.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import kotlin.math.hypot
import kotlin.math.min

/** Draws the complete glyph and animates a hand over its centerline, not its outline. */
class WritingTraceView(context: Context) : View(context) {
    private data class P(val x: Float, val y: Float)

    private val glyphPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(125, 139, 158); alpha = 82; style = Paint.Style.FILL
        typeface = android.graphics.Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
    }
    private val startPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(39, 174, 96) }
    private val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val handPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(255, 174, 0); textAlign = Paint.Align.CENTER }

    private var symbol = "A"
    private var progress = 0f
    private var lastKey = ""
    private var samples: List<P> = emptyList()
    private var start: P? = null
    private var animationStart = 0L
    private val duration = 6000L

    fun setLesson(newSymbol: String, replay: Int = 0) {
        val key = "$newSymbol|$replay|$width|$height"
        if (key == lastKey) return
        lastKey = key; symbol = newSymbol; progress = 0f
        rebuild(); animationStart = System.currentTimeMillis(); removeCallbacks(frame); post(frame); invalidate()
    }

    private val frame = object : Runnable {
        override fun run() {
            if (samples.isEmpty()) return
            val elapsed = System.currentTimeMillis() - animationStart
            progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
            invalidate()
            if (progress < 1f) postDelayed(this, 16L)
        }
    }

    override fun onDetachedFromWindow() { removeCallbacks(frame); super.onDetachedFromWindow() }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        lastKey = ""; rebuild(); animationStart = System.currentTimeMillis(); post(frame)
    }

    private fun rebuild() {
        if (width < 40 || height < 40) return
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val bitmapCanvas = Canvas(bitmap)
        glyphPaint.textSize = min(width, height).toFloat() * 0.82f
        bitmapCanvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)
        bitmapCanvas.drawText(symbol, width / 2f, height * 0.69f, glyphPaint)
        val mask = BooleanArray(width * height)
        for (y in 0 until height) for (x in 0 until width) if (Color.alpha(bitmap.getPixel(x, y)) > 20) mask[y * width + x] = true
        bitmap.recycle()

        val main = largestComponent(mask, width, height)
        if (main.none { it }) { samples = emptyList(); start = null; return }
        thin(main, width, height)
        val points = mainIndices(main, width, height)
        if (points.size < 3) { samples = emptyList(); start = null; return }
        val ordered = orderSkeleton(points, width, height)
        if (ordered.size < 3) { samples = emptyList(); start = null; return }

        val minX = ordered.minOf { it.x }; val maxX = ordered.maxOf { it.x }
        val minY = ordered.minOf { it.y }; val maxY = ordered.maxOf { it.y }
        val bw = (maxX - minX).coerceAtLeast(1f); val bh = (maxY - minY).coerceAtLeast(1f)
        val scale = min((width * 0.70f) / bw, (height * 0.72f) / bh)
        val tx = width / 2f - (minX + maxX) * scale / 2f
        val ty = height * 0.54f - (minY + maxY) * scale / 2f
        val transformed = ordered.map { P(it.x * scale + tx, it.y * scale + ty) }
        samples = resample(transformed, 900)

        val hint = startHint(symbol)
        val startIndex = samples.indices.minByOrNull { i -> hypot(samples[i].x - hint.first * width, samples[i].y - hint.second * height) } ?: 0
        samples = if (samples.size > 2) samples.drop(startIndex) + samples.take(startIndex) else samples
        start = samples.firstOrNull()
    }

    private fun startHint(s: String): Pair<Float, Float> = when (s.lowercase()) {
        "ا" -> .50f to .18f; "ب", "ت", "ث", "ي" -> .78f to .56f; "ج", "ح", "خ" -> .72f to .34f
        "د", "ذ", "ر", "ز" -> .74f to .30f; "س", "ش" -> .78f to .42f; "ص", "ض" -> .76f to .34f
        "ط", "ظ" -> .62f to .20f; "ع", "غ" -> .75f to .36f; "ف", "ق" -> .76f to .28f
        "ك" -> .70f to .20f; "ل" -> .56f to .18f; "م", "ن", "ه" -> .74f to .38f; "و" -> .70f to .30f
        "a" -> .32f to .24f; "b", "d", "p", "q", "r" -> .58f to .22f; "c", "e", "o", "s" -> .72f to .30f
        "f", "t" -> .55f to .18f; "g" -> .70f to .34f; "h", "k", "l", "m", "n" -> .30f to .20f
        "i", "j" -> .52f to .18f; "u", "v", "w", "x", "y", "z" -> .30f to .22f; else -> .30f to .25f
    }

    private fun largestComponent(mask: BooleanArray, w: Int, h: Int): BooleanArray {
        val visited = BooleanArray(mask.size); var best = IntArray(0); val queue = IntArray(mask.size)
        val dx = intArrayOf(-1,0,1,-1,1,-1,0,1); val dy = intArrayOf(-1,-1,-1,0,0,1,1,1)
        for (startIndex in mask.indices) {
            if (!mask[startIndex] || visited[startIndex]) continue
            var head = 0; var tail = 0; queue[tail++] = startIndex; visited[startIndex] = true
            val component = IntArray(mask.size); var count = 0
            while (head < tail) {
                val p = queue[head++]; component[count++] = p; val x = p % w; val y = p / w
                for (k in 0..7) { val nx=x+dx[k]; val ny=y+dy[k]; if(nx !in 0 until w || ny !in 0 until h) continue; val q=ny*w+nx; if(mask[q]&&!visited[q]){visited[q]=true;queue[tail++]=q} }
            }
            if (count > best.size) best = component.copyOf(count)
        }
        return BooleanArray(mask.size).also { out -> best.forEach { out[it] = true } }
    }

    private fun thin(m: BooleanArray, w: Int, h: Int) {
        fun at(x:Int,y:Int)=x in 0 until w&&y in 0 until h&&m[y*w+x]
        var changed: Boolean
        do {
            changed=false; val remove=ArrayList<Int>()
            for(y in 1 until h-1) for(x in 1 until w-1) if(m[y*w+x]) {
                val p2=at(x,y-1);val p3=at(x+1,y-1);val p4=at(x+1,y);val p5=at(x+1,y+1);val p6=at(x,y+1);val p7=at(x-1,y+1);val p8=at(x-1,y);val p9=at(x-1,y-1)
                val ns=listOf(p2,p3,p4,p5,p6,p7,p8,p9); val b=ns.count{it}; val a=ns.plus(p2).zipWithNext().count{!it.first&&it.second}
                if(b in 2..6&&a==1&&!(p2&&p4&&p6)&&!(p4&&p6&&p8)) remove.add(y*w+x)
            }
            remove.forEach{m[it]=false}; if(remove.isNotEmpty())changed=true; remove.clear()
            for(y in 1 until h-1) for(x in 1 until w-1) if(m[y*w+x]) {
                val p2=at(x,y-1);val p3=at(x+1,y-1);val p4=at(x+1,y);val p5=at(x+1,y+1);val p6=at(x,y+1);val p7=at(x-1,y+1);val p8=at(x-1,y);val p9=at(x-1,y-1)
                val ns=listOf(p2,p3,p4,p5,p6,p7,p8,p9); val b=ns.count{it}; val a=ns.plus(p2).zipWithNext().count{!it.first&&it.second}
                if(b in 2..6&&a==1&&!(p2&&p4&&p8)&&!(p2&&p6&&p8)) remove.add(y*w+x)
            }
            remove.forEach{m[it]=false}; if(remove.isNotEmpty())changed=true
        } while(changed)
    }

    private fun mainIndices(m:BooleanArray,w:Int,h:Int)=buildList<P>{for(y in 1 until h-1)for(x in 1 until w-1)if(m[y*w+x])add(P(x.toFloat(),y.toFloat()))}

    private fun orderSkeleton(points:List<P>,w:Int,h:Int):List<P>{
        val indexByCell=HashMap<Int,Int>(points.size);points.forEachIndexed{i,p->indexByCell[p.y.toInt()*w+p.x.toInt()]=i}
        fun neighbors(i:Int):List<Int>{val p=points[i];val x=p.x.toInt();val y=p.y.toInt();val out=ArrayList<Int>(8);for(dy in -1..1)for(dx in -1..1)if(dx!=0||dy!=0)indexByCell[y*w+x+dy*w+dx]?.let{out.add(it)};return out}
        val graph=Array(points.size){neighbors(it)};val endpoints=points.indices.filter{graph[it].size==1}
        if(endpoints.isEmpty()){
            val path=ArrayList<Int>();var prev=-1;var cur=0;repeat(points.size){path.add(cur);val next=graph[cur].firstOrNull{it!=prev}?:return@repeat;prev=cur;cur=next};return path.map{points[it]}
        }
        fun farthest(source:Int):Pair<Int,IntArray>{val dist=IntArray(points.size){-1};val parent=IntArray(points.size){-1};val q=IntArray(points.size);var head=0;var tail=0;q[tail++]=source;dist[source]=0;while(head<tail){val u=q[head++];graph[u].forEach{v->if(dist[v]<0){dist[v]=dist[u]+1;parent[v]=u;q[tail++]=v}}};val far=dist.indices.maxByOrNull{dist[it]}?:source;return far to parent}
        val a=farthest(endpoints.first()).first;val(b,parent)=farthest(a);val path=ArrayList<Int>();var cur=b;while(cur>=0){path.add(cur);if(cur==a)break;cur=parent[cur]};path.reverse();return path.map{points[it]}
    }

    private fun resample(input:List<P>,count:Int):List<P>{if(input.size<2)return input;val cumulative=FloatArray(input.size);for(i in 1 until input.size)cumulative[i]=cumulative[i-1]+hypot(input[i].x-input[i-1].x,input[i].y-input[i-1].y);val total=cumulative.last().coerceAtLeast(1f);return(0 until count).map{i->val target=total*i/(count-1).coerceAtLeast(1);val hi=cumulative.binarySearch(target).let{if(it>=0)it else-it-1}.coerceIn(1,input.lastIndex);val lo=hi-1;val span=(cumulative[hi]-cumulative[lo]).coerceAtLeast(.001f);val f=(target-cumulative[lo])/span;P(input[lo].x+(input[hi].x-input[lo].x)*f,input[lo].y+(input[hi].y-input[lo].y)*f)}}

    override fun onDraw(canvas:Canvas){super.onDraw(canvas);canvas.drawColor(Color.rgb(242,247,255));glyphPaint.textSize=min(width,height).toFloat()*.82f;canvas.drawText(symbol,width/2f,height*.69f,glyphPaint);val s=start?:return;canvas.drawCircle(s.x,s.y,15f,startPaint);canvas.drawCircle(s.x,s.y,7f,whitePaint);canvas.drawCircle(s.x,s.y,4f,startPaint);if(samples.isEmpty())return;val pos=progress*(samples.size-1);val i=pos.toInt().coerceIn(0,samples.lastIndex);val j=(i+1).coerceAtMost(samples.lastIndex);val f=pos-i;val x=samples[i].x+(samples[j].x-samples[i].x)*f;val y=samples[i].y+(samples[j].y-samples[i].y)*f;val tx=samples[j].x-samples[i].x;val ty=samples[j].y-samples[i].y;val angle=Math.toDegrees(kotlin.math.atan2(ty.toDouble(),tx.toDouble())).toFloat();handPaint.textSize=min(width,height).toFloat()*.13f;canvas.save();canvas.rotate(angle,x,y);canvas.drawText("☝",x,y-(handPaint.ascent()+handPaint.descent())/2f,handPaint);canvas.restore()}
}
