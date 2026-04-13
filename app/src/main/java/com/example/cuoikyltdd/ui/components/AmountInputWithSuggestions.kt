package com.example.cuoikyltdd.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Import hàm định dạng số hàng nghìn từ file DashboardScreen của bạn
import com.example.cuoikyltdd.ui.screens.dashboard.ThousandSeparatorTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountInputWithSuggestions(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val BrandGreen  = Color(0xFF66BB6A)
    val BorderColor = Color(0xFFE2E8F0)
    val TextSub     = Color(0xFF6B7A99)

    Column(modifier = modifier) {
        // 1. Ô NHẬP SỐ TIỀN
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                // Chỉ cho phép nhập các ký tự là số, tránh crash khi parse
                if (newValue.all { it.isDigit() }) {
                    onValueChange(newValue)
                }
            },
            label = { Text("Số tiền (VNĐ)") },
            leadingIcon = {
                Text(
                    text = "₫",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandGreen,
                    modifier = Modifier.padding(start = 16.dp, end = 4.dp)
                )
            },
            trailingIcon = {
                // Nút X dấu chéo để xóa nhanh số tiền khi gõ sai
                if (value.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Xóa", tint = TextSub)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandGreen,
                unfocusedBorderColor = BorderColor
            ),
            // Tự động phân cách hàng nghìn (100000 -> 100,000)
            visualTransformation = ThousandSeparatorTransformation()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. DANH SÁCH NÚT CỘNG TIỀN NHANH
        val suggestions = listOf(
            50_000L to "+50K",
            100_000L to "+100K",
            200_000L to "+200K",
            500_000L to "+500K",
            1_000_000L to "+1M",
            5_000_000L to "+5M"
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(suggestions) { (amount, label) ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BrandGreen.copy(alpha = 0.12f),
                    modifier = Modifier.clickable {
                        // Cộng dồn tiền cũ và tiền mới chọn
                        val currentAmt = value.toLongOrNull() ?: 0L
                        val newAmt = currentAmt + amount
                        onValueChange(newAmt.toString())
                    }
                ) {
                    Text(
                        text = label,
                        color = BrandGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}