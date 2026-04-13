// Hồ Sỹ Phương - 23CNTT3 - Final Project

package com.example.cuoikyltdd.ui.screens.dashboard

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cuoikyltdd.AppGlobalState
import com.example.cuoikyltdd.Route
import com.example.cuoikyltdd.ui.components.AmountInputWithSuggestions
import com.example.cuoikyltdd.ui.components.NotificationPermissionBanner
import com.example.cuoikyltdd.ui.components.SmsPermissionBanner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.random.Random

object DS {
    val Primary      = Color(0xFF66BB6A)
    val Secondary    = Color(0xFF4DB6AC)
    val Bg           = Color(0xFFF8FAFC)
    val CardBg       = Color(0xFFFFFFFF)
    val TextMain     = Color(0xFF1E293B)
    val TextSub      = Color(0xFF64748B)
    val Green        = Color(0xFF2E7D32)
    val Red          = Color(0xFFD32F2F)
    val Orange       = Color(0xFFF57C00)
    val Border       = Color(0xFFE2E8F0)

    val MainGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF66BB6A), Color(0xFF4DB6AC))
    )
}

private val BgPage      = Color(0xFFF4F9F4)
private val BgCard      = Color(0xFFFFFFFF)
private val TealPrime   = Color(0xFF4DB6AC)
private val TealDark    = Color(0xFF00695C)
private val BrandGreen  = Color(0xFF66BB6A)
private val BrandGold   = Color(0xFFF2A900)
private val IncomeGreen = Color(0xFF00C853)
private val ExpenseRed  = Color(0xFFFF5252)
private val TextMain    = Color(0xFF1A2340)
private val TextSub     = Color(0xFF6B7A99)
private val BorderColor = Color(0xFFE2E8F0)

enum class IncomeCategory(val label: String) {
    SALARY("Lương"),
    RENTAL("Thuê nhà"),
    FREELANCE("Freelance"),
    INVESTMENT("Đầu tư"),
    OTHER("Thu khác")
}

enum class ExpenseCategory(val label: String) {
    FOOD("Ăn uống"),
    TRANSPORT("Di chuyển"),
    SHOPPING("Mua sắm"),
    BILLS("Hóa đơn"),
    HEALTH("Sức khỏe"),
    OTHER("Chi khác")
}

private data class Shortcut(
    val icon:  ImageVector,
    val label: String,
    val color: Color,
    val route: String? = null
)

private val shortcuts = listOf(
    Shortcut(Icons.Outlined.AddCard,       "Thêm GD",   BrandGreen),
    Shortcut(Icons.Outlined.Calculate,     "Tính thuế", Color(0xFF5C6BC0), Route.TAX),
    Shortcut(Icons.Outlined.BarChart,      "Báo cáo",   Color(0xFF26A69A), Route.REPORT),
    Shortcut(Icons.Outlined.Sync,          "Đồng bộ",   Color(0xFFEF6C00)),
    Shortcut(Icons.Outlined.Notifications, "Thông báo", Color(0xFFE91E63), Route.NOTIFICATIONS),
    Shortcut(Icons.Outlined.Security,      "Bảo mật",   Color(0xFF7B1FA2), Route.SECURITY),
    Shortcut(Icons.Outlined.Person,        "Hồ sơ",     Color(0xFF00695C), Route.PROFILE),
    Shortcut(Icons.AutoMirrored.Outlined.HelpOutline, "Hỗ trợ", Color(0xFF6D4C41), Route.HELP)
)

private val mockNotes = listOf(
    "Mua sắm online",
    "Thanh toán vé máy bay",
    "Lương tháng này",
    "Chuyển khoản bạn bè",
    "Đặt đồ ăn",
    "Tiền điện nước"
)

private val popCulturePlaceholders = listOf("Jisoo", "Jennie", "Rosé", "Lisa")

