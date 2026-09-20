package com.example.japan_widget.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.example.japan_widget.data.SettingsRepository
import com.example.japan_widget.work.WidgetScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KanaWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = KanaWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // First widget instance was placed: make sure the periodic switch is scheduled
        // using whatever interval is currently configured (defaults to 2h).
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = SettingsRepository(context).current()
                WidgetScheduler.schedule(context, settings.intervalHours)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
