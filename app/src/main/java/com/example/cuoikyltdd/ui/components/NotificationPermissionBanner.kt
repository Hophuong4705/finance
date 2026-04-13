package com.example.cuoikyltdd.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

@Composable
fun NotificationPermissionBanner() {
    val context = LocalContext.current

    // 1. Kiểm tra phiên bản Android: Nếu dưới 13 (TIRAMISU) thì hệ thống tự cấp quyền, không cần hiện Banner
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return
    }

    // 2. Kiểm tra quyền Thông báo trên Android 13+
    var hasNotificationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // 3. Trình khởi chạy để bật hộp thoại hỏi quyền của hệ thống
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasNotificationPermission = isGranted
        }
    )

    // 4. Nếu ĐÃ CẤP QUYỀN thì ẩn Banner đi
    if (hasNotificationPermission) {
        return
    }

    // 5. GIAO DIỆN KHI CHƯA CẤP QUYỀN (Màu xanh dương)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)), // Nền xanh nhạt
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon chuông thông báo
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF64B5F6).copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Notification Permission",
                    tint = Color(0xFF1976D2) // Xanh dương đậm
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text giải thích
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bật thông báo ứng dụng",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0D47A1)
                )
                Text(
                    text = "Nhận thông báo biến động số dư khi đọc SMS hoặc quét bill thành công.",
                    fontSize = 12.sp,
                    color = Color(0xFF0D47A1).copy(alpha = 0.8f),
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Nút Bật quyền
            Button(
                onClick = {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("BẬT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}