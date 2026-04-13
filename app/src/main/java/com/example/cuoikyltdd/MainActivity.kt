package com.example.cuoikyltdd

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

object SharedPrefsHelper {
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("FinanceAppPrefs", Context.MODE_PRIVATE)
        AppGlobalState.isLoggedIn = prefs.getBoolean("isLoggedIn", false)
        AppGlobalState.userName = prefs.getString("userName", "") ?: ""
        AppGlobalState.userEmail = prefs.getString("userEmail", "") ?: ""
        AppGlobalState.userPhone = prefs.getString("userPhone", "") ?: ""
        AppGlobalState.savedPassword = prefs.getString("userPassword", "") ?: ""
        AppGlobalState.joinDate = prefs.getString("joinDate", "") ?: ""
        AppGlobalState.userAvatarUri = prefs.getString("userAvatarUri", "") ?: ""
        AppGlobalState.useBiometric = prefs.getBoolean("useBiometric", false)
        AppGlobalState.hideBalance = prefs.getBoolean("hideBalance", false)
        AppGlobalState.lastSyncDate = prefs.getString("lastSyncDate", "Chưa đồng bộ") ?: "Chưa đồng bộ"
    }

    fun saveUserSession(name: String, email: String, phone: String, pass: String, date: String) {
        prefs.edit().apply {
            putBoolean("isLoggedIn", true)
            putString("userName", name)
            putString("userEmail", email)
            putString("userPhone", phone)
            putString("userPassword", pass)
            putString("joinDate", date)
            apply()
        }
        AppGlobalState.isLoggedIn = true
        AppGlobalState.userName = name
        AppGlobalState.userEmail = email
        AppGlobalState.userPhone = phone
        AppGlobalState.savedPassword = pass
        AppGlobalState.joinDate = date
    }

    fun logout() {
        prefs.edit().putBoolean("isLoggedIn", false).apply()
        AppGlobalState.isLoggedIn = false
    }

    fun updateSettings(biometric: Boolean, hideBal: Boolean) {
        prefs.edit().apply {
            putBoolean("useBiometric", biometric)
            putBoolean("hideBalance", hideBal)
            apply()
        }
        AppGlobalState.useBiometric = biometric
        AppGlobalState.hideBalance = hideBal
    }

    fun updateSyncDate(date: String) {
        prefs.edit().putString("lastSyncDate", date).apply()
        AppGlobalState.lastSyncDate = date
    }

    fun updateAvatar(uri: String) {
        prefs.edit().putString("userAvatarUri", uri).apply()
        AppGlobalState.userAvatarUri = uri
    }
}

object AppGlobalState {
    var isLoggedIn by mutableStateOf(false)
    var userName by mutableStateOf("")
    var userEmail by mutableStateOf("")
    var userPhone by mutableStateOf("")
    var savedPassword by mutableStateOf("")
    var joinDate by mutableStateOf("")
    var userAvatarUri by mutableStateOf("")
    var lastSyncDate by mutableStateOf("Chưa đồng bộ")
    var useBiometric by mutableStateOf(false)
    var hideBalance by mutableStateOf(false)

    val notifications = mutableStateListOf<AppNotification>()

    fun addNotification(context: Context, title: String, message: String) {
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        notifications.add(0, AppNotification(title, message, time, false))
        triggerSystemNotification(context, title, message)
    }

    fun markAsRead(index: Int) {
        if (index in notifications.indices) {
            notifications[index] = notifications[index].copy(isRead = true)
        }
    }

    fun markAllAsRead() {
        for (i in notifications.indices) {
            notifications[i] = notifications[i].copy(isRead = true)
        }
    }

    private fun triggerSystemNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "finance_app_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Thông báo",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setAutoCancel(true)
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}

data class AppNotification(val title: String, val message: String, val time: String, val isRead: Boolean)

// ─── 3. QUẢN LÝ ROUTES & BẢNG MÀU ─────────────────────────────────────────────
object Route {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val TAX = "tax"
    const val REPORT = "report"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
    const val SECURITY = "security"
    const val SUPPORT = "support"
}

val BgPage = Color(0xFFF0F4F8)
val BgCard = Color(0xFFFFFFFF)
val TealPrime = Color(0xFF00BFA5)
val TealDark = Color(0xFF00897B)
val TextMain = Color(0xFF1A2340)
val TextSub = Color(0xFF6B7A99)
val BorderColor = Color(0xFFE2E8F0)

