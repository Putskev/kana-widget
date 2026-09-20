package com.example.japan_widget.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.example.japan_widget.data.KanaStateManager

/**
 * Tap-to-advance. Runs entirely inside the widget process via actionRunCallback,
 * so it never launches an Activity - required for the lock-screen widget surface too.
 */
class NextCharacterAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        KanaStateManager(context).advance()
        KanaWidget().update(context, glanceId)
    }
}
