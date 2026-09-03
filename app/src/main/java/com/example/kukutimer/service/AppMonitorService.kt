package com.example.kukutimer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.kukutimer.MainActivity
import com.example.kukutimer.data.AppPreferences
import com.example.kukutimer.ui.TimerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppMonitorService : Service() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var appPreferences: AppPreferences
    private lateinit var usageStatsManager: UsageStatsManager

    private var lastInterceptedPackage: String? = null
    private var lastInterceptTime: Long = 0L

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                serviceScope.launch {
                    appPreferences.resetAllSessions()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        appPreferences = AppPreferences(this)
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        createNotificationChannel()

        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1, notification)

        startMonitoring()

        return START_STICKY
    }

    private fun startMonitoring() {
        serviceScope.launch {
            while (true) {
                try {
                    checkTopApp()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(350) // Fast and responsive polling
            }
        }
    }

    private suspend fun checkTopApp() {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 15 // Look back 15 seconds

        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        var topPackageName: String? = null
        val event = UsageEvents.Event()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                topPackageName = event.packageName
            }
        }

        // Fallback using queryUsageStats if no event detected
        if (topPackageName == null) {
            val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
            val mostRecent = stats?.filter { it.lastTimeUsed > 0 }?.maxByOrNull { it.lastTimeUsed }
            topPackageName = mostRecent?.packageName
        }

        if (topPackageName != null) {
            if (topPackageName == packageName && TimerActivity.isTop) {
                return
            }
            val restrictedApps = appPreferences.restrictedApps.first()
            if (restrictedApps.contains(topPackageName)) {
                handleRestrictedApp(topPackageName)
            } else {
                // If user switched to an unrestricted app, clear last intercepted cache
                lastInterceptedPackage = null
            }
        }
    }

    private suspend fun handleRestrictedApp(targetPackage: String) {
        val endTime = appPreferences.getTimerEndTime(targetPackage).first()
        val isSessionActive = appPreferences.getSessionActive(targetPackage).first()
        val now = System.currentTimeMillis()

        if (isSessionActive) {
            // Already in an active session (user entered during 2-minute window)
            return
        }

        val cooldownMs = 1500L
        val recentlyIntercepted = (lastInterceptedPackage == targetPackage && (now - lastInterceptTime) < cooldownMs)

        val cookingMinutes = appPreferences.cookingTimeMinutes.first()
        val windowMinutes = appPreferences.windowTimeMinutes.first()
        val cookingDurationMs = cookingMinutes * 60 * 1000L
        val windowDurationMs = windowMinutes * 60 * 1000L

        if (endTime == null) {
            // Start cooking timer
            val newEndTime = now + cookingDurationMs
            appPreferences.setTimerEndTime(targetPackage, newEndTime)
            scheduleReadyAlarm(targetPackage, newEndTime)
            if (!recentlyIntercepted) {
                lastInterceptedPackage = targetPackage
                lastInterceptTime = now
                launchTimerActivity(targetPackage)
            }
        } else {
            val diff = endTime - now
            if (diff > 0) {
                // Still cooking
                if (!recentlyIntercepted) {
                    lastInterceptedPackage = targetPackage
                    lastInterceptTime = now
                    launchTimerActivity(targetPackage)
                }
            } else if (diff > -windowDurationMs) {
                // Within opportunity window! Grant session access
                appPreferences.setSessionActive(targetPackage, true)
                lastInterceptedPackage = null
            } else {
                // Window missed! Reset timer and start fresh cooking period
                val newEndTime = now + cookingDurationMs
                appPreferences.setTimerEndTime(targetPackage, newEndTime)
                scheduleReadyAlarm(targetPackage, newEndTime)
                if (!recentlyIntercepted) {
                    lastInterceptedPackage = targetPackage
                    lastInterceptTime = now
                    launchTimerActivity(targetPackage)
                }
            }
        }
    }

    private fun scheduleReadyAlarm(targetPackage: String, time: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = Intent(this, com.example.kukutimer.receiver.AlarmReceiver::class.java).apply {
            putExtra("TARGET_PACKAGE", targetPackage)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            targetPackage.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, time, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, time, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, time, pendingIntent)
        }
    }

    private fun launchTimerActivity(targetPackage: String) {
        val intent = Intent(this, TimerActivity::class.java).apply {
            putExtra("TARGET_PACKAGE", targetPackage)
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_NO_ANIMATION
            )
        }
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "monitor_channel",
                "Мониторинг ограничений Kuku Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Постоянное уведомление активной службы защиты"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "monitor_channel")
            .setContentTitle("Kuku Timer работает")
            .setContentText("Фоновый контроль ограниченных приложений активен")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            // Ignore if already unregistered
        }
        serviceJob.cancel()
    }
}
