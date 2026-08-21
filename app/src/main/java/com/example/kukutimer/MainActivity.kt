package com.example.kukutimer

import android.Manifest
import android.app.AlarmManager
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.kukutimer.data.AppPreferences
import com.example.kukutimer.service.AppMonitorService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppModel(
    val packageName: String,
    val name: String,
    val isSystem: Boolean,
    val icon: Drawable?
)

enum class AppFilterTab(val title: String) {
    DOWNLOADED("Скачанные"),
    SYSTEM("Системные"),
    RESTRICTED("Ограниченные"),
    ALL("Все")
}

class MainActivity : ComponentActivity() {
    private lateinit var appPreferences: AppPreferences

    private val usageAccessState = mutableStateOf(false)
    private val overlayState = mutableStateOf(false)
    private val notificationState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appPreferences = AppPreferences(this)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF121316),
                    surface = Color(0xFF1E1F24),
                    primary = Color(0xFFE0A96D),
                    onPrimary = Color(0xFF201300),
                    onBackground = Color(0xFFECEFF4)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        appPreferences = appPreferences,
                        packageManager = packageManager,
                        usageAccessGranted = usageAccessState.value,
                        overlayGranted = overlayState.value,
                        notificationGranted = notificationState.value,
                        onRequestUsageAccess = { requestUsageAccess() },
                        onRequestOverlay = { requestOverlayPermission() },
                        onRequestNotifications = { requestNotificationPermission() },
                        onRestartService = { startMonitoringService() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
        if (usageAccessState.value) {
            startMonitoringService()
        }
    }

    private fun checkPermissions() {
        usageAccessState.value = hasUsageStatsPermission()
        overlayState.value = Settings.canDrawOverlays(this)
        notificationState.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun startMonitoringService() {
        val intent = Intent(this, AppMonitorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageAccess() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    appPreferences: AppPreferences,
    packageManager: PackageManager,
    usageAccessGranted: Boolean,
    overlayGranted: Boolean,
    notificationGranted: Boolean,
    onRequestUsageAccess: () -> Unit,
    onRequestOverlay: () -> Unit,
    onRequestNotifications: () -> Unit,
    onRestartService: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val restrictedApps by appPreferences.restrictedApps.collectAsState(initial = emptySet())

    var allApps by remember { mutableStateOf<List<AppModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(AppFilterTab.DOWNLOADED) }
    var searchQuery by remember { mutableStateOf("") }

    // Load installed apps asynchronously
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val installedPackages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            val list = installedPackages.mapNotNull { appInfo ->
                val name = packageManager.getApplicationLabel(appInfo).toString()
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
                        (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
                val icon = try {
                    packageManager.getApplicationIcon(appInfo)
                } catch (e: Exception) {
                    null
                }
                AppModel(
                    packageName = appInfo.packageName,
                    name = name,
                    isSystem = isSystem,
                    icon = icon
                )
            }.sortedBy { it.name.lowercase() }

            allApps = list
            isLoading = false
        }
    }

    // Filter apps based on tab and search
    val filteredApps = remember(allApps, selectedTab, searchQuery, restrictedApps) {
        allApps.filter { app ->
            val matchesTab = when (selectedTab) {
                AppFilterTab.DOWNLOADED -> !app.isSystem
                AppFilterTab.SYSTEM -> app.isSystem
                AppFilterTab.RESTRICTED -> restrictedApps.contains(app.packageName)
                AppFilterTab.ALL -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                    app.name.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)

            matchesTab && matchesSearch
        }
    }

    val downloadedCount = remember(allApps) { allApps.count { !it.isSystem } }
    val systemCount = remember(allApps) { allApps.count { it.isSystem } }
    val restrictedCount = restrictedApps.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "🍚 Kuku Timer",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "10 минут варки риса перед входом",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (usageAccessGranted && overlayGranted) Color(0xFF1E382B) else Color(0xFF3E2020)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (usageAccessGranted && overlayGranted) Color(0xFF4CAF50) else Color(0xFFE53935))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (usageAccessGranted && overlayGranted) "Активен" else "Требует прав",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (usageAccessGranted && overlayGranted) Color(0xFF81C784) else Color(0xFFE57373),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Permissions warning section
        if (!usageAccessGranted || !overlayGranted || !notificationGranted) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚠️", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Необходимые разрешения",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Для перехвата приложений и показа таймера требуются разрешения Android:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (!usageAccessGranted) {
                        Button(
                            onClick = onRequestUsageAccess,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("1. Разрешить доступ к истории использования", color = MaterialTheme.colorScheme.onPrimary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (!overlayGranted) {
                        OutlinedButton(
                            onClick = onRequestOverlay,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("2. Разрешить показ поверх других приложений")
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (!notificationGranted) {
                        OutlinedButton(
                            onClick = onRequestNotifications,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("3. Разрешить уведомления (для окна 2 минут)")
                        }
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Поиск по имени или пакету...", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Text("🔍", modifier = Modifier.padding(start = 12.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Text("✕", fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        )

        // Filter Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            edgePadding = 0.dp,
            divider = {},
            containerColor = Color.Transparent,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            AppFilterTab.values().forEach { tab ->
                val count = when (tab) {
                    AppFilterTab.DOWNLOADED -> downloadedCount
                    AppFilterTab.SYSTEM -> systemCount
                    AppFilterTab.RESTRICTED -> restrictedCount
                    AppFilterTab.ALL -> allApps.size
                }
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = "${tab.title} ($count)",
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == tab) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                )
            }
        }

        // App List
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (filteredApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotEmpty()) "Ничего не найдено" else "В этой категории нет приложений",
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    val isRestricted = restrictedApps.contains(app.packageName)
                    AppItemRow(
                        app = app,
                        isRestricted = isRestricted,
                        onToggle = { checked ->
                            scope.launch {
                                appPreferences.setAppRestricted(app.packageName, checked)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppItemRow(
    app: AppModel,
    isRestricted: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRestricted) Color(0xFF2B251F) else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!isRestricted) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (app.icon != null) {
                val bitmap = remember(app.icon) { app.icon.toComposeBitmap() }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = app.name,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                } else {
                    DefaultAppIcon()
                }
            } else {
                DefaultAppIcon()
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (app.isSystem) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF2E3138)
                        ) {
                            Text(
                                text = "Системное",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.LightGray,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = isRestricted,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
fun DefaultAppIcon() {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF2E3138)),
        contentAlignment = Alignment.Center
    ) {
        Text("📱", fontSize = 20.sp)
    }
}

private fun Drawable.toComposeBitmap(): ImageBitmap? {
    return try {
        if (this is BitmapDrawable && this.bitmap != null) {
            this.bitmap.asImageBitmap()
        } else {
            val width = if (intrinsicWidth > 0) intrinsicWidth else 96
            val height = if (intrinsicHeight > 0) intrinsicHeight else 96
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
            bitmap.asImageBitmap()
        }
    } catch (e: Exception) {
        null
    }
}
