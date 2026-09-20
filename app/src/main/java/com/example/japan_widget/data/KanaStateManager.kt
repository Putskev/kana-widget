package com.example.japan_widget.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

/**
 * Tracks which card is currently shown and, for shuffle mode, the remaining
 * draw queue, so a character never repeats before every card in the pool has
 * had a turn. Persisted in DataStore so it survives process death / reboot.
 */
class KanaStateManager(private val context: Context) {

    private object Keys {
        val CURRENT_ID = stringPreferencesKey("current_id")
        val QUEUE = stringPreferencesKey("shuffle_queue")
    }

    private val settingsRepository = SettingsRepository(context)

    /** Returns the currently displayed card, picking an initial one if none exists yet. */
    suspend fun current(): WidgetCard? {
        val settings = settingsRepository.current()
        val data = KanaRepository.loadData(context)
        val kanjiWords = KanjiRepository.loadData(context).words
        val pool = KanaRepository.buildPool(data, kanjiWords, settings.mode, settings.includeDakuten, settings.includeRare)
        if (pool.isEmpty()) return null

        val prefs = context.kanaDataStore.data.first()
        val currentId = prefs[Keys.CURRENT_ID]
        val existing = pool.firstOrNull { it.id == currentId }
        if (existing != null) return existing

        // No valid current card yet (first run, or filters changed it out) -> pick a starting card.
        return advanceWithin(pool, settings.order, currentId = null)
    }

    /** Moves to the next card according to the current order setting and persists it. */
    suspend fun advance(): WidgetCard? {
        val settings = settingsRepository.current()
        val data = KanaRepository.loadData(context)
        val kanjiWords = KanjiRepository.loadData(context).words
        val pool = KanaRepository.buildPool(data, kanjiWords, settings.mode, settings.includeDakuten, settings.includeRare)
        if (pool.isEmpty()) return null

        val prefs = context.kanaDataStore.data.first()
        val currentId = prefs[Keys.CURRENT_ID]
        return advanceWithin(pool, settings.order, currentId)
    }

    private suspend fun advanceWithin(pool: List<WidgetCard>, order: Order, currentId: String?): WidgetCard {
        val next = when (order) {
            Order.SEQUENTIAL -> {
                val currentIndex = pool.indexOfFirst { it.id == currentId }
                val nextIndex = if (currentIndex == -1) 0 else (currentIndex + 1) % pool.size
                pool[nextIndex]
            }
            Order.SHUFFLED -> nextFromShuffleQueue(pool, currentId)
        }
        context.kanaDataStore.edit { it[Keys.CURRENT_ID] = next.id }
        return next
    }

    private suspend fun nextFromShuffleQueue(pool: List<WidgetCard>, currentId: String?): WidgetCard {
        val poolIds = pool.map { it.id }.toSet()
        val prefs = context.kanaDataStore.data.first()
        var queue = prefs[Keys.QUEUE]
            ?.split(",")
            ?.filter { it.isNotBlank() && it in poolIds }
            ?.toMutableList()
            ?: mutableListOf()

        if (queue.isEmpty()) {
            queue = pool.map { it.id }.shuffled().toMutableList()
            // Avoid immediately repeating the card just shown, if possible.
            if (queue.size > 1 && queue.first() == currentId) {
                val swapIndex = 1
                val tmp = queue[0]
                queue[0] = queue[swapIndex]
                queue[swapIndex] = tmp
            }
        }

        val nextId = queue.removeAt(0)
        context.kanaDataStore.edit { it[Keys.QUEUE] = queue.joinToString(",") }
        return pool.first { it.id == nextId }
    }
}
