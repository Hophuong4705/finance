package com.example.cuoikyltdd.ui.screens.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cuoikyltdd.data.remote.dto.ExchangeRateResponse

@Composable
fun ExchangeRateBar(exchangeRate: ExchangeRateResponse?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "Tỷ giá USD", style = MaterialTheme.typography.labelMedium)
            Text(
                text = exchangeRate?.let { "${it.usdToVnd} VND" } ?: "Đang cập nhật...",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = "Giá Vàng (SJC)", style = MaterialTheme.typography.labelMedium)
            Text(
                text = exchangeRate?.let { "${it.goldPrice} Tr/Lượng" } ?: "Đang cập nhật...",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}