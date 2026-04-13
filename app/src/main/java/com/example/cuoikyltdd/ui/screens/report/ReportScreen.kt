package com.example.cuoikyltdd.ui.screens.report

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cuoikyltdd.ui.screens.dashboard.DashboardViewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    navController: NavController,
    viewModel: ReportViewModel = hiltViewModel(),
    dashboardViewModel: DashboardViewModel = hiltViewModel() // Lấy dữ liệu giao dịch
) {
    val transactions by dashboardViewModel.transactions.collectAsState()
    var exportFeedback by remember { mutableStateOf<String?>(null) }

    // Tính toán dữ liệu thực tế cho biểu đồ
    val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val total = totalIncome + totalExpense

    Scaffold(
        containerColor = BgPage,
        topBar = {
            TopAppBar(
                title = { Text("Báo cáo & Xuất file", color = TextMain, fontWeight = FontWeight.Bold) },
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
                .padding(20.dp)
        ) {

            // ── PHẦN BIỂU ĐỒ THỰC TẾ ────────────────────────────────────────
            SectionCard(title = "Thống kê tỷ lệ") {
                if (total > 0) {
                    val incomeWeight = (totalIncome / total).toFloat()

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Thu nhập", color = IncomeGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Chi tiêu", color = ExpenseRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Thanh biểu đồ tỷ lệ
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(CircleShape)
                                .background(BorderColor)
                        ) {
                            Box(modifier = Modifier.fillMaxHeight().weight(incomeWeight.coerceAtLeast(0.01f)).background(IncomeGreen))
                            Box(modifier = Modifier.fillMaxHeight().weight((1f - incomeWeight).coerceAtLeast(0.01f)).background(ExpenseRed))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Hiển thị số tiền đã định dạng
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Tổng thu", color = TextSub, fontSize = 11.sp)
                                Text(formatCurrency(totalIncome), color = TextMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Tổng chi", color = TextSub, fontSize = 11.sp)
                                Text(formatCurrency(totalExpense), color = TextMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                } else {
                    InfoBanner(
                        message = "Chưa có dữ liệu giao dịch. Hãy thêm thu nhập hoặc chi tiêu để xem biểu đồ.",
                        color = TealPrime
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── XUẤT BÁO CÁO ─────────────────────────────────────────────────
            Text(
                "XUẤT FILE DỮ LIỆU",
                color = TextSub,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            ExportButton(
                label       = "Xuất dữ liệu Excel (.xlsx)",
                description = "Danh sách chi tiết tất cả giao dịch",
                icon        = Icons.Outlined.TableChart,
                accentColor = ExcelGreen,
                onClick     = {
                    viewModel.exportDataToExcel()
                    exportFeedback = "excel"
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            ExportButton(
                label       = "Xuất tờ khai Thuế TNCN (PDF)",
                description = "Báo cáo tổng hợp thuế dự tính",
                icon        = Icons.Outlined.Article,
                accentColor = PdfRed,
                onClick     = {
                    viewModel.exportDataToPDF()
                    exportFeedback = "pdf"
                }
            )

            // ── Phản hồi khi xuất file ──────────────────────────────────────
            exportFeedback?.let { type ->
                Spacer(modifier = Modifier.height(16.dp))
                val color = if (type == "excel") ExcelGreen else PdfRed
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.08f))
                        .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✓  File đang được lưu vào thư mục Downloads", color = color, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── HƯỚNG DẪN ───────────────────────────────────────────────────
            SectionCard(title = "Lưu ý") {
                GuideRow("📊", "Dữ liệu được lấy trực tiếp từ các giao dịch bạn đã nhập.")
                GuideRow("📁", "Bạn có thể mở file Excel bằng Google Sheets hoặc Microsoft Excel.")
                GuideRow("🧾", "File PDF được định dạng để dễ dàng in ấn và lưu trữ.")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─── HELPER: Định dạng tiền tệ 10.000 ₫ ───────────────────────────────────────
private fun formatCurrency(amount: Double): String {
    val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale("vi", "VN")))
    return formatter.format(amount) + " ₫"
}

// ─── Composable phụ ──────────────────────────────────────────────────────────
@Composable
private fun InfoBanner(message: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("ℹ️", fontSize = 20.sp)
        Text(message, color = color, fontSize = 13.sp, lineHeight = 20.sp)
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(BgCard)
            .border(1.dp, BorderColor, RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Text(title, color = TealPrime, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun GuideRow(emoji: String, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(emoji, fontSize = 18.sp)
        Text(text, color = TextSub, fontSize = 13.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun ExportButton(
    label: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accentColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label,       color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(description, color = TextSub,  fontSize = 12.sp)
        }
        Text("→", color = accentColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}