package com.example.cuoikyltdd.ui.screens.help

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// ── BỘ MÀU CHUẨN CỦA FINANCEME ───────────────────────────────────────────────
private val TealDark = Color(0xFF006064)
private val TealLight = Color(0xFF00BFA5)
private val BgPage = Color(0xFFF0F4F8)
private val TextMain = Color(0xFF1A2340)
private val TextSub = Color(0xFF6B7A99)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trợ giúp & Hỗ trợ", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TealDark,
                    navigationIconContentColor = TealDark
                )
            )
        },
        containerColor = BgPage
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── KHỐI HEADER TRUNG TÂM TRỢ GIÚP ────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(TealLight.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = null,
                        tint = TealDark,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Chúng tôi có thể giúp gì cho bạn?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextMain
                )
                Text(
                    text = "Khám phá các hướng dẫn bên dưới để dùng App tốt hơn",
                    fontSize = 14.sp,
                    color = TextSub,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // ── DANH SÁCH CÂU HỎI THƯỜNG GẶP (FAQ) ────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "CÂU HỎI THƯỜNG GẶP",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealDark,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                FaqItem(
                    question = "Tại sao ứng dụng không đọc được SMS biến động số dư?",
                    answer = "Để tính năng đọc SMS hoạt động, bạn cần:\n1. Cấp quyền đọc SMS trên màn hình chính.\n2. Đảm bảo App được phép chạy ngầm (Không bị hệ thống tối ưu hóa pin tắt mất).\n3. Tin nhắn phải có chứa các từ khóa như 'SD', 'So du', '+', '-' kèm theo VNĐ."
                )

                FaqItem(
                    question = "Tính năng quét hóa đơn (OCR) hoạt động như thế nào?",
                    answer = "Bạn chỉ cần mở tính năng Quét Bill, đưa camera vào phần Tổng tiền của hóa đơn. Ứng dụng sẽ sử dụng AI (Google ML Kit) để tự động nhận diện số tiền lớn nhất và trích xuất thành khoản Chi tiêu."
                )

                FaqItem(
                    question = "Dữ liệu của tôi có được an toàn không?",
                    answer = "Tuyệt đối an toàn. Mọi giao dịch được mã hóa và lưu trữ Offline trên bộ nhớ máy thông qua Room Database. Chỉ khi bạn đăng nhập, dữ liệu mới được đồng bộ lên máy chủ Mongoose."
                )

                FaqItem(
                    question = "Làm sao để xuất báo cáo ra Excel/PDF?",
                    answer = "Trong màn hình Thống kê (Dashboard), hãy tìm biểu tượng Tải xuống ở góc trên. Bạn có thể chọn xuất theo tháng hoặc năm. File sẽ được lưu trong thư mục Download của điện thoại."
                )
            }

            // ── THÔNG TIN LIÊN HỆ ĐỒ ÁN ──────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "THÔNG TIN LIÊN HỆ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealDark,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ContactRow(icon = Icons.Default.Person, title = "Sinh viên thực hiện", value = "Hồ Sỹ Phương")
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = BgPage)
                        ContactRow(icon = Icons.Default.Class, title = "Lớp học phần", value = "23CNTT3")
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = BgPage)
                        ContactRow(icon = Icons.Default.Email, title = "Email hỗ trợ", value = "phuonghs@ued.udn.vn")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Phiên bản ứng dụng
            Text(
                text = "FinanceMe Version 1.0.6",
                color = TextSub.copy(alpha = 0.5f),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// =========================================================================================
// ── COMPONENT TÁI SỬ DỤNG: ITEM CÂU HỎI CÓ THỂ MỞ RỘNG (EXPANDABLE FAQ) ────────────────
// =========================================================================================

@Composable
fun FaqItem(question: String, answer: String) {
    var isExpanded by remember { mutableStateOf(false) }

    // Hiệu ứng xoay icon mũi tên
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = TealLight,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = question,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextMain,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextSub,
                    modifier = Modifier.rotate(rotationAngle)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Text(
                    text = answer,
                    fontSize = 14.sp,
                    color = TextSub,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(top = 12.dp, start = 32.dp)
                )
            }
        }
    }
}

@Composable
fun ContactRow(icon: ImageVector, title: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(BgPage),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = TealDark, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontSize = 12.sp, color = TextSub)
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextMain)
        }
    }
}