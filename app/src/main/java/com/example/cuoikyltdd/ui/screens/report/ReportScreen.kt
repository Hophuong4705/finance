package com.example.cuoikyltdd.ui.screens.report

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cuoikyltdd.data.local.entity.TransactionEntity
import com.example.cuoikyltdd.ui.screens.dashboard.DashboardViewModel
import com.example.cuoikyltdd.util.NotificationHelper
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

// ─── Palette màu sắc ────────────────────────────────────────────────────────
private val BgPage      = Color(0xFFF0F4F8)
private val BgCard      = Color(0xFFFFFFFF)
private val TealPrime   = Color(0xFF00BFA5)
private val IncomeGreen = Color(0xFF00C853)
private val ExpenseRed  = Color(0xFFFF5252)
private val ExcelGreen  = Color(0xFF1B873A)
private val PdfRed      = Color(0xFFE53935)
private val TextMain    = Color(0xFF1A2340)
private val TextSub     = Color(0xFF6B7A99)
private val BorderColor = Color(0xFFE2E8F0)

// Màu sắc ngẫu nhiên cho biểu đồ
private val ChartColors = listOf(
    Color(0xFF26C6DA), Color(0xFF66BB6A), Color(0xFFFFCA28), Color(0xFFFFA726),
    Color(0xFFFF7043), Color(0xFFEF5350), Color(0xFFAB47BC), Color(0xFF5C6BC0)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    navController: NavController,
    viewModel: ReportViewModel = hiltViewModel(),
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    // 🔥 Bắt buộc để dùng NotificationHelper
    val context = LocalContext.current

    val transactions by dashboardViewModel.transactions.collectAsState()
    var exportFeedback by remember { mutableStateOf<String?>(null) }

    // Quản lý Tab: 0 = Chi tiêu, 1 = Thu nhập, 2 = Tiết kiệm
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Chi phí", "Thu nhập", "Tiết kiệm")

    Scaffold(
        containerColor = BgPage,
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết cá nhân hóa", color = TextMain, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TealPrime)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgCard)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── CỤM CHUYỂN TAB ──────────────────────────────────────────────
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = BgCard,
                contentColor = TealPrime,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = TealPrime, height = 3.dp
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                color = if (selectedTab == index) TealPrime else TextSub,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── KHU VỰC HIỂN THỊ DỮ LIỆU THEO TAB ───────────────────────────
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                when (selectedTab) {
                    0 -> CategoryStatsView(transactions.filter { it.type == "EXPENSE" }, ExpenseRed, "Tổng chi")
                    1 -> CategoryStatsView(transactions.filter { it.type == "INCOME" }, IncomeGreen, "Tổng thu")
                    2 -> SavingsStatsView(transactions)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── KHU VỰC XUẤT FILE BÁO CÁO ───────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    "XUẤT FILE DỮ LIỆU", color = TextSub, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // 🔥 ĐÃ FIX: Chèn logic gọi thông báo khi Xuất CSV
                ExportButton(
                    label = "Xuất dữ liệu Excel (.csv)",
                    description = "Chi tiết tất cả giao dịch",
                    icon = Icons.Outlined.TableChart,
                    accentColor = ExcelGreen,
                    onClick = {
                        viewModel.exportDataToExcel()
                        exportFeedback = "excel"
                        NotificationHelper.pushSimple(context, "Hoàn tất", "Đã lưu file dữ liệu Excel (.csv) thành công!")
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))

                // 🔥 ĐÃ FIX: Chèn logic gọi thông báo khi Xuất PDF
                ExportButton(
                    label = "Xuất tờ khai Thuế (PDF)",
                    description = "Báo cáo tổng hợp thuế",
                    icon = Icons.Outlined.Article,
                    accentColor = PdfRed,
                    onClick = {
                        viewModel.exportDataToPDF()
                        exportFeedback = "pdf"
                        NotificationHelper.pushSimple(context, "Hoàn tất", "Đã xuất tờ khai PDF vào thư mục Downloads!")
                    }
                )

                exportFeedback?.let { type ->
                    Spacer(modifier = Modifier.height(16.dp))
                    val color = if (type == "excel") ExcelGreen else PdfRed
                    Row(
                        modifier = Modifier
                            .fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(color.copy(alpha = 0.08f)).border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✓  Đã lưu vào thư mục máy tính/Downloads", color = color, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ─── COMPONENT: Giao diện Thống kê danh mục (Thu/Chi) giống thiết kế ────────
@Composable
private fun CategoryStatsView(transactions: List<TransactionEntity>, mainColor: Color, label: String) {
    val totalAmount = transactions.sumOf { it.amount }

    // Nhóm giao dịch theo tên (source) và tính tổng tiền
    val groupedData = transactions.groupBy { it.source.ifBlank { "Khác" } }
        .mapValues { it.value.sumOf { tx -> tx.amount } }
        .toList()
        .sortedByDescending { it.second } // Xếp từ cao xuống thấp

    if (totalAmount == 0.0) {
        EmptyDataBanner()
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
            .background(BgCard).padding(20.dp)
    ) {
        // 1. Biểu đồ Bánh Vòng (Donut Chart) & Chú thích
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vẽ biểu đồ
            Box(modifier = Modifier.size(130.dp), contentAlignment = Alignment.Center) {
                DonutChart(data = groupedData.map { it.second.toFloat() }, colors = ChartColors)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(label, color = TextSub, fontSize = 10.sp)
                    Text(formatCurrencyShort(totalAmount), color = TextMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Chú thích các danh mục bên cạnh
            Column(modifier = Modifier.weight(1f)) {
                groupedData.take(4).forEachIndexed { index, pair ->
                    val color = ChartColors[index % ChartColors.size]
                    val percentage = (pair.second / totalAmount * 100)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(pair.first, color = TextMain, fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 1)
                        Text("${String.format(Locale.US, "%.1f", percentage)}%", color = TextSub, fontSize = 11.sp)
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = BorderColor)

        // 2. Danh sách Chi tiết với thanh Progress
        groupedData.forEachIndexed { index, pair ->
            val color = ChartColors[index % ChartColors.size]
            val percentage = (pair.second / totalAmount * 100).toFloat()

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                // Icon tròn lấy chữ cái đầu
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(pair.first.take(1).uppercase(), color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(pair.first, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(formatCurrency(pair.second), color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { percentage / 100f },
                            modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape),
                            color = color, trackColor = BorderColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${String.format(Locale.US, "%.1f", percentage)}%", color = TextSub, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ─── COMPONENT: Thống kê Tiết kiệm ──────────────────────────────────────────
@Composable
private fun SavingsStatsView(transactions: List<TransactionEntity>) {
    val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val balance = totalIncome - totalExpense

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
            .background(BgCard).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Tình hình tích lũy", color = TextSub, fontSize = 13.sp)
        Text(
            formatCurrency(balance),
            fontSize = 32.sp, fontWeight = FontWeight.ExtraBold,
            color = if (balance >= 0) TealPrime else ExpenseRed
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Biểu đồ so sánh đơn giản
        val total = totalIncome + totalExpense
        if (total > 0) {
            val incomeRatio = (totalIncome / total).toFloat()
            Row(modifier = Modifier.fillMaxWidth().height(16.dp).clip(CircleShape)) {
                Box(modifier = Modifier.fillMaxHeight().weight(incomeRatio.coerceAtLeast(0.01f)).background(IncomeGreen))
                Box(modifier = Modifier.fillMaxHeight().weight((1f - incomeRatio).coerceAtLeast(0.01f)).background(ExpenseRed))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Tổng thu", color = TextSub, fontSize = 11.sp)
                    Text(formatCurrency(totalIncome), color = IncomeGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Tổng chi", color = TextSub, fontSize = 11.sp)
                    Text(formatCurrency(totalExpense), color = ExpenseRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// ─── COMPONENT VẼ BIỂU ĐỒ DONUT (CANVAS) ────────────────────────────────────
@Composable
fun DonutChart(data: List<Float>, colors: List<Color>, strokeWidth: Float = 35f) {
    val total = data.sum()
    if (total == 0f) return

    Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
        var startAngle = -90f
        data.forEachIndexed { index, value ->
            val sweepAngle = (value / total) * 360f
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                size = Size(size.width, size.height),
                topLeft = Offset(0f, 0f)
            )
            // Thêm khoảng hở nhỏ giữa các múi
            startAngle += sweepAngle
        }
    }
}

// ─── Helpers ────────────────────────────────────────────────────────────────
private fun formatCurrency(amount: Double): String {
    val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale("vi", "VN")))
    return formatter.format(amount) + " ₫"
}

// Format thu gọn cho vừa cái lỗ giữa vòng tròn (VD: 1.5M, 500K)
private fun formatCurrencyShort(amount: Double): String {
    return when {
        amount >= 1_000_000 -> String.format(Locale.US, "%.1fM", amount / 1_000_000)
        amount >= 1_000 -> String.format(Locale.US, "%.0fK", amount / 1_000)
        else -> "${amount.toInt()} ₫"
    }
}

@Composable
private fun EmptyDataBanner() {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(BgCard).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Chưa có dữ liệu giao dịch", color = TextSub)
    }
}

@Composable
private fun ExportButton(label: String, description: String, icon: ImageVector, accentColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(BgCard).border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(accentColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(description, color = TextSub, fontSize = 12.sp)
        }
    }
}