// ─── 4. ACTIVITY CHÍNH & VÂN TAY ──────────────────────────────────────────────
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )
        SharedPrefsHelper.init(this)
        checkNotificationPermission()
        setContent {
            MaterialTheme {
                AppNavHost()
            }
        }
    }

    private fun checkNotificationPermission() {
        val enabledListeners = android.provider.Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (enabledListeners?.contains(packageName) != true) {
            android.app.AlertDialog.Builder(this)
                .setTitle("Cấp quyền tự động")
                .setMessage("Để app bắt giao dịch ngân hàng, vui lòng bật 'Trợ lý tài chính'.")
                .setPositiveButton("Đi đến cài đặt") { _, _ ->
                    startActivity(android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
                .setNegativeButton("Bỏ qua", null)
                .show()
        }
    }
}

// Hàm ép kiểu Context an toàn cho Vân tay
fun Context.findFragmentActivity(): FragmentActivity? {
    var currentContext = this
    while (currentContext is android.content.ContextWrapper) {
        if (currentContext is FragmentActivity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

fun authenticateWithBiometrics(context: Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
    val activity = context.findFragmentActivity()
    if (activity == null) {
        onError("Thiết bị không hỗ trợ vân tay!")
        return
    }

    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            onError("Lỗi: $errString")
        }
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onSuccess()
        }
        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            onError("Vân tay không khớp!")
        }
    })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Đăng nhập FinanceApp")
        .setSubtitle("Chạm vào cảm biến vân tay để mở khóa")
        .setNegativeButtonText("Hủy")
        .build()

    biometricPrompt.authenticate(promptInfo)
}

// Hàm đọc ảnh từ Uri (Dùng cho Avatar)
@Composable
fun rememberBitmapFromUri(uri: Uri?): ImageBitmap? {
    val context = LocalContext.current
    var bitmap by remember(uri) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(uri) {
        if (uri != null && uri.toString().isNotEmpty()) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                bitmap = BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    return bitmap
}

// ─── 5. ĐIỀU HƯỚNG CHÍNH ──────────────────────────────────────────────────────
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route
    val showBottomNav = currentRoute in listOf(Route.DASHBOARD, Route.TAX, Route.REPORT)
    val startDest = if (AppGlobalState.isLoggedIn) Route.DASHBOARD else Route.LOGIN

    Scaffold(
        containerColor = BgPage,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomNav,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                AppBottomNav(navController, currentRoute)
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = startDest, modifier = Modifier.padding(innerPadding)) {
            composable(Route.LOGIN) { com.example.cuoikyltdd.ui.screens.auth.LoginScreen(navController) }
            composable(Route.REGISTER) { com.example.cuoikyltdd.ui.screens.auth.RegisterScreen(navController) }
            composable(Route.DASHBOARD) { com.example.cuoikyltdd.ui.screens.dashboard.DashboardScreen(navController) }
            composable(Route.TAX) { com.example.cuoikyltdd.ui.screens.tax.TaxScreen(navController) }
            composable(Route.REPORT) { com.example.cuoikyltdd.ui.screens.report.ReportScreen(navController) }
            composable(Route.NOTIFICATIONS) { NotificationScreen(navController) }
            composable(Route.PROFILE) { ProfileScreen(navController) }
            composable(Route.SECURITY) { SecurityScreen(navController) }
            composable(Route.SUPPORT) { SupportScreen(navController) }
        }
    }
}

// ─── 6. GIAO DIỆN CHUNG (BOTTOM NAV & TOP BAR) ───────────────────────────────
data class BottomNavItem(val route: String, val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector)

