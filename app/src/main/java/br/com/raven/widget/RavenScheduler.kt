package br.com.raven.widget

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object RavenScheduler {
    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<RavenWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "raven_poll", ExistingPeriodicWorkPolicy.UPDATE, request
        )
    }

    fun runNow(context: Context) {
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<RavenWorker>().build()
        )
    }
}
