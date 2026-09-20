package com.example.japan_widget.data

import kotlinx.serialization.Serializable

@Serializable
data class KanaWord(
    val jp: String,
    val romaji: String,
    val de: String
)

@Serializable
data class KanaEntry(
    val char: String,
    val romaji: String,
    val script: String,
    val kind: String,
    val word: KanaWord? = null,
    val rare: Boolean = false,
    val note: String? = null
)

@Serializable
data class KanjiWordEntry(
    val kanji: String,
    val kana: String,
    val romaji: String,
    val de: String,
    val cat: String? = null
)

@Serializable
data class KanjiData(
    val version: Int,
    val language: String,
    val words: List<KanjiWordEntry> = emptyList()
)

@Serializable
data class KanaData(
    val version: Int,
    val language: String,
    val hiragana: List<KanaEntry> = emptyList(),
    val katakana: List<KanaEntry> = emptyList()
)

/** Script pool a card belongs to, used both as a filter and as part of its stable id. */
enum class Script {
    HIRAGANA, KATAKANA, KANJI
}

/**
 * Common display shape for the widget, regardless of whether the underlying
 * entry is kana or kanji. [id] is stable across settings changes so it can be
 * persisted as "currently shown" / used in the no-repeat shuffle queue.
 */
data class WidgetCard(
    val id: String,
    val script: Script,
    val char: String,
    val line1: String,
    val line2Bold: String?,
    val line3: String?,
    val note: String?
)

fun KanaEntry.toWidgetCard(script: Script): WidgetCard {
    val id = "${script.name}:$char"
    return WidgetCard(
        id = id,
        script = script,
        char = char,
        line1 = romaji,
        line2Bold = word?.de,
        line3 = word?.let { "${it.jp}・${it.romaji}" },
        note = note
    )
}

fun KanjiWordEntry.toWidgetCard(): WidgetCard {
    val id = "${Script.KANJI.name}:$kanji"
    return WidgetCard(
        id = id,
        script = Script.KANJI,
        char = kanji,
        line1 = "$romaji・$kana",
        line2Bold = de,
        line3 = null,
        note = null
    )
}
