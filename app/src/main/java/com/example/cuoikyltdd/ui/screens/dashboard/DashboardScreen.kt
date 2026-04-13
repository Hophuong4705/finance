package com.example.cuoikyltdd.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cuoikyltdd.AppGlobalState
import com.example.cuoikyltdd.MainTopBar
import com.example.cuoikyltdd.Route
import com.example.cuoikyltdd.ui.screens.dashboard.components.ExchangeRateBar
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

// ─── Palette ──────────────────────────────────────────────────────────────────
private val BgPage      = Color(0xFFF0F4F8)
private val BgCard      = Color(0xFFFFFFFF)
private val TealPrime   = Color(0xFF00BFA5)
private val IncomeGreen = Color(0xFF00C853)
private val ExpenseRed  = Color(0xFFFF5252)
private val TextMain    = Color(0xFF1A2340)
private val TextSub     = Color(0xFF6B7A99)
private val BorderColor = Color(0xFFE2E8F0)

// Shortcut tiện ích trên Dashboard
private data class Shortcut(val icon: ImageVector, val label: String, val color: Color)
private val shortcuts = listOf(
    Shortcut(Icons.Outlined.AddCard,       "Thêm giao dịch", Color(0xFF00BFA5)),
    Shortcut(Icons.Outlined.Calculate,     "Tính thuế",      Color(0xFF5C6BC0)),
    Shortcut(Icons.Outlined.BarChart,      "Báo cáo",        Color(0xFF26A69A)),
    Shortcut(Icons.Outlined.Sync,          "Đồng bộ",        Color(0xFFEF6C00)),
    Shortcut(Icons.Outlined.Notifications, "Thông báo",      Color(0xFFE91E63)),
    Shortcut(Icons.Outlined.Security,      "Bảo mật",        Color(0xFF7B1FA2)),
    Shortcut(Icons.Outlined.Person,        "Hồ sơ",          Color(0xFF00695C)),
    Shortcut(Icons.AutoMirrored.Outlined.HelpOutline, "Hỗ trợ", Color(0xFF6D4C41)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsState()
    val exchangeRate by viewModel.exchangeRate
    var balanceVisible by remember { mutableStateOf(true) }
    var showDialog     by remember { mutableStateOf(false) }

    val totalBalance = transactions.sumOf { if (it.type == "INCOME") it.amount else -it.amount }
    val totalIncome  = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type != "INCOME" }.sumOf { it.amount }

    // ĐÃ FIX: Lấy context và số lượng thông báo từ AppGlobalState
    val context = LocalContext.current
    val unreadCount = AppGlobalState.notifications.count { !it.isRead }

    // ĐÃ FIX: Logic xử lý hộp thoại thêm giao dịch (gọi rung và chuông)
    if (showDialog) {
        AddTransactionDialog(
            onDismiss = { showDialog = false },
            onAdd = { amount, note, type, source ->
                viewModel.addTransaction(amount, note, type, source)

                // Gọi hàm bắn thông báo và rung máy
                AppGlobalState.addNotification(
                    context = context,
                    title = "Giao dịch mới",
                    message = "Đã thêm ${if (type == "INCOME") "thu nhập" else "chi tiêu"} ${formatCurrency(amount)} - $source"
                )
                showDialog = false
            }
        )
    }

    Scaffold(
        containerColor = BgPage,
        topBar = {
            MainTopBar(
                navController     = navController,
                userName          = AppGlobalState.userName, // Cập nhật tên từ GlobalState
                notificationCount = unreadCount
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { showDialog = true },
                containerColor = TealPrime,
                contentColor   = Color.White,
                shape          = CircleShape,
                modifier       = Modifier.size(58.dp).shadow(12.dp, CircleShape, spotColor = TealPrime.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Thêm", modifier = Modifier.size(26.dp))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF00BFA5), Color(0xFF006064))))
                        .padding(horizontal = 20.dp, vertical = 28.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Tổng số dư VND  ›",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 13.sp
                                )
                                Spacer(Modifier.height(6.dp))
                                AnimatedContent(
                                    targetState = balanceVisible,
                                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                                    label = "balance"
                                ) { v ->
                                    if (v) Text(
                                        formatCurrency(totalBalance),
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    ) else Text(
                                        "*** *** VND",
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            IconButton(
                                onClick  = { balanceVisible = !balanceVisible },
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    if (balanceVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null, tint = Color.White
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BalanceChip("Thu nhập", totalIncome,  IncomeGreen, Modifier.weight(1f))
                            BalanceChip("Chi tiêu", totalExpense, ExpenseRed,  Modifier.weight(1f))
                        }
                    }
                }
            }

            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    ExchangeRateBar(exchangeRate = exchangeRate)
                }
            }

            item {
                Card(
                    modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape     = RoundedCornerShape(20.dp),
                    colors    = CardDefaults.cardColors(containerColor = BgCard),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            shortcuts.take(4).forEach { sc ->
                                ShortcutItem(
                                    shortcut     = sc,
                                    modifier     = Modifier.weight(1f),
                                    onClick      = {
                                        when (sc.label) {
                                            "Thêm giao dịch" -> showDialog = true
                                            "Tính thuế"      -> navController.navigate(Route.TAX)
                                            "Báo cáo"        -> navController.navigate(Route.REPORT)
                                            "Thông báo"      -> navController.navigate(Route.NOTIFICATIONS)
                                            "Hồ sơ"          -> navController.navigate(Route.PROFILE)
                                            else -> {}
                                        }
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            shortcuts.drop(4).forEach { sc ->
                                ShortcutItem(
                                    shortcut = sc,
                                    modifier = Modifier.weight(1f),
                                    onClick  = {
                                        when (sc.label) {
                                            "Thông báo" -> navController.navigate(Route.NOTIFICATIONS)
                                            "Hồ sơ"     -> navController.navigate(Route.PROFILE)
                                            else -> {}
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Giao dịch gần đây", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (transactions.isNotEmpty()) {
                        Text("Xem tất cả", color = TealPrime, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (transactions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("💳", fontSize = 44.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Chưa có giao dịch nào", color = TextSub, fontSize = 14.sp)
                        Text("Nhấn + để thêm giao dịch đầu tiên", color = TextSub.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                }
            } else {
                items(transactions) { tx ->
                    TransactionRow(
                        transaction = tx,
                        onDelete    = { viewModel.deleteTransaction(tx) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BalanceChip(label: String, amount: Double, color: Color, modifier: Modifier) {
    val isIncome = color == Color(0xFF00C853)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(30.dp).clip(CircleShape).background(color.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isIncome) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)
            )
        }
        Column {
            Text(label, color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
            Text(formatCurrency(amount), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ShortcutItem(shortcut: Shortcut, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(shortcut.color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(shortcut.icon, contentDescription = shortcut.label, tint = shortcut.color, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(shortcut.label, fontSize = 11.sp, color = TextMain, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Composable
private fun TransactionRow(
    transaction: com.example.cuoikyltdd.data.local.entity.TransactionEntity,
    onDelete: () -> Unit
) {
    val isIncome    = transaction.type == "INCOME"
    val accentColor = if (isIncome) IncomeGreen else ExpenseRed
    val sign        = if (isIncome) "+" else "-"
    val displayTitle = transaction.note.ifBlank { transaction.source }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(CircleShape).background(accentColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(if (isIncome) "↑" else "↓", fontSize = 18.sp, color = accentColor, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(displayTitle, color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(transaction.source, color = TextSub, fontSize = 11.sp)
            Text(transaction.date.toString(), color = TextSub.copy(alpha = 0.7f), fontSize = 10.sp)
        }
        Text("$sign${formatCurrency(transaction.amount)}", color = accentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = ExpenseRed.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onAdd: (Double, String, String, String) -> Unit
) {
    var amountText     by remember { mutableStateOf("") }
    var note           by remember { mutableStateOf("") }
    var type           by remember { mutableStateOf("EXPENSE") }
    val sources        = listOf("Tiền mặt", "MoMo", "Vietcombank", "Lương", "Ví khác")
    var selectedSource by remember { mutableStateOf(sources[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = BgCard,
        shape            = RoundedCornerShape(24.dp),
        title = { Text("Thêm giao dịch", fontWeight = FontWeight.Bold, color = TextMain) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BorderColor)
                        .padding(4.dp)
                ) {
                    listOf("INCOME" to "Thu nhập", "EXPENSE" to "Chi tiêu").forEach { (t, label) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (type == t) if (t == "INCOME") IncomeGreen else ExpenseRed else Color.Transparent)
                                .clickable { type = t }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, color = if (type == t) Color.White else TextSub, fontWeight = if (type == t) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                        }
                    }
                }

                Text("Nguồn tiền:", fontSize = 12.sp, color = TextSub, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sources.forEach { src ->
                        val picked = selectedSource == src
                        val accentColor = if (type == "INCOME") IncomeGreen else ExpenseRed
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (picked) accentColor else BorderColor)
                                .clickable { selectedSource = src }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(src, color = if (picked) Color.White else TextSub, fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value         = amountText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) amountText = it },
                    label         = { Text("Số tiền", fontSize = 13.sp) },
                    suffix        = { Text("₫", fontWeight = FontWeight.Bold) },
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = ThousandSeparatorTransformation(),
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value         = note,
                    onValueChange = { note = it },
                    label         = { Text("Ghi chú (tuỳ chọn)", fontSize = 13.sp) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) onAdd(amount, note, type, selectedSource)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrime),
                shape  = RoundedCornerShape(12.dp)
            ) { Text("Lưu", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy", color = TextSub) }
        }
    )
}

fun formatCurrency(amount: Double): String {
    val f = DecimalFormat("#,###", DecimalFormatSymbols(Locale("vi", "VN")))
    return f.format(amount) + " ₫"
}

class ThousandSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val orig = text.text
        if (orig.isEmpty()) return TransformedText(text, OffsetMapping.Identity)
        val fmt = DecimalFormat("#,###", DecimalFormatSymbols(Locale("vi", "VN"))).format(orig.toLong())
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                return DecimalFormat("#,###", DecimalFormatSymbols(Locale("vi", "VN"))).format(orig.substring(0, offset).toLong()).length
            }
            override fun transformedToOriginal(offset: Int) = orig.length
        }
        return TransformedText(AnnotatedString(fmt), mapping)
    }
}