package com.example.kukutimer.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.kukutimer.MainActivity
import com.example.kukutimer.data.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val targetPackage = intent.getStringExtra("TARGET_PACKAGE") ?: return
        val appPreferences = AppPreferences(context)

        CoroutineScope(Dispatchers.IO).launch {
            val pm = context.packageManager
            val appName = try {
                val info = pm.getApplicationInfo(targetPackage, 0)
                pm.getApplicationLabel(info).toString()
            } catch (e: Exception) {
                targetPackage
            }

            val windowMinutes = appPreferences.windowTimeMinutes.first()

            val notificationIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, "monitor_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("🍙 Рис готов!")
                .setContentText("У вас есть $windowMinutes мин, чтобы войти в $appName.")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(targetPackage.hashCode(), notification)
        }
    }
}
