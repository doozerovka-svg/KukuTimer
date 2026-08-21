package com.example.kukutimer

import android.Manifest
import android.app.AppOpsManager
import android.content.ComponentName
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
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.kukutimer.data.AppPreferences
import com.example.kukutimer.service.AppMonitorService
import com.example.kukutimer.service.KukuAccessibilityService
import com.example.kukutimer.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppModel(
    val packageName: String,
    val name: String,
    val isSystem: Boolean,
    val icon: Drawable?
)

enum class AppFilterTab(val title: String, val kanji: String) {
    DOWNLOADED("Скачанные", "導入"),
    SYSTEM("Системные", "基幹"),
    RESTRICTED("Ограниченные", "封印"),
    ALL("Все", "全般")
}

class MainActivity : ComponentActivity() {
    private lateinit var appPreferences: AppPreferences

    private val usageAccessState = mutableStateOf(false)
    private val overlayState = mutableStateOf(false)
    private val accessibilityState = mutableStateOf(false)
    private val notificationState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appPreferences = AppPreferences(this)

        setContent {
            KukuTimerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SumiDark
                ) {
                    MainScreen(
                        appPreferences = appPreferences,
                        packageManager = packageManager,
                        usageAccessGranted = usageAccessState.value,
                        overlayGranted = overlayState.value,
                        accessibilityGranted = accessibilityState.value,
                        notificationGranted = notificationState.value,
                        onRequestUsageAccess = { requestUsageAccess() },
                        onRequestOverlay = { requestOverlayPermission() },
                        onRequestAccessibility = { requestAccessibilityPermission() },
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
        startMonitoringService()
    }

    private fun checkPermissions() {
        usageAccessState.value = hasUsageStatsPermission()
        overlayState.value = Settings.canDrawOverlays(this)
        accessibilityState.value = isAccessibilityServiceEnabled(this, KukuAccessibilityService::class.java)
        notificationState.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun startMonitoringService() {
        try {
            val intent = Intent(this, AppMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

    private fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<*>): Boolean {
        val expectedComponentName = ComponentName(context, serviceClass)
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)
        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledComponent = ComponentName.unflattenFromString(componentNameString)
            if (enabledComponent != null && enabledComponent == expectedComponentName) {
                return true
            }
        }
        return false
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

    private fun requestAccessibilityPermission() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
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
    accessibilityGranted: Boolean,
    notificationGranted: Boolean,
    onRequestUsageAccess: () -> Unit,
    onRequestOverlay: () -> Unit,
    onRequestAccessibility: () -> Unit,
    onRequestNotifications: () -> Unit,
    onRestartService: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val restrictedApps by appPreferences.restrictedApps.collectAsState(initial = emptySet())

    var allApps by remember { mutableStateOf<List<AppModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(AppFilterTab.DOWNLOADED) }
    var searchQuery by remember { mutableStateOf("") }

    val isProtectionActive = accessibilityGranted || (usageAccessGranted && overlayGranted)

    // Load ALL applications on the device
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val launcherActivities = packageManager.queryIntentActivities(launcherIntent, 0)
            val appsMap = mutableMapOf<String, AppModel>()

            // 1. Add all launcher apps (with real user-facing name and icon)
            for (resolveInfo in launcherActivities) {
                val pkg = resolveInfo.activityInfo?.packageName ?: continue
                val name = resolveInfo.loadLabel(packageManager).toString()
                val appInfo = resolveInfo.activityInfo.applicationInfo
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
                        (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
                val icon = try { resolveInfo.loadIcon(packageManager) } catch (e: Exception) { null }

                appsMap[pkg] = AppModel(
                    packageName = pkg,
                    name = name,
                    isSystem = isSystem,
                    icon = icon
                )
            }

            // 2. Add any other installed packages that might not be in launcher
            val installedPackages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            for (appInfo in installedPackages) {
                if (!appsMap.containsKey(appInfo.packageName)) {
                    val name = packageManager.getApplicationLabel(appInfo).toString()
                    val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
                            (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
                    val icon = try { packageManager.getApplicationIcon(appInfo) } catch (e: Exception) { null }

                    appsMap[appInfo.packageName] = AppModel(
                        packageName = appInfo.packageName,
                        name = name,
                        isSystem = isSystem,
                        icon = icon
                    )
                }
            }

            allApps = appsMap.values.sortedBy { it.name.lowercase() }
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
            .background(SumiDark)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // App Header with Japanese Zen Calligraphy & Inkan Seal
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ククターマー",
                        fontSize = 11.sp,
                        color = ShuIro,
                        letterSpacing = 4.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• 白百合",
                        fontSize = 11.sp,
                        color = KinGold,
                        letterSpacing = 2.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Kuku Timer",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Shirayuri,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "10 минут варки риса для осознанности",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = InkTextSecondary,
                        fontSize = 12.sp
                    )
                )
            }

            // Traditional Status Seal
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isProtectionActive) MatsuGreen.copy(alpha = 0.2f) else ShuIro.copy(alpha = 0.15f),
                border = BorderStroke(
                    1.dp,
                    if (isProtectionActive) MatsuGreen.copy(alpha = 0.6f) else ShuIro.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isProtectionActive) MatsuGreen else ShuIro)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isProtectionActive) "Защита активна" else "Требует настройки",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isProtectionActive) Shirayuri else ShuIro,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Permissions Card with Clean Japanese Zen styling
        if (!accessibilityGranted || !usageAccessGranted || !overlayGranted || !notificationGranted) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SumiCard),
                border = BorderStroke(1.dp, ShuIro.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "朱",
                            color = ShuIro,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Активация мгновенного перехвата",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Shirayuri,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Включите Kuku Timer в Спец. возможностях для мгновенного перехвата заблокированных приложений:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = InkTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (!accessibilityGranted) {
                        Button(
                            onClick = onRequestAccessibility,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ShuIro,
                                contentColor = Shirayuri
                            )
                        ) {
                            Text(
                                text = "👉 Включить в Спец. возможностях",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (!overlayGranted) {
                        OutlinedButton(
                            onClick = onRequestOverlay,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, SumiBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = InkTextPrimary)
                        ) {
                            Text("Показ поверх других приложений", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (!usageAccessGranted) {
                        OutlinedButton(
                            onClick = onRequestUsageAccess,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, SumiBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = InkTextPrimary)
                        ) {
                            Text("Доступ к истории использования", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (!notificationGranted) {
                        OutlinedButton(
                            onClick = onRequestNotifications,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, SumiBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = InkTextPrimary)
                        ) {
                            Text("Разрешить уведомления (окно 2 мин)", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Minimalist Zen Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = "Поиск приложения (например, Часы)...",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = InkTextTertiary,
                        fontSize = 13.sp
                    )
                )
            },
            leadingIcon = {
                Text("🔍", modifier = Modifier.padding(start = 12.dp), fontSize = 14.sp)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Text("✕", fontWeight = FontWeight.Bold, color = InkTextSecondary, fontSize = 14.sp)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SumiSurface,
                unfocusedContainerColor = SumiSurface,
                focusedBorderColor = ShuIro.copy(alpha = 0.7f),
                unfocusedBorderColor = SumiBorder,
                focusedTextColor = Shirayuri,
                unfocusedTextColor = Shirayuri
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Japanese Minimalist Category Scrollable Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            edgePadding = 0.dp,
            divider = {},
            containerColor = Color.Transparent,
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            AppFilterTab.values().forEach { tab ->
                val count = when (tab) {
                    AppFilterTab.DOWNLOADED -> downloadedCount
                    AppFilterTab.SYSTEM -> systemCount
                    AppFilterTab.RESTRICTED -> restrictedCount
                    AppFilterTab.ALL -> allApps.size
                }
                val isSelected = selectedTab == tab

                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = tab.kanji,
                                fontSize = 11.sp,
                                color = if (isSelected) ShuIro else InkTextTertiary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${tab.title} ($count)",
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Shirayuri else InkTextSecondary,
                                fontSize = 13.sp
                            )
                        }
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
                CircularProgressIndicator(
                    color = ShuIro,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(36.dp)
                )
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
                    color = InkTextTertiary,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    val isRestricted = restrictedApps.contains(app.packageName)
                    ZenAppItemRow(
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
fun ZenAppItemRow(
    app: AppModel,
    isRestricted: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val animatedBg by animateColorAsState(
        targetValue = if (isRestricted) Color(0xFF261D1C) else SumiSurface,
        animationSpec = tween(300),
        label = "RowBg"
    )

    val animatedBorder by animateColorAsState(
        targetValue = if (isRestricted) ShuIro.copy(alpha = 0.6f) else SumiBorder.copy(alpha = 0.6f),
        animationSpec = tween(300),
        label = "RowBorder"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = animatedBg),
        border = BorderStroke(1.dp, animatedBorder),
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
            // App Icon with Sumi Border
            if (app.icon != null) {
                val bitmap = remember(app.icon) { app.icon.toComposeBitmap() }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = app.name,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .border(0.5.dp, SumiBorder, RoundedCornerShape(11.dp))
                    )
                } else {
                    ZenDefaultAppIcon()
                }
            } else {
                ZenDefaultAppIcon()
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Shirayuri,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (app.isSystem) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SumiCard,
                            border = BorderStroke(0.5.dp, SumiBorder)
                        ) {
                            Text(
                                text = "基幹",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = InkTextTertiary,
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = InkTextTertiary,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = isRestricted,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Shirayuri,
                    checkedTrackColor = ShuIro,
                    uncheckedThumbColor = InkTextTertiary,
                    uncheckedTrackColor = SumiCard,
                    uncheckedBorderColor = SumiBorder
                )
            )
        }
    }
}

@Composable
fun ZenDefaultAppIcon() {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(SumiCard)
            .border(0.5.dp, SumiBorder, RoundedCornerShape(11.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("🍙", fontSize = 18.sp)
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
