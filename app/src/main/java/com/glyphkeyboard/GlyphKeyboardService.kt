package com.glyphkeyboard

import android.inputmethodservice.InputMethodService
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout

class GlyphKeyboardService : InputMethodService() {

    private lateinit var keyboardView: KeyboardView
    private var overlayWindow: PopupOverlay? = null

    private var shiftState = ShiftState.OFF

    enum class ShiftState { OFF, ONCE, LOCKED }

    override fun onCreateInputView(): View {
        keyboardView = KeyboardView(
            context = this,
            onKey = ::handleKey,
            onLongPress = ::showRadialPicker,
        )
        return keyboardView
    }

    private fun handleKey(model: KeyModel) {
        val ic = currentInputConnection ?: return
        when (model) {
            is KeyModel.Letter -> {
                val ch = if (shiftState != ShiftState.OFF)
                    model.char.uppercaseChar() else model.char
                ic.commitText(ch.toString(), 1)
                if (shiftState == ShiftState.ONCE) {
                    shiftState = ShiftState.OFF
                    keyboardView.isShifted = false
                }
            }
            is KeyModel.Backspace -> ic.deleteSurroundingText(1, 0)
            is KeyModel.Space     -> ic.commitText(" ", 1)
            is KeyModel.Enter     -> {
                val action = currentInputEditorInfo?.imeOptions
                    ?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
                if (action != EditorInfo.IME_ACTION_NONE &&
                    action != EditorInfo.IME_ACTION_UNSPECIFIED) {
                    ic.performEditorAction(action)
                } else {
                    ic.commitText("\n", 1)
                }
            }
            is KeyModel.Shift -> {
                shiftState = when (shiftState) {
                    ShiftState.OFF   -> ShiftState.ONCE
                    ShiftState.ONCE  -> ShiftState.LOCKED
                    ShiftState.LOCKED -> ShiftState.OFF
                }
                keyboardView.isShifted = shiftState != ShiftState.OFF
            }
            is KeyModel.SymbolToggle -> { /* TODO */ }
        }
    }

    private fun showRadialPicker(model: KeyModel.Letter, anchorX: Float, anchorY: Float) {
        val glyphs = GLYPH_MAP[model.char] ?: return
        if (glyphs.isEmpty()) return

        dismissRadialPicker()

        overlayWindow = PopupOverlay(
            service = this,
            glyphs = glyphs,
            anchorX = anchorX,
            anchorY = anchorY,
            onPick = { glyph ->
                currentInputConnection?.commitText(glyph, 1)
                dismissRadialPicker()
            },
            onDismiss = { dismissRadialPicker() },
        )
        overlayWindow?.show()
    }

    private fun dismissRadialPicker() {
        overlayWindow?.dismiss()
        overlayWindow = null
    }

    override fun onFinishInput() {
        super.onFinishInput()
        dismissRadialPicker()
    }
}
