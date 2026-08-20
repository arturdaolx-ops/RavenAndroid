package br.com.raven.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import org.json.JSONObject

class RavenWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val prefs = context.getSharedPreferences("raven", Context.MODE_PRIVATE)
        val json = prefs.getString("lastJson", null)

        for (id in ids) {
            val views = RemoteViews(context.packageName, android.R.layout.simple_list_item_2)
            views.setTextViewText(android.R.id.text1, "● RAVEN     AO VIVO")

            if (json != null) {
                try {
                    val data = JSONObject(json).getJSONObject("data")
                    val today = data.getJSONObject("today")
                    val revenue = today.optDouble("revenue", 0.0)
                    val payments = today.optInt("payments", 0)
                    val text = String.format(java.util.Locale("pt", "BR"),
                        "VENDAS HOJE\nR$ %.2f • %d vendas", revenue, payments)
                    views.setTextViewText(android.R.id.text2, text)
                } catch (_: Exception) {
                    views.setTextViewText(android.R.id.text2, "Token/API aguardando dados")
                }
            } else {
                views.setTextViewText(android.R.id.text2, "Abra o Raven e salve seu token")
            }
            manager.updateAppWidget(id, views)
        }
    }
}
