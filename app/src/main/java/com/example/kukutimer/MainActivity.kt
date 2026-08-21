package com.example.kukutimer

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kukutimer.data.AppPreferences
import com.example.kukutimer.service.AppMonitorService
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appPreferences = AppPreferences(this)

        if (!hasUsageStatsPermission()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        } else {
            startForegroundService(Intent(this, AppMonitorService::class.java))
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppListScreen(appPreferences, packageManager)
                }
            }
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(), packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}

@Composable
fun AppListScreen(appPreferences: AppPreferences, packageManager: PackageManager) {
    val scope = rememberCoroutineScope()
    val restrictedApps by appPreferences.restrictedApps.collectAsState(initial = emptySet())
    
    val installedApps = remember {
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { packageManager.getLaunchIntentForPackage(it.packageName) != null }
            .sortedBy { packageManager.getApplicationLabel(it).toString() }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Kuku Timer", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Select apps to restrict (10min wait).")
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn {
            items(installedApps) { appInfo ->
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                val isRestricted = restrictedApps.contains(appInfo.packageName)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(appName, modifier = Modifier.weight(1f))
                    Switch(
                        checked = isRestricted,
                        onCheckedChange = { checked ->
                            scope.launch {
                                appPreferences.setAppRestricted(appInfo.packageName, checked)
                            }
                        }
                    )
                }
            }
        }
    }
}