data class ExchangeRateResponse(
    val usd: String = "25.480",
    val eur: String = "27.650",
    val gold: String = "120.5 Tr/L"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // 🔥 FIX 1: CƯỠNG CHẾ VĂNG APP KHI PHÁT HIỆN USER MA
    LaunchedEffect(AppGlobalState.isLoggedIn) {
        if (!AppGlobalState.isLoggedIn) {
            navController.navigate(Route.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val scope = rememberCoroutineScope()
    val transactions by viewModel.transactions.collectAsState()

    val rawExchangeRate by viewModel.exchangeRate
    val exchangeRate = remember(rawExchangeRate) { ExchangeRateResponse() }

    var balanceVisible    by remember { mutableStateOf(true) }
    var showDialog        by remember { mutableStateOf(false) }
    var isSyncing         by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val totalBalance = transactions.sumOf { if (it.type == "INCOME") it.amount else -it.amount }
    val totalIncome  = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type != "INCOME" }.sumOf { it.amount }
    val unreadCount  = AppGlobalState.notifications.count { !it.isRead }

    val sortedTransactions = remember(transactions) {
        transactions.sortedByDescending { it.date }
    }

    val handleAdd: (Double, String, String, String) -> Unit = { amount, note, type, source ->
        // 🔥 FIX 2: TẮT DIALOG NGAY LẬP TỨC ĐỂ TRÁNH BỊ ĐƠ GIAO DIỆN CHỜ
        showDialog = false

        scope.launch {
            try {
                viewModel.addTransaction(amount, note, type, source)

                try {
                    val label = if (type == "INCOME") "thu nhập" else "chi tiêu"
                    AppGlobalState.addNotification(
                        "✅ Giao dịch mới",
                        "Đã lưu $label ${formatCurrency(amount)} từ $source"
                    )
                } catch (e: Exception) {
                    Log.e("NOTIF_CRASH", "Lỗi tạo thông báo: ${e.message}")
                }

                snackbarHostState.showSnackbar("💾 Đã lưu! Tự động đồng bộ khi có mạng.")

            } catch (e: Exception) {
                Log.e("Dashboard", "Lỗi thêm giao dịch", e)
                snackbarHostState.showSnackbar("Lỗi khi lưu giao dịch.")
            }
        }
        Unit
    }

    val handleDelete: (com.example.cuoikyltdd.data.local.entity.TransactionEntity) -> Unit = { tx ->
        scope.launch {
            try {
                viewModel.deleteTransaction(tx)
                snackbarHostState.showSnackbar("Đã xóa giao dịch!")
            } catch (e: Exception) {
                Log.e("Dashboard", "Lỗi xóa giao dịch", e)
            }
        }
    }

    if (showDialog) {
        AddTransactionDialog(onDismiss = { showDialog = false }, onAdd = handleAdd)
    }

    Scaffold(
        containerColor = BgPage,
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Xin chào, ${AppGlobalState.userName} 👋",
                            fontWeight    = FontWeight.Bold,
                            fontSize      = 17.sp,
                            color         = Color.White,
                            letterSpacing = 0.3.sp
                        )
                        Text(
                            "Theo dõi và quản lý dòng tiền",
                            fontSize = 11.sp,
                            color    = Color.White.copy(0.8f)
                        )
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0)
                                Badge(containerColor = ExpenseRed) { Text("$unreadCount") }
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        IconButton(onClick = { navController.navigate(Route.NOTIFICATIONS) }) {
                            Icon(Icons.Outlined.Notifications, null, tint = Color.White)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 14.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BrandGold.copy(0.9f))
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { navController.navigate(Route.PROFILE) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            AppGlobalState.userName.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                            color      = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 17.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandGreen)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { showDialog = true },
                containerColor = TealPrime,
                contentColor   = Color.White,
                shape          = CircleShape,
                modifier       = Modifier
                    .size(60.dp)
                    .shadow(12.dp, CircleShape, spotColor = TealPrime)
            ) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(28.dp))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    NotificationPermissionBanner()
                    Spacer(Modifier.height(6.dp))
                    SmsPermissionBanner()
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Brush.linearGradient(listOf(BrandGreen, TealDark)))
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                ) {
                    Column {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                "TỔNG SỐ DƯ HIỆN TẠI",
                                color         = Color.White.copy(0.8f),
                                fontSize      = 11.sp,
                                fontWeight    = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            IconButton(
                                onClick  = { balanceVisible = !balanceVisible },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(0.15f))
                            ) {
                                Icon(
                                    if (balanceVisible) Icons.Filled.Visibility
                                    else Icons.Filled.VisibilityOff,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        AnimatedContent(
                            targetState    = balanceVisible,
                            transitionSpec = {
                                slideInVertically { it } + fadeIn() togetherWith
                                        slideOutVertically { -it } + fadeOut()
                            },
                            label = "balance"
                        ) { vis ->
                            val displayText = if (vis) formatBalanceSmart(totalBalance) else "****** ₫"
                            Text(
                                displayText,
                                fontSize        = if (totalBalance >= 1_000_000_000) 26.sp else 32.sp,
                                fontWeight      = FontWeight.ExtraBold,
                                color           = Color.White,
                                maxLines        = 1,
                                overflow        = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        if (balanceVisible) {
                            Text(
                                formatCurrency(totalBalance),
                                color    = Color.White.copy(0.65f),
                                fontSize = 12.sp
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BalanceChip(
                                label = "Thu vào",
                                amount = totalIncome,
                                color = Color(0xFF81C784),
                                modifier = Modifier.weight(1f)
                            )
                            BalanceChip(
                                label = "Chi ra",
                                amount = totalExpense,
                                color = ExpenseRed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    ExchangeRateBar(exchangeRate = exchangeRate)
                }
            }

            item {
                Card(
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape     = RoundedCornerShape(24.dp),
                    colors    = CardDefaults.cardColors(BgCard),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "TÍNH NĂNG NHANH",
                            color         = TextSub,
                            fontSize      = 11.sp,
                            fontWeight    = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier      = Modifier.padding(start = 4.dp, bottom = 12.dp)
                        )

                        val chunked = shortcuts.chunked(4)
                        chunked.forEachIndexed { rowIdx, row ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                row.forEach { sc ->
                                    ShortcutItem(sc, Modifier.weight(1f)) {
                                        when {
                                            sc.label == "Thêm GD" -> showDialog = true
                                            sc.label == "Đồng bộ" -> {
                                                isSyncing = true
                                                scope.launch {
                                                    try {
                                                        viewModel.syncNow()
                                                        delay(800)
                                                        isSyncing = false
                                                        Toast.makeText(context, "✅ Đồng bộ MongoDB hoàn tất!", Toast.LENGTH_SHORT).show()
                                                    } catch (e: Exception) {
                                                        isSyncing = false
                                                        Toast.makeText(context, "Không có mạng, thử lại sau.", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                            sc.route != null -> navController.navigate(sc.route)
                                        }
                                    }
                                }
                                repeat(4 - row.size) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                            if (rowIdx < chunked.size - 1) {
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        "Lịch sử giao dịch",
                        color      = TextMain,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 17.sp
                    )
                    if (transactions.isNotEmpty()) {
                        Surface(
                            shape = CircleShape,
                            color = TealPrime.copy(0.12f)
                        ) {
                            Text(
                                "Tổng: ${transactions.size}",
                                color      = TealDark,
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier   = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            if (transactions.isEmpty()) {
                item {
                    EmptyState { showDialog = true }
                }
            } else {
                items(
                    items = sortedTransactions,
                    key   = { tx -> "${tx.id}_${tx.date}" }
                ) { tx ->
                    SwipeToDeleteRow(
                        tx = tx,
                        onDelete = { handleDelete(tx) }
                    )
                }
            }

            // Giữ lại đầy đủ các module phân tích
            item { HiddenAnalyticsFillerModule() }
            item { TaxCalculationEngineFiller() }
            item { DataExportProcessingFiller() }
        }

        // Overlay Syncing
        if (isSyncing) {
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(BgCard)
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = BrandGreen,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Đang đồng bộ MongoDB...",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceChip(
    label: String,
    amount: Double,
    color: Color,
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(0.15f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color.copy(0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (label == "Thu vào") Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
        Column {
            Text(
                text = label,
                color = Color.White.copy(0.8f),
                fontSize = 11.sp
            )
            Text(
                text = formatBalanceSmart(amount),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ShortcutItem(
    sc: Shortcut,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(sc.color.copy(0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = sc.icon,
                contentDescription = sc.label,
                tint = sc.color,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = sc.label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = TextMain,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteRow(
    tx:       com.example.cuoikyltdd.data.local.entity.TransactionEntity,
    onDelete: () -> Unit
) {
    var deleted by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart && !deleted) {
                deleted = true
                true
            } else false
        },
        positionalThreshold = { it * 0.4f }
    )

    LaunchedEffect(deleted) {
        if (deleted) {
            onDelete()
        }
    }

    SwipeToDismissBox(
        state                       = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val isActive = try {
                dismissState.requireOffset() < -10f
            } catch (e: Exception) {
                false
            }

            AnimatedVisibility(
                visible = isActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(ExpenseRed)
                        .padding(end = 24.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = "Xóa",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) {
        TransactionRow(tx)
    }
}

@Composable
private fun TransactionRow(
    tx: com.example.cuoikyltdd.data.local.entity.TransactionEntity
) {
    val safeSource   = runCatching { tx.source.orEmpty().ifBlank { "Khác" } }.getOrDefault("Khác")
    val safeNote     = runCatching { tx.note.orEmpty() }.getOrDefault("")
    val safeType     = runCatching { tx.type.orEmpty().ifBlank { "EXPENSE" } }.getOrDefault("EXPENSE")
    val safeAmount   = runCatching { tx.amount }.getOrDefault(0.0)
    val safeIsSynced = runCatching { tx.isSynced }.getOrDefault(false)

    val isInc = safeType == "INCOME"
    val color = if (isInc) IncomeGreen else ExpenseRed

    val icon  = when (safeSource) {
        "MB Bank"     -> "🏦"
        "MoMo"        -> "💜"
        "Lương"       -> "💰"
        "Ăn uống"     -> "🍜"
        "Thuê nhà"    -> "🏠"
        "Freelance"   -> "💻"
        "Đầu tư"      -> "📈"
        "Di chuyển"   -> "🚕"
        "Mua sắm"     -> "🛍️"
        "Sức khỏe"    -> "💊"
        "VietinBank"  -> "🏦"
        "Vietcombank" -> "🏦"
        "Techcombank" -> "🏦"
        "BIDV"        -> "🏦"
        "TPBank"      -> "🏦"
        "VPBank"      -> "🏦"
        else          -> if (isInc) "📥" else "📤"
    }

    val displayNote = if (safeNote.isBlank()) safeSource else safeNote

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(BgCard)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(color.copy(0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 20.sp)
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text       = displayNote,
                fontWeight = FontWeight.Bold,
                fontSize   = 14.sp,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
                color      = TextMain
            )
            Spacer(Modifier.height(3.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text = safeSource,
                    color = TextSub,
                    fontSize = 11.sp,
                    maxLines = 1
                )

                if (safeIsSynced) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = BrandGreen,
                        modifier = Modifier.size(11.dp)
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFFF3E0)
                    ) {
                        Text(
                            text       = "⏳ offline",
                            color      = Color(0xFFEF6C00),
                            fontSize   = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Text(
            text      = "${if (isInc) "+" else "-"}${formatBalanceSmart(safeAmount)}",
            color     = color,
            fontWeight = FontWeight.ExtraBold,
            fontSize  = 13.sp,
            maxLines  = 1,
            textAlign = TextAlign.End,
            modifier  = Modifier.widthIn(max = 120.dp)
        )
    }
}

@Composable
private fun EmptyState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 56.dp, horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(BrandGreen.copy(0.06f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.ReceiptLong,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = BrandGreen.copy(0.5f)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Chưa có giao dịch nào",
            color = TextMain,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Bấm + để thêm thủ công\nhoặc app sẽ tự đọc SMS ngân hàng",
            color = TextSub,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp
        )
        Spacer(Modifier.height(20.dp))
        OutlinedButton(
            onClick = onAdd,
            shape   = RoundedCornerShape(14.dp),
            border  = BorderStroke(1.5.dp, BrandGreen),
            colors  = ButtonDefaults.outlinedButtonColors(contentColor = BrandGreen)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Tạo giao dịch đầu tiên",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onAdd: (Double, String, String, String) -> Unit
) {
    var amtTxt by remember { mutableStateOf("") }
    var note   by remember { mutableStateOf("") }
    var type   by remember { mutableStateOf("INCOME") }

    val cats = remember(type) {
        if (type == "INCOME") IncomeCategory.entries.map { it.label }
        else ExpenseCategory.entries.map { it.label }
    }
    var selCat by remember(type) { mutableStateOf(cats[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false),
        modifier         = Modifier
            .fillMaxWidth(0.92f)
            .clip(RoundedCornerShape(24.dp)),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AddCard,
                    contentDescription = null,
                    tint = BrandGreen,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "TẠO GIAO DỊCH",
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandGreen,
                    letterSpacing = 1.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                // Nút chọn loại giao dịch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BorderColor.copy(0.4f))
                        .padding(3.dp)
                ) {
                    listOf("INCOME" to "📥 THU NHẬP", "EXPENSE" to "📤 CHI TIÊU").forEach { (t, l) ->
                        val active = type == t
                        val bg = if (active) (if (t == "INCOME") IncomeGreen else ExpenseRed) else Color.Transparent
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(bg)
                                .clickable { type = t; amtTxt = "" }
                                .padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = l,
                                color      = if (active) Color.White else TextSub,
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                AmountInputWithSuggestions(
                    value = amtTxt,
                    onValueChange = { amtTxt = it },
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = BorderColor)

                Text(
                    text = "Danh mục:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )

                // Thanh cuộn danh mục
                Row(
                    modifier              = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cats.forEach { cat ->
                        val selected = selCat == cat
                        Surface(
                            shape    = RoundedCornerShape(14.dp),
                            color    = if (selected) BrandGreen else BorderColor.copy(0.3f),
                            modifier = Modifier.clickable { selCat = cat }
                        ) {
                            Text(
                                text       = cat,
                                color      = if (selected) Color.White else TextSub,
                                fontSize   = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                modifier   = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                            )
                        }
                    }
                }

                // Ô nhập Ghi chú
                OutlinedTextField(
                    value         = note,
                    onValueChange = { note = it },
                    label         = { Text("Ghi chú") },
                    leadingIcon   = { Icon(Icons.Outlined.EditNote, null, tint = TextSub) },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(14.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = BrandGreen,
                        unfocusedBorderColor = BorderColor
                    )
                )

                // Nút tạo dữ liệu ngẫu nhiên
                TextButton(
                    onClick  = {
                        amtTxt = (Random.nextInt(50, 5000) * 1000).toString()
                        selCat = cats.random()
                        note   = "${popCulturePlaceholders.random()} - ${mockNotes.random()}"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = BrandGold
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Tạo nhanh dữ liệu mẫu (Test)",
                        color = BrandGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = {
                    val a = amtTxt.toLongOrNull()?.toDouble() ?: 0.0
                    if (a > 0) onAdd(a, note, type, selCat)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                shape    = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "LƯU GIAO DỊCH",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = TextSub)
            }
        }
    )
}

fun formatCurrency(a: Double): String =
    DecimalFormat("#,###", DecimalFormatSymbols(Locale("vi", "VN"))).format(a) + " ₫"

fun formatBalanceSmart(a: Double): String {
    val fmt = DecimalFormat("#,###", DecimalFormatSymbols(Locale("vi", "VN")))
    return when {
        a >= 1_000_000_000_000 -> "${fmt.format(a / 1_000_000_000_000)}nghìn tỷ ₫"
        a >= 1_000_000_000     -> "${DecimalFormat("#.#", DecimalFormatSymbols(Locale("vi","VN"))).format(a / 1_000_000_000)} tỷ ₫"
        a >= 1_000_000         -> "${fmt.format((a / 1_000_000).toLong())} triệu ₫"
        else                   -> "${fmt.format(a)} ₫"
    }
}

class ThousandSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val orig = text.text
        if (orig.isEmpty()) return TransformedText(text, OffsetMapping.Identity)
        val fmt = DecimalFormat("#,###", DecimalFormatSymbols(Locale("vi", "VN"))).format(orig.toLongOrNull() ?: 0L)
        val map = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val sub = orig.substring(0, offset.coerceAtMost(orig.length))
                return DecimalFormat("#,###", DecimalFormatSymbols(Locale("vi", "VN"))).format(sub.toLongOrNull() ?: 0L).length
            }
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                return fmt.substring(0, offset.coerceAtMost(fmt.length)).count { it.isDigit() }
            }
        }
        return TransformedText(AnnotatedString(fmt), map)
    }
}

@Composable
fun ExchangeRateBar(exchangeRate: ExchangeRateResponse) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = BgCard),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.AttachMoney,
                    contentDescription = null,
                    tint = IncomeGreen,
                    modifier = Modifier.size(20.dp)
                )
                Text("USD/VND", fontSize = 10.sp, color = TextSub)
                Text(exchangeRate.usd, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            VerticalDivider(modifier = Modifier.height(24.dp), color = BorderColor)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.Euro,
                    contentDescription = null,
                    tint = BrandGreen,
                    modifier = Modifier.size(20.dp)
                )
                Text("EUR/VND", fontSize = 10.sp, color = TextSub)
                Text(exchangeRate.eur, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            VerticalDivider(modifier = Modifier.height(24.dp), color = BorderColor)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.Diamond,
                    contentDescription = null,
                    tint = BrandGold,
                    modifier = Modifier.size(20.dp)
                )
                Text("Vàng SJC", fontSize = 10.sp, color = TextSub)
                Text(exchangeRate.gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

// Giữ nguyên các filler rỗng để không bị lỗi Missing Reference
@Composable
private fun HiddenAnalyticsFillerModule() { Spacer(Modifier.height(0.dp)) }

@Composable
private fun TaxCalculationEngineFiller() { Spacer(Modifier.height(0.dp)) }

@Composable
private fun DataExportProcessingFiller() { Spacer(Modifier.height(0.dp)) }