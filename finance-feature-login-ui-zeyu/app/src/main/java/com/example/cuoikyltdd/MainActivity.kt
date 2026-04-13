
package com.example.cuoikyltdd

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.cuoikyltdd.ui.screens.auth.LoginScreen
import com.example.cuoikyltdd.ui.screens.auth.RegisterScreen
import com.example.cuoikyltdd.ui.screens.dashboard.DashboardScreen
import com.example.cuoikyltdd.ui.screens.help.HelpScreen
import com.example.cuoikyltdd.ui.screens.notifications.NotificationsScreen
import com.example.cuoikyltdd.ui.screens.profile.ProfileScreen
import com.example.cuoikyltdd.ui.screens.profile.ChangePasswordScreen
import com.example.cuoikyltdd.ui.screens.report.ReportScreen
import com.example.cuoikyltdd.ui.screens.security.SecurityScreen
import com.example.cuoikyltdd.ui.screens.tax.TaxScreen
import com.example.cuoikyltdd.ui.theme.FinanceMeTheme
import com.example.cuoikyltdd.util.NotificationHelper // 🔥 ĐÃ THÊM: Import thư viện thông báo
import dagger.hilt.android.AndroidEntryPoint

object Route {
    const val LOGIN           = "login"
    const val REGISTER        = "register"
    const val DASHBOARD       = "dashboard"
    const val TAX             = "tax"
    const val REPORT          = "report"
    const val NOTIFICATIONS   = "notifications"
    const val PROFILE         = "profile"
    const val SECURITY        = "security"
    const val HELP            = "help"
    const val CHANGE_PASSWORD = "change_password"
}

@Suppress("SpellCheckingInspection", "unused")
object SharedPrefsHelper {
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("FinancePrefs", Context.MODE_PRIVATE)
        val lastEmail = getLastEmail()
        if (lastEmail.isNotBlank()) {
            loadUserSession(lastEmail)
        }
    }

    fun loadUserSession(email: String) {
        AppGlobalState.userEmail     = email
        AppGlobalState.userName      = prefs.getString("name_$email", "Người dùng") ?: "Người dùng"
        AppGlobalState.userAvatarUri = prefs.getString("avatarUri_$email", "") ?: ""
        AppGlobalState.useBiometric  = prefs.getBoolean("bio_$email", false)
        AppGlobalState.isLoggedIn    = getToken().isNotEmpty()
    }

    fun saveToken(token: String) {
        prefs.edit(commit = true) { putString("jwt_token", token) }
    }

    fun getToken(): String = prefs.getString("jwt_token", "") ?: ""

    fun saveUserSession(name: String, email: String, token: String) {
        saveToken(token)
        saveLastEmail(email)
        prefs.edit(commit = true) {
            putString("name_$email", name)
            putString("email", email)
        }
        loadUserSession(email)
    }

    fun updatePassword(newPass: String) {
        prefs.edit(commit = true) { putString("pass", newPass) }
        AppGlobalState.savedPassword = newPass
    }

    fun saveBiometric(enabled: Boolean) {
        val email = AppGlobalState.userEmail
        prefs.edit(commit = true) { putBoolean("bio_$email", enabled) }
        AppGlobalState.useBiometric = enabled
    }

    fun saveAvatarUri(email: String, uri: String) {
        prefs.edit(commit = true) { putString("avatarUri_$email", uri) }
        AppGlobalState.userAvatarUri = uri
    }

    fun logout() {
        prefs.edit(commit = true) { remove("jwt_token") }
        AppGlobalState.isLoggedIn = false
    }

    fun hasAccountAndToken(): Boolean = getToken().isNotEmpty()

    fun saveLastEmail(email: String) {
        prefs.edit(commit = true) { putString("last_email", email) }
    }

    fun getLastEmail(): String = prefs.getString("last_email", "") ?: ""
}

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val requestSmsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(this, "Tính năng bắt giao dịch tự động bị hạn chế!", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SharedPrefsHelper.init(this)

        // 🔥 ĐÃ FIX: Khởi tạo kênh thông báo ngay khi mở App để Android nhận diện Kênh v3
        NotificationHelper.createChannel(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestSmsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
        }

        if (!isNotificationServiceEnabled()) {
            showNotificationPermissionDialog()
        }

        setContent {
            FinanceMeTheme {
                AppNavHost(activityContext = this)
            }
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(packageName) == true
    }

    private fun showNotificationPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Quyền thông báo")
            .setMessage("Cấp quyền thông báo giúp ứng dụng tự động phân tích dữ liệu từ App ngân hàng.")
            .setPositiveButton("Cài đặt") { _, _ ->
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}

@Composable
fun AppNavHost(activityContext: Context) {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    val showBottomNav = currentRoute in listOf(Route.DASHBOARD, Route.TAX, Route.REPORT)

    // Bất kể đã đăng nhập hay chưa, mỗi khi mở App đều bị ném về màn hình LOGIN.
    // Nếu trong máy đã có thông tin, LoginScreen sẽ tự hiện tên và nút Vân tay.
    var startDest by remember {
        mutableStateOf(Route.LOGIN)
    }

    Scaffold(
        bottomBar = { if (showBottomNav) AppBottomNav(navController, currentRoute) }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = startDest,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Route.LOGIN)           { LoginScreen(navController) }
            composable(Route.REGISTER)        { RegisterScreen(navController) }
            composable(Route.DASHBOARD)       { DashboardScreen(navController) }
            composable(Route.TAX)             { TaxScreen(navController) }
            composable(Route.REPORT)          { ReportScreen(navController) }
            composable(Route.NOTIFICATIONS)   { NotificationsScreen(navController) }
            composable(Route.PROFILE)         { ProfileScreen(navController) }
            composable(Route.SECURITY)        { SecurityScreen(navController) }
            composable(Route.HELP)            { HelpScreen(navController) }
            composable(Route.CHANGE_PASSWORD) { ChangePasswordScreen(navController) }
        }
    }
}

@Composable
fun AppBottomNav(navController: NavController, currentRoute: String?) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        val navItems = listOf(
            Triple(Route.DASHBOARD, "Trang chủ", Icons.Default.Home),
            Triple(Route.TAX,       "Thuế TNCN", Icons.Default.Calculate),
            Triple(Route.REPORT,    "Báo cáo",   Icons.Default.BarChart)
        )

        navItems.forEach { (route, itemLabel, icon) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick  = {
                    navController.navigate(route) {
                        popUpTo(Route.DASHBOARD) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon  = { Icon(imageVector = icon, contentDescription = itemLabel) },
                label = {
                    Text(
                        text = itemLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    }
}

/**
 * TÊN HÀM ĐỘC NHẤT: executeBiometricAuth (Tránh lỗi Conflicting Overloads)
 */
fun executeBiometricAuth(
    context: Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val activity = context as? FragmentActivity ?: return
    val executor = ContextCompat.getMainExecutor(context)
    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                    onError(errString.toString())
                }
            }
            override fun onAuthenticationFailed() {
                onError("Dữ liệu vân tay không hợp lệ!")
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Xác thực hệ thống")
        .setSubtitle("Vui lòng chạm vào cảm biến vân tay")
        .setNegativeButtonText("Sử dụng mật khẩu")
        .build()
    biometricPrompt.authenticate(promptInfo)
}