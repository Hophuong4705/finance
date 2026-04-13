
package com.example.cuoikyltdd.ui.screens.profile
import com.example.cuoikyltdd.SharedPrefsHelper
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.HelpCenter // Đã thêm import này
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cuoikyltdd.AppGlobalState
import com.example.cuoikyltdd.Route
import androidx.core.net.toUri

// ── BỘ MÀU SẮC CHỦ ĐẠO THEO GIAO DIỆN GỐC ────────────────────────────────────
private val TealDark  = Color(0xFF006064)
private val TealLight = Color(0xFF00BFA5)
private val BgPage    = Color(0xFFF0F4F8)
private val BgCard    = Color(0xFFFFFFFF)
private val TextMain  = Color(0xFF1A2340)
private val TextSub   = Color(0xFF6B7A99)
private val ErrorRed  = Color(0xFFE53935)
private val BorderCol = Color(0xFFF1F5F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context             = LocalContext.current
    var showLogoutDialog    by remember { mutableStateOf(false) }

    var avatarUri           by remember { mutableStateOf(AppGlobalState.userAvatarUri) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                Log.e("PROFILE_SCREEN", "Permission denied for Image URI", e)
            }

            val uriStr = it.toString()
            avatarUri  = uriStr
            AppGlobalState.userAvatarUri = uriStr
            SharedPrefsHelper.saveAvatarUri(AppGlobalState.userEmail, uriStr)

            Toast.makeText(context, "Đã cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show()
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title   = {
                Text(
                    text = "Xác nhận đăng xuất",
                    fontWeight = FontWeight.ExtraBold,
                    color = TextMain
                )
            },
            text    = {
                Text(
                    text = "Bạn có chắc chắn muốn thoát khỏi phiên làm việc này không? Để tiếp tục sử dụng, bạn sẽ cần đăng nhập lại.",
                    color = TextSub,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        SharedPrefsHelper.logout()
                        navController.navigate(Route.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Đăng xuất ngay", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Hủy bỏ", color = TextSub)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    Scaffold(
        containerColor = BgPage,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hồ sơ cá nhân",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TealDark)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(TealDark, TealLight)
                        )
                    )
                    .padding(top = 35.dp, bottom = 45.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .border(4.dp, Color.White, CircleShape)
                                .background(Color.White.copy(0.25f))
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarUri.isNotBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(avatarUri.toUri())
                                        .crossfade(true)
                                        .build(),
                                    contentDescription  = "Avatar người dùng",
                                    contentScale        = ContentScale.Crop,
                                    modifier            = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = AppGlobalState.userName
                                        .firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                                    color      = Color.White,
                                    fontSize   = 46.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(TealLight)
                                .border(2.5.dp, Color.White, CircleShape)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Thay đổi ảnh đại diện",
                                tint     = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = AppGlobalState.userName,
                        color      = Color.White,
                        fontSize   = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = AppGlobalState.userEmail.ifBlank { "Chưa cập nhật email" },
                        color    = Color.White.copy(0.85f),
                        fontSize = 14.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(0.2f)
                    ) {
                        Text(
                            text = "Thành viên từ: ${AppGlobalState.joinDate.ifBlank { "Hôm nay" }}",
                            color    = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            SectionTitleHeader(title = "THÔNG TIN CHI TIẾT")

            ProfileCardContainer {
                ProfileInfoRow(
                    icon = Icons.Outlined.Person,
                    label = "Họ và tên",
                    value = AppGlobalState.userName
                )
                ProfileDividerLine()
                ProfileInfoRow(
                    icon = Icons.Outlined.Email,
                    label = "Địa chỉ Email đăng nhập",
                    value = AppGlobalState.userEmail.ifBlank { "Chưa khai báo" }
                )
                ProfileDividerLine()
                ProfileInfoRow(
                    icon = Icons.Outlined.Phone,
                    label = "Số điện thoại liên lạc",
                    value = AppGlobalState.userPhone.ifBlank { "Chưa cập nhật" }
                )
                ProfileDividerLine()
                ProfileInfoRow(
                    icon = Icons.Outlined.DateRange,
                    label = "Thời điểm khởi tạo",
                    value = AppGlobalState.joinDate.ifBlank { "Mới đây" }
                )
            }

            Spacer(Modifier.height(20.dp))

            SectionTitleHeader(title = "CÀI ĐẶT & BẢO MẬT")

            ProfileCardContainer {
                ProfileActionRow(
                    icon  = Icons.Outlined.Security,
                    label = "Bảo mật & Cấu hình Vân tay",
                    tint  = Color(0xFF7B1FA2)
                ) {
                    navController.navigate(Route.SECURITY)
                }

                ProfileDividerLine()

                ProfileActionRow(
                    icon  = Icons.Outlined.Lock,
                    label = "Đổi mật khẩu tài khoản",
                    tint  = Color(0xFFF57C00)
                ) {
                    navController.navigate("change_password")
                }

                ProfileDividerLine()

                ProfileActionRow(
                    icon  = Icons.Outlined.NotificationsActive,
                    label = "Tùy chỉnh Thông báo",
                    tint  = Color(0xFFE91E63)
                ) {
                    navController.navigate(Route.NOTIFICATIONS)
                }

                ProfileDividerLine()

                // 🔥 ĐÃ FIX LỖI CẢNH BÁO VÀNG Ở ĐÂY (Dùng AutoMirrored)
                ProfileActionRow(
                    icon  = Icons.AutoMirrored.Outlined.HelpCenter,
                    label = "Trung tâm Hỗ trợ Khách hàng",
                    tint  = Color(0xFF0288D1)
                ) {
                    navController.navigate(Route.HELP)
                }
            }

            Spacer(Modifier.height(20.dp))

            SectionTitleHeader(title = "QUẢN TRỊ TÀI KHOẢN")

            ProfileCardContainer {
                ProfileActionRow(
                    icon      = Icons.AutoMirrored.Filled.ExitToApp,
                    label     = "Đăng xuất tài khoản an toàn",
                    tint      = ErrorRed,
                    showArrow = false
                ) {
                    showLogoutDialog = true
                }
            }

            Spacer(Modifier.height(45.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Phiên bản ứng dụng 1.0.4 - UED Build",
                    color = TextSub.copy(0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Hệ thống Quản lý Tài chính Thông minh",
                    color = TextSub.copy(0.4f),
                    fontSize = 10.sp
                )
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SectionTitleHeader(title: String) {
    Text(
        text = title,
        color         = TextSub,
        fontSize      = 12.sp,
        fontWeight    = FontWeight.ExtraBold,
        letterSpacing = 1.2.sp,
        modifier      = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
    )
}

@Composable
private fun ProfileCardContainer(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = BgCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

@Composable
private fun ProfileDividerLine() {
    HorizontalDivider(
        color    = BorderCol,
        modifier = Modifier.padding(horizontal = 20.dp),
        thickness = 1.dp
    )
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(TealLight.copy(0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TealLight,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                color = TextSub,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                color = TextMain,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProfileActionRow(
    icon: ImageVector,
    label: String,
    tint: Color,
    showArrow: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(tint.copy(0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            color      = TextMain,
            fontSize   = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.weight(1f)
        )
        if (showArrow) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSub,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}