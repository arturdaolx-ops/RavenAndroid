package br.com.raven.widget

import android.app.*
import android.content.Context
import android.media.AudioAttributes
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

class RavenWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("raven", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "")?.trim().orEmpty()
        if (token.isBlank()) return Result.success()

        try {
            val url = URL("https://ravenbot.com.br/api/widget/data?token=$token")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            conn.requestMethod = "GET"
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            val root = JSONObject(text)
            if (!root.optBoolean("success", false)) return Result.success()

            val data = root.getJSONObject("data")
            val today = data.getJSONObject("today")
            val payments = today.optInt("payments", 0)
            val revenue = today.optDouble("revenue", 0.0)

            val last = prefs.getInt("lastPayments", payments)
            if (payments > last && prefs.getBoolean("notifications", true)) {
                showNotification(revenue, payments)
            }
            prefs.edit().putInt("lastPayments", payments).apply()
            prefs.edit().putString("lastJson", text).apply()
            return Result.success()
        } catch (_: Exception) {
            return Result.retry()
        }
    }

    private fun showNotification(revenue: Double, payments: Int) {
        val channelId = "raven_sales"
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= 26) {
            val prefs = applicationContext.getSharedPreferences("raven", Context.MODE_PRIVATE)
            val sound = prefs.getBoolean("sound", true)
            val vibration = prefs.getBoolean("vibration", true)
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            val channel = NotificationChannel(
                channelId, "Vendas Raven",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.setSound(
                if (sound) android.provider.Settings.System.DEFAULT_NOTIFICATION_URI else null,
                attrs
            )
            channel.enableVibration(vibration)
            nm.createNotificationChannel(channel)
        }

        val showValue = applicationContext
            .getSharedPreferences("raven", Context.MODE_PRIVATE)
            .getBoolean("showValue", true)

        val amount = String.format(java.util.Locale("pt", "BR"), "R$ %.2f", revenue)
        val text = if (showValue) "Venda aprovada • $amount" else "Nova venda aprovada"

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🟢 NOVA VENDA — RAVEN")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$text\nTotal hoje: $payments vendas"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        nm.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
    }
}
