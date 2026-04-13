package com.example.cuoikyltdd.ui.screens.tax

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

// ─── Palette màu sắc ──────────────────────────────────────────────────────────
private val BgPage      = Color(0xFFF0F4F8)
private val BgCard      = Color(0xFFFFFFFF)
private val TealPrime   = Color(0xFF00BFA5)
private val IncomeGreen = Color(0xFF00C853)
private val WarningAmber= Color(0xFFFF8F00)
private val DangerRed   = Color(0xFFFF5252)
private val TextMain    = Color(0xFF1A2340)
private val TextSub     = Color(0xFF6B7A99)
private val BorderColor = Color(0xFFE2E8F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxScreen(
    navController: NavController,
    viewModel: TaxViewModel = hiltViewModel()
) {
    var incomeInput by remember { mutableStateOf("") }
    // 🔥 ĐÃ FIX: Chuyển thành số nguyên để dùng nút +/-
    var dependents  by remember { mutableStateOf(0) }
    val taxResult   by viewModel.taxResult.collectAsState()

    Scaffold(
        containerColor = BgPage,
        topBar = {
            TopAppBar(
                title = { Text("Tính Thuế TNCN", color = TextMain, fontWeight = FontWeight.Bold) },
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
            Text("Nhập thu nhập và số người phụ thuộc để tính thuế TNCN dự tính.", color = TextSub, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(20.dp))

            // ── Form nhập & Gợi ý ─────────────────────────────────────────────
            SectionCard(title = "Thông tin thu nhập") {
                Text("Gợi ý nhanh:", fontSize = 11.sp, color = TextSub, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("15000000", "25000000", "45000000", "70000000").forEach { salary ->
                        AssistChip(
                            onClick = { incomeInput = salary },
                            label = { Text(formatCurrency(salary.toDouble())) }
                        )
                    }
                }

                StyledTextField(
                    value = incomeInput,
                    onValueChange = { if (it.all { char -> char.isDigit() }) incomeInput = it },
                    label = "Thu nhập tháng",
                    suffix = "₫",
                    keyboardType = KeyboardType.Number,
                    visualTransformation = ThousandSeparatorTransformation()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 🔥 ĐÃ FIX: Giao diện Tăng/Giảm người phụ thuộc
                Text("Số người phụ thuộc", color = TextSub, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgPage)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { if (dependents > 0) dependents-- },
                        modifier = Modifier.background(BgCard, CircleShape).size(40.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Giảm", tint = DangerRed)
                    }

                    Text(
                        text = "$dependents người",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )

                    IconButton(
                        onClick = { dependents++ },
                        modifier = Modifier.background(BgCard, CircleShape).size(40.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tăng", tint = TealPrime)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val income = incomeInput.toDoubleOrNull() ?: 0.0
                    viewModel.calculateTax(income, dependents) // Truyền thẳng biến Int
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrime)
            ) {
                Text("PHÂN TÍCH THUẾ", fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 1.sp)
            }

            taxResult?.let { result ->
                Spacer(modifier = Modifier.height(24.dp))
                TaxResultCard(result = result)
            }

            Spacer(modifier = Modifier.height(20.dp))
            TaxBracketCard()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ... [Giữ nguyên toàn bộ các component TaxResultCard, TaxBracketCard, ThousandSeparatorTransformation ở dưới] ...
@Composable
private fun TaxResultCard(result: com.example.cuoikyltdd.domain.model.TaxResult) {
    val taxRate = if (result.taxableIncome > 0)
        (result.taxPayable / result.taxableIncome * 100).coerceIn(0.0, 100.0).toFloat()
    else 0f

    val animProgress by animateFloatAsState(
        targetValue   = taxRate / 100f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing), label = ""
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BgCard)
            .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Text("Chi tiết nghĩa vụ thuế", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(16.dp))

        ResultRow("Tổng thu nhập",       result.totalIncome,    TealPrime)
        ResultRow("Tổng giảm trừ",       result.totalDeduction, WarningAmber)
        ResultRow("Thu nhập tính thuế",  result.taxableIncome,  TextMain)

        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = BorderColor.copy(alpha = 0.5f))

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Thuế TNCN phải nộp", color = TextSub, fontSize = 13.sp)
            Text(
                formatCurrency(result.taxPayable),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (result.taxPayable > 0) DangerRed else IncomeGreen,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Thuế suất thực tế: ${String.format(Locale.US, "%.1f", taxRate)}%", color = TextSub, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { animProgress },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                color = if (taxRate > 20) DangerRed else WarningAmber,
                trackColor = BorderColor,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun TaxBracketCard() {
    val brackets = listOf(
        Triple("Đến 5 triệu",      "5%",  Color(0xFF26C6DA)),
        Triple("5 – 10 triệu",     "10%", Color(0xFF66BB6A)),
        Triple("10 – 18 triệu",    "15%", Color(0xFFFFCA28)),
        Triple("18 – 32 triệu",    "20%", Color(0xFFFFA726)),
        Triple("32 – 52 triệu",    "25%", Color(0xFFFF7043)),
        Triple("52 – 80 triệu",    "30%", Color(0xFFEF5350)),
        Triple("Trên 80 triệu",    "35%", Color(0xFFEC407A)),
    )

    SectionCard(title = "Biểu thuế tham khảo") {
        brackets.forEach { (range, rate, color) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(range, color = TextSub, fontSize = 13.sp)
                }
                Text(rate, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

fun formatCurrency(amount: Double): String {
    val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale("vi", "VN")))
    return formatter.format(amount) + " ₫"
}

class ThousandSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) return TransformedText(text, OffsetMapping.Identity)
        val formatted = DecimalFormat("#,###", DecimalFormatSymbols(Locale("vi", "VN"))).format(originalText.toLong())
        val annotatedString = AnnotatedString(formatted)
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val transformedBeforeCursor = DecimalFormat("#,###", DecimalFormatSymbols(Locale("vi", "VN")))
                    .format(originalText.substring(0, offset).toLong())
                return transformedBeforeCursor.length
            }
            override fun transformedToOriginal(offset: Int): Int { return originalText.length }
        }
        return TransformedText(annotatedString, offsetMapping)
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
        Text(title, color = TealPrime, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StyledTextField(
    value: String, onValueChange: (String) -> Unit,
    label: String, suffix: String, keyboardType: KeyboardType,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label   = { Text(label, color = TextSub, fontSize = 13.sp) },
        suffix  = { Text(suffix, color = TextMain, fontWeight = FontWeight.Bold) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TealPrime,
            unfocusedBorderColor = BorderColor
        )
    )
}

@Composable
private fun ResultRow(label: String, amount: Double, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSub, fontSize = 13.sp)
        Text(formatCurrency(amount), color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}