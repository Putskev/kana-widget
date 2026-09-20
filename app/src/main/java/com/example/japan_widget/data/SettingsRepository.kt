package com.example.japan_widget.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.kanaDataStore by preferencesDataStore(name = "kana_prefs")

data class KanaSettings(
    val mode: Mode = Mode.BOTH,
    val order: Order = Order.SEQUENTIAL,
    val intervalHours: Int = 2,
    val includeDakuten: Boolean = true,
    val includeRare: Boolean = false
)

/** Reads and writes user-configurable settings (Einstellungen-Activity). */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val MODE = stringPreferencesKey("mode")
        val ORDER = stringPreferencesKey("order")
        val INTERVAL_HOURS = intPreferencesKey("interval_hours")
        val INCLUDE_DAKUTEN = booleanPreferencesKey("include_dakuten")
        val INCLUDE_RARE = booleanPreferencesKey("include_rare")
    }

    val settingsFlow: Flow<KanaSettings> = context.kanaDataStore.data.map { prefs ->
        KanaSettings(
            mode = prefs[Keys.MODE]?.let { runCatching { Mode.valueOf(it) }.getOrNull() } ?: Mode.BOTH,
            order = prefs[Keys.ORDER]?.let { runCatching { Order.valueOf(it) }.getOrNull() } ?: Order.SEQUENTIAL,
            intervalHours = prefs[Keys.INTERVAL_HOURS] ?: 2,
            includeDakuten = prefs[Keys.INCLUDE_DAKUTEN] ?: true,
            includeRare = prefs[Keys.INCLUDE_RARE] ?: false
        )
    }

    suspend fun current(): KanaSettings = settingsFlow.first()

    suspend fun setMode(mode: Mode) {
        context.kanaDataStore.edit { it[Keys.MODE] = mode.name }
    }

    suspend fun setOrder(order: Order) {
        context.kanaDataStore.edit { it[Keys.ORDER] = order.name }
    }

    suspend fun setIntervalHours(hours: Int) {
        context.kanaDataStore.edit { it[Keys.INTERVAL_HOURS] = hours }
    }

    suspend fun setIncludeDakuten(enabled: Boolean) {
        context.kanaDataStore.edit { it[Keys.INCLUDE_DAKUTEN] = enabled }
    }

    suspend fun setIncludeRare(enabled: Boolean) {
        context.kanaDataStore.edit { it[Keys.INCLUDE_RARE] = enabled }
    }
}
