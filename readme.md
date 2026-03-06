Android Custom Keyboard with Long-Press Glyph Picker
This is a great project! Here's a breakdown of what you'll need to build it.
Architecture Overview
You'll build an Android IME (Input Method Editor) — a system service that Android uses for all keyboards. The core components are:
Component
Purpose
InputMethodService
The base class your keyboard extends
Custom View
Your QWERTY keyboard layout
Long-press Handler
Detects 300ms hold, triggers glyph picker
Popup/Bottom Sheet
Displays your alternate glyphs per letter
Glyph data file
JSON/map storing your symbols per letter
Project Structure


app/                                                            
├── src/main/                                             
│   ├── java/com/yourapp/keyboard/
│   │   ├── GlyphKeyboardService.kt       ← IME entry point
│   │   ├── KeyboardView.kt               ← Custom key rendering
│   │   ├── GlyphPickerPopup.kt           ← Long-press popup
│   │   └── GlyphData.kt                  ← Your symbol mappings                                             
│   ├── res/                                             
│   │   ├── xml/                                             
│   │   │   ├── method.xml                ← IME                declaration
│   │   │   └── keyboard.xml              ← Key layout (optional)                                             
│   │   └── layout/                                                            
│   │       ├── keyboard_view.xml                              
│   │       └── glyph_picker.xml                                             
│   └── AndroidManifest.xml                                             