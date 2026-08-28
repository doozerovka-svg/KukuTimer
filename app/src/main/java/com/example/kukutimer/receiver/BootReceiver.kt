package com.example.kukutimer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.kukutimer.data.AppPreferences
import com.example.kukutimer.service.AppMonitorService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val appPreferences = AppPreferences(context)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val restricted = appPreferences.restrictedApps.first()
                    val onboardingDone = appPreferences.isOnboardingCompleted.first()
                    if (onboardingDone && restricted.isNotEmpty()) {
                        val serviceIntent = Intent(context, AppMonitorService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(serviceIntent)
                        } else {
                            context.startService(serviceIntent)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