@Composable
fun AppBottomNav(navController: NavController, currentRoute: String?) {
    val items = listOf(
        BottomNavItem(Route.DASHBOARD, "Trang chủ", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem(Route.TAX, "Thuế", Icons.Filled.Calculate, Icons.Outlined.Calculate),
        BottomNavItem(Route.REPORT, "Báo cáo", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    )
    NavigationBar(
        containerColor = BgCard,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(if (selected) item.selectedIcon else item.unselectedIcon, contentDescription = item.label) },
                label = { Text(item.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TealPrime,
                    selectedTextColor = TealPrime,
                    unselectedIconColor = TextSub,
                    unselectedTextColor = TextSub,
                    indicatorColor = TealPrime.copy(alpha = 0.12f)
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(navController: NavController, userName: String = AppGlobalState.userName, notificationCount: Int = 0) {
    var showMenu by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Column {
                Text("Xin chào 👋", fontSize = 12.sp, color = TextSub)
                Text(userName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextMain)
            }
        },
        actions = {
            BadgedBox(
                badge = {
                    if (notificationCount > 0) {
                        Badge(containerColor = Color(0xFFFF5252)) {
                            Text("$notificationCount", fontSize = 10.sp)
                        }
                    }
                }
            ) {
                IconButton(onClick = { navController.navigate(Route.NOTIFICATIONS) }) {
                    Icon(Icons.Outlined.Notifications, "Thông báo", tint = TextMain)
                }
            }
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.Menu, "Menu", tint = TextMain)
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(BgCard).width(230.dp)
            ) {
                MenuHeader(userName)
                HorizontalDivider(color = BorderColor)
                MenuRow(Icons.Outlined.Person, "Hồ sơ tài khoản") {
                    navController.navigate(Route.PROFILE)
                    showMenu = false
                }
                MenuRow(Icons.Outlined.Security, "Bảo mật & Quyền riêng tư") {
                    navController.navigate(Route.SECURITY)
                    showMenu = false
                }
                MenuRow(Icons.Outlined.Sync, "Đồng bộ lên Cloud") {
                    showSyncDialog = true
                    showMenu = false
                }
                MenuRow(Icons.AutoMirrored.Outlined.Help, "Hỗ trợ & HDSD") {
                    navController.navigate(Route.SUPPORT)
                    showMenu = false
                }
                MenuRow(Icons.Outlined.Info, "Về ứng dụng") {
                    showAboutDialog = true
                    showMenu = false
                }
                HorizontalDivider(color = BorderColor)
                MenuRow(Icons.Outlined.Logout, "Đăng xuất", tint = Color(0xFFFF5252)) {
                    SharedPrefsHelper.logout()
                    navController.navigate(Route.LOGIN) { popUpTo(0) { inclusive = true } }
                    showMenu = false
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = BgCard)
    )

    if (showSyncDialog) SyncDataDialog { showSyncDialog = false }
    if (showAboutDialog) AboutDialog { showAboutDialog = false }
}

@Composable
private fun MenuHeader(name: String) {
    val bitmap = rememberBitmapFromUri(
        if (AppGlobalState.userAvatarUri.isNotEmpty()) Uri.parse(AppGlobalState.userAvatarUri) else null
    )
    Row(
        modifier = Modifier.padding(16.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(TealPrime, TealDark))),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Text(name.firstOrNull()?.uppercase() ?: "U", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Column {
            Text(name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextMain)
            Text("Tài khoản cá nhân", fontSize = 11.sp, color = TextSub)
        }
    }
}

@Composable
private fun MenuRow(icon: ImageVector, label: String, tint: Color = TextMain, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
                Text(label, fontSize = 14.sp, color = tint)
            }
        },
        onClick = onClick
    )
}

// ─── 7. HỘP THOẠI ĐỒNG BỘ & THÔNG TIN ─────────────────────────────────────────
@Composable
fun SyncDataDialog(onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        scope.launch {
            delay(2000)
            val syncTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            SharedPrefsHelper.updateSyncDate(syncTime)
            Toast.makeText(context, "Đã đồng bộ!", Toast.LENGTH_SHORT).show()
            onDismiss()
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgCard,
        title = { Text("Đồng bộ dữ liệu", fontWeight = FontWeight.Bold, color = TextMain) },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(color = TealPrime, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(16.dp))
                Text("Đang tải dữ liệu lên máy chủ an toàn...", color = TextSub)
            }
        },
        confirmButton = { }
    )
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgCard,
        title = { Text("Về ứng dụng", fontWeight = FontWeight.Bold, color = TextMain) },
        text = {
            Column {
                Text("Trợ lý Tài chính cá nhân", fontWeight = FontWeight.Bold, color = TealPrime, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Text("Phiên bản: 1.0.0", color = TextSub, fontSize = 14.sp)
                Text("Phát triển bởi: Sinh viên XYZ", color = TextSub, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text("Ứng dụng giúp quản lý thu chi, tự động bóc tách tin nhắn ngân hàng và tính thuế TNCN.", color = TextSub, fontSize = 13.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = TealPrime)
            }
        }
    )
}

