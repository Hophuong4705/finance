// Hồ Sỹ Phương - 23CNTT3 - Final Project

package com.example.cuoikyltdd.ui.screens.notifications

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cuoikyltdd.AppGlobalState
import com.example.cuoikyltdd.AppNotification
import com.example.cuoikyltdd.data.remote.NotificationDto

// ── BỘ MÀU XANH CÂY NHẠT (LIGHT GREEN) MỚI ────────────────────────────────────
private val DarkGreen   = Color(0xFF2E7D32)
private val BrandGreen  = Color(0xFF66BB6A)
private val BgPage      = Color(0xFFF4F9F4)
private val BgCard      = Color(0xFFFFFFFF)
private val TextMain    = Color(0xFF1A2340)
private val TextSub     = Color(0xFF6B7A99)
private val IncomeGreen = Color(0xFF00C853)
private val ExpenseRed  = Color(0xFFFF5252)
private val OrangeWarn  = Color(0xFFEF6C00)

// Filter tabs
private enum class NotifFilter(val label: String) {
    ALL("Tất cả"), BANK("Ngân hàng"), MANUAL("Thủ công"), SYSTEM("Hệ thống")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationsViewModel = hiltViewModel() // Gọi API qua ViewModel chuẩn MVVM
) {
    val context = LocalContext.current
    val notifications = AppGlobalState.notifications
    var selectedFilter by remember { mutableStateOf(NotifFilter.ALL) }
    var showClearDialog by remember { mutableStateOf(false) }

    // Đánh dấu tất cả đã đọc khi vào màn hình
    LaunchedEffect(Unit) {
        AppGlobalState.markAllRead()
    }

    // Filter logic
    val filtered = remember(notifications.toList(), selectedFilter) {
        when (selectedFilter) {
            NotifFilter.ALL    -> notifications.toList()
            NotifFilter.BANK   -> notifications.filter {
                it.title.contains("MB Bank", ignoreCase = true) ||
                        it.title.contains("MoMo", ignoreCase = true) ||
                        it.title.contains("THÀNH CÔNG")
            }
            NotifFilter.MANUAL -> notifications.filter {
                it.title.contains("Thêm thủ công", ignoreCase = true)
            }
            NotifFilter.SYSTEM -> notifications.filter {
                !it.title.contains("MB Bank", ignoreCase = true) &&
                        !it.title.contains("MoMo", ignoreCase = true) &&
                        !it.title.contains("Thêm thủ công", ignoreCase = true)
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title   = { Text("Xóa tất cả?", fontWeight = FontWeight.Bold) },
            text    = { Text("Toàn bộ lịch sử thông báo trên máy và Cloud sẽ bị xóa sạch.") },
            confirmButton = {
                Button(
                    onClick = {
                        // 1. Xóa ở UI (Local) - ĐÃ SỬA THÀNH HÀM LƯU ĐỒNG BỘ
                        AppGlobalState.clearNotifications()
                        showClearDialog = false

                        // 2. Gọi API Xóa Sạch trên Mongoose qua ViewModel
                        viewModel.deleteAllNotifications { success ->
                            if (success) {
                                Toast.makeText(context, "Đã dọn sạch dữ liệu Cloud", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Lỗi mạng: Không thể xóa trên Cloud", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(ExpenseRed)
                ) { Text("Xóa sạch") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Hủy", color = TextSub) }
            }
        )
    }

    Scaffold(
        containerColor = BgPage,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Thông báo", fontWeight = FontWeight.Bold, color = Color.White)
                        if (notifications.isNotEmpty()) {
                            Text(
                                "${notifications.size} thông báo",
                                color    = Color.White.copy(0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(
                                Icons.Outlined.DeleteSweep,
                                contentDescription = "Xóa tất cả",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkGreen)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // ── Filter tabs ───────────────────────────────────────────────
            if (notifications.isNotEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = selectedFilter.ordinal,
                    containerColor   = BgCard,
                    contentColor     = BrandGreen,
                    edgePadding      = 16.dp,
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedFilter.ordinal]),
                            color = BrandGreen
                        )
                    },
                    divider          = {}
                ) {
                    NotifFilter.entries.forEach { filter ->
                        val count = when (filter) {
                            NotifFilter.ALL    -> notifications.size
                            NotifFilter.BANK   -> notifications.count {
                                it.title.contains("MB Bank", ignoreCase = true) ||
                                        it.title.contains("MoMo", ignoreCase = true) ||
                                        it.title.contains("THÀNH CÔNG")
                            }
                            NotifFilter.MANUAL -> notifications.count {
                                it.title.contains("Thêm thủ công", ignoreCase = true)
                            }
                            NotifFilter.SYSTEM -> notifications.count {
                                !it.title.contains("MB Bank", ignoreCase = true) &&
                                        !it.title.contains("MoMo", ignoreCase = true) &&
                                        !it.title.contains("Thêm thủ công", ignoreCase = true)
                            }
                        }
                        Tab(
                            selected = selectedFilter == filter,
                            onClick  = { selectedFilter = filter },
                            text = {
                                Row(
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        filter.label,
                                        fontSize = 13.sp,
                                        color = if (selectedFilter == filter) BrandGreen else TextSub
                                    )
                                    if (count > 0) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (selectedFilter == filter)
                                                BrandGreen.copy(0.15f)
                                            else
                                                Color(0xFFE2E8F0)
                                        ) {
                                            Text(
                                                "$count",
                                                fontSize = 10.sp,
                                                color    = if (selectedFilter == filter)
                                                    DarkGreen else TextSub,
                                                modifier = Modifier.padding(
                                                    horizontal = 6.dp, vertical = 2.dp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
                HorizontalDivider(color = Color(0xFFE2E8F0))
            }

            // ── Nội dung ──────────────────────────────────────────────────
            if (notifications.isEmpty()) {
                EmptyState()
            } else if (filtered.isEmpty()) {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔍", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Không có thông báo loại này",
                            color    = TextSub,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier       = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        NotifSummaryBar(notifications.toList())
                    }

                    itemsIndexed(
                        items = filtered,
                        key   = { index, notif -> "${index}_${notif.time}" }
                    ) { _, notif ->
                        NotificationCard(
                            notif    = notif,
                            onDelete = {
                                // 1. Xóa ở Local UI - ĐÃ SỬA THÀNH HÀM LƯU ĐỒNG BỘ
                                AppGlobalState.removeNotification(notif)

                                // 2. Gọi API Sync để Mongoose xóa theo
                                val dtos = AppGlobalState.notifications.map {
                                    NotificationDto(it.title, it.message, it.time, it.isRead)
                                }
                                viewModel.syncDeletedNotifications(dtos)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ── Summary bar ───────────────────────────────────────────────────────────────

@Composable
private fun NotifSummaryBar(notifications: List<AppNotification>) {
    val bankCount   = notifications.count {
        it.title.contains("MB Bank", ignoreCase = true) ||
                it.title.contains("MoMo", ignoreCase = true)
    }
    val manualCount = notifications.count {
        it.title.contains("Thêm thủ công", ignoreCase = true)
    }

    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryItem("🏦", "Ngân hàng", bankCount, BrandGreen, Modifier.weight(1f))
        VerticalDivider(modifier = Modifier.height(36.dp), color = Color(0xFFE2E8F0))
        SummaryItem("✏️", "Thủ công", manualCount, OrangeWarn, Modifier.weight(1f))
        VerticalDivider(modifier = Modifier.height(36.dp), color = Color(0xFFE2E8F0))
        SummaryItem("🔔", "Tổng", notifications.size, TextMain, Modifier.weight(1f))
    }
}

@Composable
private fun SummaryItem(
    emoji: String,
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier
) {
    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 20.sp)
        Text(
            "$count",
            color      = color,
            fontWeight = FontWeight.ExtraBold,
            fontSize   = 18.sp
        )
        Text(label, color = TextSub, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

// ── Notification card ─────────────────────────────────────────────────────────

@Composable
private fun NotificationCard(
    notif: AppNotification,
    onDelete: () -> Unit
) {
    val isMBBank  = notif.title.contains("MB Bank", ignoreCase = true)
    val isMoMo    = notif.title.contains("MoMo", ignoreCase = true)
    val isSuccess = notif.title.contains("THÀNH CÔNG") || notif.title.contains("Thêm")
    val isError   = notif.title.contains("THẤT BẠI") || notif.title.contains("Lỗi")
    val isManual  = notif.title.contains("Thêm thủ công", ignoreCase = true)

    val accentColor = when {
        isMBBank  -> BrandGreen
        isMoMo    -> Color(0xFF9C27B0)
        isSuccess -> IncomeGreen
        isError   -> ExpenseRed
        isManual  -> OrangeWarn
        else      -> Color(0xFF5C6BC0)
    }

    val emoji = when {
        isMBBank  -> "🏦"
        isMoMo    -> "💜"
        isSuccess && !isManual -> "✅"
        isError   -> "❌"
        isManual  -> "✏️"
        else      -> "🔔"
    }

    val amountRegex = Regex("([+\\-])?([\\d,.]+)\\s*[₫đd]")
    val amountMatch = amountRegex.find(notif.message)
    val hasAmount   = amountMatch != null

    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(BgCard),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top // Đảm bảo mọi thứ căn theo cạnh trên
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 22.sp)
            }

            Spacer(Modifier.width(12.dp))

            // Cột chứa nội dung chính (Dùng weight(1f) để nó chiếm hết khoảng trống, đẩy nút X sát phải)
            Column(Modifier.weight(1f)) {
                Text(
                    text       = notif.title,
                    color      = TextMain,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text       = notif.message,
                    color      = TextSub,
                    fontSize   = 13.sp,
                    lineHeight = 20.sp
                )

                if (hasAmount) {
                    Spacer(Modifier.height(8.dp))
                    val isIncome = notif.message.contains("+") ||
                            notif.title.contains("thu nhập", ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = (if (isIncome) IncomeGreen else ExpenseRed).copy(0.1f)
                    ) {
                        Text(
                            text       = amountMatch!!.value.trim(),
                            color      = if (isIncome) DarkGreen else ExpenseRed,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Cột chứa Thời gian và Nút X (Đẩy sang phải)
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                IconButton(
                    onClick  = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "Xóa thông báo",
                        tint     = TextSub.copy(0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.height(16.dp)) // Tạo khoảng cách giữa nút X và thời gian
                Text(
                    text  = notif.time,
                    color = TextSub.copy(0.7f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyState() {
    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(BrandGreen.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.NotificationsNone,
                    null,
                    tint     = BrandGreen,
                    modifier = Modifier.size(50.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "Chưa có thông báo",
                color      = TextMain,
                fontWeight = FontWeight.Bold,
                fontSize   = 18.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Khi MB Bank hoặc MoMo gửi\nbiến động số dư, hệ thống sẽ\ncập nhật tự động tại đây.",
                color     = TextSub,
                fontSize  = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}