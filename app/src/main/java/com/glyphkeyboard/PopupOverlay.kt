package com.glyphkeyboard

import android.graphics.PixelFormat
import android.inputmethodservice.InputMethodService
import android.view.*

/**
 * Wraps RadialGlyphPicker in a full-screen PopupWindow so it can float above
 * the IME window without stealing input focus from it.
 */
class PopupOverlay(
    private val service: InputMethodService,
    glyphs: List<String>,
    anchorX: Float,
    anchorY: Float,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    private val popup: PopupWindow

    init {
        val picker = RadialGlyphPicker(
            context   = service,
            glyphs    = glyphs,
            anchorX   = anchorX,
            anchorY   = anchorY,
            onPick    = onPick,
            onDismiss = onDismiss,
        )

        popup = PopupWindow(service).apply {
            contentView = picker
            width       = WindowManager.LayoutParams.MATCH_PARENT
            height      = WindowManager.LayoutParams.MATCH_PARENT
            isFocusable = false
            isTouchable = true
            isOutsideTouchable = false
            setBackgroundDrawable(null)
        }
    }

    fun show() {
        val anchor = service.window?.window?.decorView ?: return
        popup.showAtLocation(anchor, Gravity.NO_GRAVITY, 0, 0)
    }

    fun dismiss() {
        if (popup.isShowing) popup.dismiss()
    }
}
