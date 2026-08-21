package com.example.kukutimer.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kukutimer.data.AppPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerActivity : ComponentActivity() {
    private lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appPreferences = AppPreferences(this)
        
        val targetPackage = intent.getStringExtra("TARGET_PACKAGE") ?: return finish()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TimerScreen(targetPackage, appPreferences, packageManager) {
                        // User decided to leave or timer finished
                        goHome()
                    }
                }
            }
        }
    }

    private fun goHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }
}

@Composable
fun TimerScreen(
    targetPackage: String,
    appPreferences: AppPreferences,
    packageManager: PackageManager,
    onExit: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var endTime by remember { mutableStateOf<Long?>(null) }
    var remainingSeconds by remember { mutableStateOf(0L) }
    
    val appName = remember {
        try {
            val info = packageManager.getApplicationInfo(targetPackage, 0)
            packageManager.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            targetPackage
        }
    }

    LaunchedEffect(targetPackage) {
        appPreferences.getTimerEndTime(targetPackage).collect { savedEndTime ->
            endTime = savedEndTime
        }
    }

    LaunchedEffect(endTime) {
        if (endTime != null) {
            while (true) {
                val now = System.currentTimeMillis()
                val diff = endTime!! - now
                if (diff > 0) {
                    remainingSeconds = diff / 1000
                    delay(1000)
                } else {
                    remainingSeconds = 0
                    break
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Rice is cooking...", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "You must wait before opening $appName.\nLeave this screen and come back when it's ready.",
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        if (remainingSeconds > 0) {
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.displayLarge
            )
        } else {
            Text(
                text = "Ready! You have 2 minutes to open the app.",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onExit) {
            Text("Go Home")
        }
    }
}
