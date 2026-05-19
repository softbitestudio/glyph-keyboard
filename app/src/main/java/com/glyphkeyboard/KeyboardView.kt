package com.glyphkeyboard

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView

class KeyboardView(
    context: Context,
    private val onKey: (KeyModel) -> Unit,
    private val onLongPress: (KeyModel.Letter, Float, Float) -> Unit,
) : LinearLayout(context) {

    var isShifted = false
        set(value) {
            field = value
            refreshLetterLabels()
        }

    private val handler = Handler(Looper.getMainLooper())
    private val keyViews = mutableMapOf<KeyModel, TextView>()

    init {
        orientation = VERTICAL
        setBackgroundColor(0xFF1A1A2E.toInt())
        buildRows()
    }

    private fun buildRows() {
        removeAllViews()
        keyViews.clear()

        for (row in QWERTY_ROWS) {
            val rowView = LinearLayout(context).apply {
                orientation = HORIZONTAL
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT,
                )
            }

            for (model in row) {
                val tv = makeKeyView(model)
                keyViews[model] = tv
                rowView.addView(tv)
            }
            addView(rowView)
        }
    }

    private fun makeKeyView(model: KeyModel): TextView {
        val label = labelFor(model)
        val hasGlyphs = model is KeyModel.Letter &&
                GLYPH_MAP[model.char]?.isNotEmpty() == true

        return TextView(context).apply {
            text = label
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.key_background)
            typeface = Typeface.DEFAULT_BOLD

            val weight = weightFor(model)
            layoutParams = LinearLayout.LayoutParams(0, dp(52)).apply {
                this.weight = weight
                setMargins(dp(2), dp(3), dp(2), dp(3))
            }

            // Glyph indicator dot
            if (hasGlyphs) {
                setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                // Draw indicator via foreground
                foreground = context.getDrawable(R.drawable.glyph_indicator_dot)
            }

            var longPressTriggered = false
            val longPressRunnable = Runnable {
                longPressTriggered = true
                if (model is KeyModel.Letter) {
                    val loc = IntArray(2)
                    getLocationInWindow(loc)
                    val cx = loc[0] + width / 2f
                    val cy = loc[1].toFloat()
                    onLongPress(model, cx, cy)
                }
            }

            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        longPressTriggered = false
                        if (model is KeyModel.Letter) {
                            handler.postDelayed(longPressRunnable, 300)
                        }
                        v.isPressed = true
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        handler.removeCallbacks(longPressRunnable)
                        v.isPressed = false
                        if (!longPressTriggered) {
                            onKey(model)
                        }
                        true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        handler.removeCallbacks(longPressRunnable)
                        v.isPressed = false
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun refreshLetterLabels() {
        for ((model, tv) in keyViews) {
            if (model is KeyModel.Letter) {
                tv.text = if (isShifted) model.char.uppercaseChar().toString()
                           else model.char.toString()
            }
        }
    }

    private fun labelFor(model: KeyModel) = when (model) {
        is KeyModel.Letter   -> model.char.toString()
        is KeyModel.Space    -> "SPACE"
        is KeyModel.Backspace -> "⌫"
        is KeyModel.Enter    -> "↵"
        is KeyModel.Shift    -> "⇧"
        is KeyModel.SymbolToggle -> "?123"
    }

    private fun weightFor(model: KeyModel) = when (model) {
        is KeyModel.Space    -> 4f
        is KeyModel.Backspace,
        is KeyModel.Shift    -> 1.5f
        is KeyModel.Enter    -> 1.5f
        else                 -> 1f
    }

    private fun dp(v: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics
    ).toInt()
}
