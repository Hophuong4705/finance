package com.example.cuoikyltdd.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── BẢNG MÀU THƯƠNG HIỆU FINANCEME ───────────────────────────────────────────
val TealDark = Color(0xFF006064)
val TealLight = Color(0xFF00BFA5)
val BrandGreen = Color(0xFF66BB6A)
val BgPageLight = Color(0xFFF0F4F8)
val BgPageDark = Color(0xFF121212)

// 🔥 ĐÃ THÊM: Màu chữ cường độ cao để chống mờ nhòe (Slate Colors)
val TextMainLight = Color(0xFF0F172A) // Đen đậm, bao nét
val TextSubLight  = Color(0xFF334155) // Xám vừa, dùng cho chữ gợi ý (Placeholder)
val TextMainDark  = Color(0xFFF8FAFC) // Trắng sáng
val TextSubDark   = Color(0xFFCBD5E1) // Xám sáng

private val DarkColorScheme = darkColorScheme(
    primary = TealLight,
    secondary = BrandGreen,
    tertiary = TealDark,
    background = BgPageDark,
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    // 🔥 Ép màu chữ hiển thị rõ ràng trên nền tối
    onBackground = TextMainDark,
    onSurface = TextMainDark,
    onSurfaceVariant = TextSubDark
)

private val LightColorScheme = lightColorScheme(
    primary = TealDark,
    secondary = BrandGreen,
    tertiary = TealLight,
    background = BgPageLight,
    surface = Color.White,
    onPrimary = Color.White,
    // 🔥 ĐÃ FIX: Ép hệ thống dùng màu đen đậm cho mọi Text mặc định trên nền sáng
    onBackground = TextMainLight,
    onSurface = TextMainLight,
    onSurfaceVariant = TextSubLight
)

@Composable
fun FinanceMeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Đổi thành false để giữ đúng màu Xanh Ngọc của app, không bị ghi đè bởi màu hệ thống Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // 🔥 Đồng bộ màu thanh trạng thái (Status Bar) với màu Primary của App
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}