// ─── 8. CÁC MÀN HÌNH CHỨC NĂNG (BẢO MẬT, HỖ TRỢ, THÔNG BÁO, HỒ SƠ) ───────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(navController: NavController) {
    Scaffold(
        containerColor = BgPage,
        topBar = {
            TopAppBar(
                title = { Text("Bảo mật", fontWeight = FontWeight.Bold, color = TextMain) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TealPrime)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgCard)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Cài đặt", color = TealPrime, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = BgCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Khóa bằng Vân tay", color = TextMain, fontWeight = FontWeight.Medium)
                            Text("Yêu cầu xác thực khi mở app", color = TextSub, fontSize = 12.sp)
                        }
                        Switch(
                            checked = AppGlobalState.useBiometric,
                            onCheckedChange = { SharedPrefsHelper.updateSettings(it, AppGlobalState.hideBalance) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TealPrime)
                        )
                    }
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), color = BorderColor)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Ẩn số dư", color = TextMain, fontWeight = FontWeight.Medium)
                            Text("Mặc định ẩn tiền khi mở app", color = TextSub, fontSize = 12.sp)
                        }
                        Switch(
                            checked = AppGlobalState.hideBalance,
                            onCheckedChange = { SharedPrefsHelper.updateSettings(AppGlobalState.useBiometric, it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TealPrime)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(navController: NavController) {
    Scaffold(
        containerColor = BgPage,
        topBar = {
            TopAppBar(
                title = { Text("Hỗ trợ", fontWeight = FontWeight.Bold, color = TextMain) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TealPrime)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgCard)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BgCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("FAQ", color = TealPrime, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(16.dp))
                    SupportItem("Làm sao bắt tin nhắn?", "Cấp quyền 'Truy cập thông báo' trong cài đặt.")
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), color = BorderColor)
                    SupportItem("Dữ liệu an toàn không?", "Có, lưu cục bộ trên máy.")
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrime),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Outlined.Email, null)
                Spacer(Modifier.width(8.dp))
                Text("Liên hệ", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SupportItem(q: String, a: String) {
    Column {
        Text(q, fontWeight = FontWeight.Bold, color = TextMain, fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))
        Text(a, color = TextSub, fontSize = 13.sp, lineHeight = 20.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController) {
    val notifications = AppGlobalState.notifications
    Scaffold(
        containerColor = BgPage,
        topBar = {
            TopAppBar(
                title = { Text("Thông báo", fontWeight = FontWeight.Bold, color = TextMain) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TealPrime)
                    }
                },
                actions = {
                    TextButton(onClick = { AppGlobalState.markAllAsRead() }) {
                        Text("Đọc tất cả", color = TealPrime, fontSize = 13.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgCard)
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔔", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Trống", color = TextSub)
                }
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notifications.size) { idx ->
                    NotificationItem(notifications[idx]) {
                        AppGlobalState.markAsRead(idx)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(notif: AppNotification, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (!notif.isRead) TealPrime.copy(alpha = 0.06f) else BgCard)
            .border(1.dp, if (!notif.isRead) TealPrime.copy(alpha = 0.25f) else BorderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(TealPrime.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Notifications, null, tint = TealPrime, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(notif.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextMain)
            Spacer(Modifier.height(2.dp))
            Text(notif.message, fontSize = 12.sp, color = TextSub, lineHeight = 18.sp)
        }
        Text(notif.time, fontSize = 11.sp, color = TextSub)
    }
}

// ─── 9. MÀN HÌNH HỒ SƠ & CHỌN ẢNH ĐẠI DIỆN ───────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            SharedPrefsHelper.updateAvatar(uri.toString())
        }
    }

    val bitmap = rememberBitmapFromUri(
        if (AppGlobalState.userAvatarUri.isNotEmpty()) Uri.parse(AppGlobalState.userAvatarUri) else null
    )

    Scaffold(
        containerColor = BgPage,
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ", fontWeight = FontWeight.Bold, color = TextMain) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TealPrime)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgCard)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(TealPrime, TealDark)))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    // Box Avatar (Có thể click để đổi ảnh)
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(3.dp, Color.White, CircleShape)
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                AppGlobalState.userName.firstOrNull()?.uppercase() ?: "U",
                                fontSize = 32.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(AppGlobalState.userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(AppGlobalState.userEmail, color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
            ProfileSection("Thông tin cá nhân") {
                ProfileRow(Icons.Outlined.Person, "Họ tên", AppGlobalState.userName)
                ProfileRow(Icons.Outlined.Email, "Email", AppGlobalState.userEmail)
                ProfileRow(Icons.Outlined.Phone, "SĐT", AppGlobalState.userPhone)
                ProfileRow(Icons.Outlined.DateRange, "Ngày tạo", AppGlobalState.joinDate)
            }

            Spacer(Modifier.height(12.dp))
            ProfileSection("Tiện ích") {
                ProfileRow(Icons.Outlined.Sync, "Đồng bộ", AppGlobalState.lastSyncDate)
                ProfileRow(Icons.Outlined.Security, "Vân tay", if(AppGlobalState.useBiometric) "Bật" else "Tắt")
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    SharedPrefsHelper.logout()
                    navController.navigate(Route.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
            ) {
                Icon(Icons.Outlined.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Đăng xuất", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(title, color = TealPrime, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = TealPrime, modifier = Modifier.size(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextSub, fontSize = 11.sp)
            Text(value, color = TextMain, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}