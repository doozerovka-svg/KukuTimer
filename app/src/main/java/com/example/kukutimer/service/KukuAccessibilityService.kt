package com.example.kukutimer.service

import android.accessibilityservice.AccessibilityService
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import com.example.kukutimer.data.AppPreferences
import com.example.kukutimer.receiver.AlarmReceiver
import com.example.kukutimer.ui.TimerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class KukuAccessibilityService : AccessibilityService() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var appPreferences: AppPreferences

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

    override fun onServiceConnected() {
        super.onServiceConnected()
        appPreferences = AppPreferences(this)
        try {
            registerReceiver(screenReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val targetPackage = event.packageName?.toString() ?: return
        val className = event.className?.toString() ?: ""

        // If TimerActivity is opening, do not intercept to avoid loops
        if (targetPackage == packageName && className.contains("TimerActivity")) {
            return
        }

        serviceScope.launch {
            try {
                val restricted = appPreferences.restrictedApps.first()
                if (restricted.contains(targetPackage)) {
                    handleRestrictedApp(targetPackage)
                } else if (targetPackage != packageName) {
                    // Reset throttle when user leaves restricted app
                    lastInterceptedPackage = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun handleRestrictedApp(targetPackage: String) {
        val now = System.currentTimeMillis()
        val isSessionActive = appPreferences.getSessionActive(targetPackage).first()

        if (isSessionActive) {
            // User entered during 2-minute window -> unlimited access until screen turns off
            return
        }

        val endTime = appPreferences.getTimerEndTime(targetPackage).first()
        val cooldownMs = 1200L
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
                // Still cooking!
                if (!recentlyIntercepted) {
                    lastInterceptedPackage = targetPackage
                    lastInterceptTime = now
                    launchTimerActivity(targetPackage)
                }
            } else if (diff > -windowDurationMs) {
                // Within opportunity window! Allow access
                appPreferences.setSessionActive(targetPackage, true)
                lastInterceptedPackage = null
            } else {
                // Window expired! Reset timer and start fresh cooking period
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
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
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
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
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

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {}
        serviceJob.cancel()
    }
}
