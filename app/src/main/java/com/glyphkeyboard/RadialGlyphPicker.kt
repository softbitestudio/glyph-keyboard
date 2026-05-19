package com.glyphkeyboard

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

/**
 * Fullscreen overlay view that renders a radial glyph menu.
 *
 * Glyphs are placed around a central point. The user drags from the anchor
 * to a wedge; releasing commits the highlighted glyph. Touching inside the
 * dead-zone radius or outside the outer radius cancels.
 */
class RadialGlyphPicker(
    context: Context,
    private val glyphs: List<String>,
    private val anchorX: Float,
    private val anchorY: Float,
    private val onPick: (String) -> Unit,
    private val onDismiss: () -> Unit,
) : View(context) {

    private val innerRadius = dp(48f)
    private val outerRadius = dp(108f)
    private val deadZone   = dp(16f)

    private val count = glyphs.size
    private val sliceAngle = (2 * PI / count).toFloat()

    private val paintBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val paintHighlight = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF5C6BC0.toInt()
        style = Paint.Style.FILL
    }
    private val paintStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF3949AB.toInt()
        style = Paint.Style.STROKE
        strokeWidth = dp(1.5f)
    }
    private val paintGlyph = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = sp(20f)
    }
    private val paintCenter = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xCC1A1A2E.toInt()
        style = Paint.Style.FILL
    }
    private val paintCenterText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = sp(16f)
    }
    private val paintScrim = Paint().apply {
        color = 0x55000000.toInt()
    }

    private var selectedIndex = -1

    // cx/cy clamped so the menu stays on screen; computed in onSizeChanged
    private var cx = anchorX
    private var cy = anchorY

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val margin = outerRadius + dp(8f)
        cx = anchorX.coerceIn(margin, w - margin)
        cy = anchorY.coerceIn(margin, h - margin)
    }

    override fun onDraw(canvas: Canvas) {
        // Dim background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paintScrim)

        val midR = (innerRadius + outerRadius) / 2f

        for (i in glyphs.indices) {
            val startAngle = i * sliceAngle - sliceAngle / 2 - PI.toFloat() / 2
            val endAngle   = startAngle + sliceAngle

            val path = wedgePath(cx, cy, innerRadius, outerRadius, startAngle, endAngle)

            paintBg.color = if (i == selectedIndex) 0xFF5C6BC0.toInt() else 0xCC1A1A2E.toInt()
            canvas.drawPath(path, paintBg)
            canvas.drawPath(path, paintStroke)

            val midAngle = startAngle + sliceAngle / 2
            val gx = cx + midR * cos(midAngle)
            val gy = cy + midR * sin(midAngle)

            val fm = paintGlyph.fontMetrics
            canvas.drawText(glyphs[i], gx, gy - (fm.ascent + fm.descent) / 2, paintGlyph)
        }

        // Center circle
        canvas.drawCircle(cx, cy, innerRadius - dp(2f), paintCenter)
        canvas.drawCircle(cx, cy, innerRadius - dp(2f), paintStroke)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val dx = event.x - cx
        val dy = event.y - cy
        val dist = sqrt(dx * dx + dy * dy)

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                selectedIndex = if (dist in deadZone..outerRadius + dp(24f)) {
                    var angle = atan2(dy, dx) + PI.toFloat() / 2
                    if (angle < 0) angle += 2 * PI.toFloat()
                    ((angle / sliceAngle).toInt() % count + count) % count
                } else {
                    -1
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (selectedIndex in glyphs.indices) {
                    onPick(glyphs[selectedIndex])
                } else {
                    onDismiss()
                }
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                onDismiss()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun wedgePath(
        cx: Float, cy: Float,
        r1: Float, r2: Float,
        startAngle: Float, endAngle: Float,
    ): Path {
        val path = Path()
        val gap = 0.03f // radians between slices
        val a0 = startAngle + gap
        val a1 = endAngle - gap

        path.moveTo(cx + r1 * cos(a0), cy + r1 * sin(a0))
        // inner arc
        path.arcTo(
            cx - r1, cy - r1, cx + r1, cy + r1,
            Math.toDegrees(a0.toDouble()).toFloat(),
            Math.toDegrees((a1 - a0).toDouble()).toFloat(),
            false,
        )
        // line to outer
        path.lineTo(cx + r2 * cos(a1), cy + r2 * sin(a1))
        // outer arc (reverse)
        path.arcTo(
            cx - r2, cy - r2, cx + r2, cy + r2,
            Math.toDegrees(a1.toDouble()).toFloat(),
            Math.toDegrees((a0 - a1).toDouble()).toFloat(),
            false,
        )
        path.close()
        return path
    }

    private fun dp(v: Float) = v * resources.displayMetrics.density
    private fun sp(v: Float) = v * resources.displayMetrics.scaledDensity
}
