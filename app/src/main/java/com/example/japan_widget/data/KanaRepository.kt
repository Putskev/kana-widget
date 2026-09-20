package com.example.japan_widget.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/** Loads and caches assets/kana.json, and builds the filtered pool of cards for the widget. */
object KanaRepository {

    private const val ASSET_FILE = "kana.json"

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Volatile
    private var cached: KanaData? = null

    suspend fun loadData(context: Context): KanaData {
        cached?.let { return it }
        return withContext(Dispatchers.IO) {
            cached ?: run {
                val text = context.assets.open(ASSET_FILE).bufferedReader().use { it.readText() }
                val data = json.decodeFromString(KanaData.serializer(), text)
                cached = data
                data
            }
        }
    }

    /**
     * Builds the ordered, filtered list of cards for the given settings.
     * Order follows the JSON order (basic before dakuten/handakuten), hiragana before katakana.
     */
    fun buildPool(
        data: KanaData,
        kanjiWords: List<KanjiWordEntry>,
        mode: Mode,
        includeDakuten: Boolean,
        includeRare: Boolean
    ): List<WidgetCard> {
        fun kanaAllowed(entry: KanaEntry): Boolean {
            if (!includeDakuten && entry.kind != "basic") return false
            if (!includeRare && entry.rare) return false
            return true
        }

        val hiragana = if (mode == Mode.HIRAGANA || mode == Mode.BOTH) {
            data.hiragana.filter(::kanaAllowed).map { it.toWidgetCard(Script.HIRAGANA) }
        } else emptyList()

        val katakana = if (mode == Mode.KATAKANA || mode == Mode.BOTH) {
            data.katakana.filter(::kanaAllowed).map { it.toWidgetCard(Script.KATAKANA) }
        } else emptyList()

        val kanji = if (mode == Mode.KANJI) {
            kanjiWords.map { it.toWidgetCard() }
        } else emptyList()

        return hiragana + katakana + kanji
    }
}

/** Loads and caches assets/kanji.json. */
object KanjiRepository {

    private const val ASSET_FILE = "kanji.json"

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Volatile
    private var cached: KanjiData? = null

    suspend fun loadData(context: Context): KanjiData {
        cached?.let { return it }
        return withContext(Dispatchers.IO) {
            cached ?: run {
                val text = context.assets.open(ASSET_FILE).bufferedReader().use { it.readText() }
                val data = json.decodeFromString(KanjiData.serializer(), text)
                cached = data
                data
            }
        }
    }
}

enum class Mode {
    HIRAGANA, KATAKANA, BOTH, KANJI
}

enum class Order {
    SEQUENTIAL, SHUFFLED
}
