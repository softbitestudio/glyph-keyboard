package com.glyphkeyboard

sealed class KeyModel {
    data class Letter(val char: Char) : KeyModel()
    object Space : KeyModel()
    object Backspace : KeyModel()
    object Enter : KeyModel()
    object Shift : KeyModel()
    object SymbolToggle : KeyModel()
}

val QWERTY_ROWS: List<List<KeyModel>> = listOf(
    listOf('q','w','e','r','t','y','u','i','o','p').map { KeyModel.Letter(it) },
    listOf('a','s','d','f','g','h','j','k','l').map { KeyModel.Letter(it) },
    listOf<KeyModel>(KeyModel.Shift) +
        listOf('z','x','c','v','b','n','m').map { KeyModel.Letter(it) } +
        listOf(KeyModel.Backspace),
    listOf(KeyModel.SymbolToggle, KeyModel.Space, KeyModel.Enter)
)
