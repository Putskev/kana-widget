package com.example.japan_widget.work

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.japan_widget.data.KanaStateManager
import com.example.japan_widget.widget.KanaWidget

/** Advances to the next character and refreshes every placed widget instance. */
class KanaUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        KanaStateManager(applicationContext).advance()
        KanaWidget().updateAll(applicationContext)
        return Result.success()
    }
}
