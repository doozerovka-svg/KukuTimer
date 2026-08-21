package com.example.kukutimer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.kukutimer.data.AppPreferences
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

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                // Reset all active sessions when screen turns off
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
                checkTopApp()
                delay(1000) // Poll every second
            }
        }
    }

    private suspend fun checkTopApp() {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 60 // Look back 1 minute

        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        var topPackageName: String? = null
        val event = UsageEvents.Event()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                topPackageName = event.packageName
            }
        }

        if (topPackageName != null && topPackageName != packageName) {
            val restrictedApps = appPreferences.restrictedApps.first()
            if (restrictedApps.contains(topPackageName)) {
                handleRestrictedApp(topPackageName)
            }
        }
    }

    private suspend fun handleRestrictedApp(targetPackage: String) {
        val endTime = appPreferences.getTimerEndTime(targetPackage).first()
        val isSessionActive = appPreferences.getSessionActive(targetPackage).first()
        val now = System.currentTimeMillis()
        
        if (isSessionActive) {
            // Already in an active session
            return
        }

        if (endTime == null) {
            // No timer started. Need to start cooking.
            val newEndTime = System.currentTimeMillis() + 10 * 60 * 1000
            appPreferences.setTimerEndTime(targetPackage, newEndTime)
            scheduleReadyAlarm(targetPackage, newEndTime)
            launchTimerActivity(targetPackage)
        } else {
            val diff = endTime - now
            if (diff > 0) {
                // Still cooking
                launchTimerActivity(targetPackage)
            } else if (diff > - (2 * 60 * 1000)) { // within 2 minutes after cooking
                // Ready! Mark session active
                appPreferences.setSessionActive(targetPackage, true)
            } else {
                // Missed the 2 minute window. Reset timer and start cooking again.
                val newEndTime = System.currentTimeMillis() + 10 * 60 * 1000
                appPreferences.setTimerEndTime(targetPackage, newEndTime)
                scheduleReadyAlarm(targetPackage, newEndTime)
                launchTimerActivity(targetPackage)
            }
        }
    }

    private fun scheduleReadyAlarm(targetPackage: String, time: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = Intent(this, Class.forName("com.example.kukutimer.receiver.AlarmReceiver")).apply {
            putExtra("TARGET_PACKAGE", targetPackage)
        }
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            this, targetPackage.hashCode(), intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
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
        val intent = Intent(this, Class.forName("com.example.kukutimer.ui.TimerActivity")).apply {
            putExtra("TARGET_PACKAGE", targetPackage)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "monitor_channel",
            "App Monitoring",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "monitor_channel")
            .setContentTitle("Kuku Timer")
            .setContentText("Monitoring restricted apps...")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenReceiver)
        serviceJob.cancel()
    }
}
