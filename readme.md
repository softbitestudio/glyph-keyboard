#ʘʀɪsʏɴ ‖ Glyph Keyboard 🔤

The keyboard with a thousand tongues.

An Android custom keyboard (IME) with a QWERTY layout that lets users long-press any letter to pick from a curated collection of symbols and glyphs from languages around the world.

---

## Features

- ✅ Standard QWERTY layout
- ✅ 300ms long-press triggers a glyph picker popup
- ✅ 26 letters × up to 12 glyphs each (fully customizable)
- ✅ Small red dot indicator on keys that have glyphs
- ✅ Shift (single-shot) and Caps Lock (double-tap shift)
- ✅ Respects host app's IME action (Search, Done, Go, etc.)
- ✅ Unicode-safe — works with Greek, Latin extended, IPA, math symbols, and more
- ✅ Optional custom font support (e.g. Noto Sans for maximum Unicode coverage)

---

## Project Structure

```
app/src/main/
├── java/com/glyphkeyboard/
│   ├── GlyphKeyboardService.kt   ← IME entry point (InputMethodService)
│   ├── KeyboardView.kt           ← Custom QWERTY keyboard view + long-press logic
│   ├── GlyphPickerPopup.kt       ← Horizontal popup shown on long press
│   ├── GlyphData.kt              ← ⭐ Your glyph collection lives here
│   └── KeyModel.kt               ← Data models + QWERTY row definitions
├── res/
│   ├── xml/method.xml            ← IME registration metadata
│   ├── layout/
│   │   ├── keyboard_view.xml     ← Keyboard root layout (4 rows)
│   │   ├── key_view.xml          ← Individual key layout
│   │   ├── glyph_picker_popup.xml← Popup container
│   │   └── glyph_item.xml        ← Single glyph button in popup
│   ├── values/
│   │   ├── colors.xml            ← All color tokens
│   │   ├── dimens.xml            ← Sizes, spacing, font sizes
│   │   └── strings.xml
│   └── drawable/
│       ├── popup_background.xml
│       ├── glyph_indicator_dot.xml
│       ├── glyph_item_background.xml
│       └── key_background.xml
└── AndroidManifest.xml
```

---

## How to Build & Install

1. **Open in Android Studio** — Open the `GlyphKeyboard` folder as an existing project.
2. **Sync Gradle** — Android Studio will prompt you; click *Sync Now*.
3. **Run on device or emulator** — Press ▶ or use `./gradlew installDebug`.
4. **Enable the keyboard on your device:**
   - Settings → General Management → Keyboard → On-Screen Keyboard → Manage Keyboards
   - Toggle **Glyph Keyboard** on
   - In any text field, tap the keyboard icon (bottom-right of nav bar) → select *Glyph Keyboard*

---

## Adding Your Own Glyphs

Open `GlyphData.kt`. Each letter maps to a `List<String>`:

```kotlin
'a' to listOf(
    "α",   // Greek alpha
    "å",   // Nordic
    "ä",   // Germanic umlaut
    // ← add your own glyphs here
),
```

Tips:
- Each string can be **any Unicode character or sequence** (including emoji, ligatures, composed characters, etc.)
- The popup scrolls horizontally, so you can add as many as you like
- The **first glyph** in the list appears in the popup header label
- Use a Unicode chart (e.g. https://symbl.cc) to find codepoints

---

## Using a Custom Font

For best rendering of rare Unicode glyphs, bundle the **Noto Sans** font family:

1. Download `NotoSans-Regular.ttf` from [Google Fonts](https://fonts.google.com/noto/specimen/Noto+Sans)
2. Place it in `app/src/main/assets/fonts/`
3. Uncomment these lines in `GlyphKeyboardService.kt`:

```kotlin
val tf = Typeface.createFromAsset(assets, "fonts/NotoSans-Regular.ttf")
setGlyphTypeface(tf)
```

---

## Theming

Edit `res/values/colors.xml` to retheme the keyboard:

| Color token | What it affects |
|---|---|
| `keyboard_bg` | Overall keyboard background |
| `key_bg` / `key_bg_pressed` | Key normal / pressed states |
| `key_text` | Letter color |
| `glyph_indicator` | The small dot on keys with glyphs |
| `popup_bg` | Popup background |
| `popup_item_bg_pressed` | Popup glyph highlight on tap |

---

## Extending: Symbol Layer

`KeyModel.SymbolToggle` is wired up but the symbol layer isn't implemented yet.
To add it:

1. Define a `SYMBOL_ROWS` layout in `KeyModel.kt` (digits, punctuation, etc.)
2. In `KeyboardView.kt`, track a `currentLayer: Layer` (QWERTY / SYMBOL)
3. In `createKeyView` for `SymbolToggle`, swap the rows

---

## Architecture Notes

- **No deprecated `KeyboardView`** — Android's built-in `KeyboardView` was deprecated in API 29. This project uses a fully custom `LinearLayout`-based layout, giving complete control over rendering.
- **Long-press** uses a `Handler.postDelayed(300ms)` pattern. `ACTION_UP` before 300ms = normal tap; after 300ms = long-press (popup shown, tap is consumed).
- **Popup** uses `PopupWindow` with `isFocusable = false` so the IME window stays active.
- **No external dependencies** beyond AndroidX CardView.

---

## Minimum Requirements

| | |
|---|---|
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 34 (Android 14) |
| Language | Kotlin |
| Build system | Gradle 8.x |
