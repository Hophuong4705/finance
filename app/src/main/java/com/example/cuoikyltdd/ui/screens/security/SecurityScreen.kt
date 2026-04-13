package com.example.cuoikyltdd.ui.screens.security

import com.example.cuoikyltdd.SharedPrefsHelper
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cuoikyltdd.AppGlobalState
import com.example.cuoikyltdd.Route
import com.example.cuoikyltdd.executeBiometricAuth

// ── BỘ MÀU THEO CHUẨN APP MB BANK ──────────────────────────────────────────
private val BgPage       = Color(0xFFF9FAFB)
private val TextMain     = Color(0xFF1E293B)
private val RedMB        = Color(0xFFA11717) // Đỏ thẫm MB
private val OrangeToggle = Color(0xFFF37B30) // Cam nút gạt MB
private val DividerColor = Color(0xFFF1F5F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(navController: NavController) {
    val context = LocalContext.current

    // Trạng thái nút gạt Vân tay
    var isBiometricEnabled by remember { mutableStateOf(AppGlobalState.useBiometric) }

    Scaffold(
        containerColor = BgPage,
        topBar = {
            TopAppBar(
                title = {
                    Text("Cài đặt", fontWeight = FontWeight.Bold, color = RedMB)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = RedMB)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgPage)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // ── KHU VỰC 1: BẢO MẬT ──────────────────────────────────────────
            Text("Bảo mật", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextMain)
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    SecurityRow(Icons.Outlined.PhonelinkSetup, "Quản lý thiết bị truy cập")
                    HorizontalDivider(color = DividerColor)

                    SecurityRow(Icons.Outlined.VpnKey, "Thiết lập Digital OTP")
                    HorizontalDivider(color = DividerColor)

                    SecurityRow(Icons.Outlined.Face, "Thiết lập xác thực khuôn mặt")
                    HorizontalDivider(color = DividerColor)

                    // Dòng Cài đặt Vân tay (Có nút gạt Switch)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Fingerprint, contentDescription = null, tint = RedMB, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Đăng nhập bằng vân tay", fontSize = 15.sp, color = TextMain, modifier = Modifier.weight(1f))

                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = { isTurningOn ->
                                if (isTurningOn) {
                                    executeBiometricAuth(
                                        context = context,
                                        onSuccess = {
                                            isBiometricEnabled = true
                                            SharedPrefsHelper.saveBiometric(true)
                                            Toast.makeText(context, "Đã BẬT vân tay", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { err: String ->
                                            Toast.makeText(context, "Lỗi: $err", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                } else {
                                    isBiometricEnabled = false
                                    SharedPrefsHelper.saveBiometric(false)
                                    Toast.makeText(context, "Đã TẮT vân tay", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = OrangeToggle
                            )
                        )
                    }
                    HorizontalDivider(color = DividerColor)

                    // 🔥 DÒNG ĐỔI MẬT KHẨU MỚI: Bấm vào sẽ bay sang trang đổi mật khẩu
                    SecurityRow(
                        icon = Icons.Outlined.LockReset,
                        title = "Đổi mật khẩu",
                        onClick = {
                            navController.navigate(Route.CHANGE_PASSWORD)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── KHU VỰC 2: GIAO DIỆN ────────────────────────────────────────
            Text("Giao diện", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextMain)
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    SecurityRow(Icons.Outlined.ColorLens, "Đổi theme")
                    HorizontalDivider(color = DividerColor)
                    SecurityRow(Icons.Outlined.Wallpaper, "Thay ảnh nền cá nhân")
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ── COMPONENT TÁI SỬ DỤNG CHO TỪNG DÒNG ─────────────────────────────────────
@Composable
fun SecurityRow(icon: ImageVector, title: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = RedMB, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 15.sp, color = TextMain, modifier = Modifier.weight(1f))

        // Mũi tên điều hướng ở bên phải
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = RedMB)
    }
}