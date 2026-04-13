
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

private val DarkColorScheme = darkColorScheme(
    primary = TealLight,
    secondary = BrandGreen,
    tertiary = TealDark,
    background = BgPageDark,
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = TealDark,
    secondary = BrandGreen,
    tertiary = TealLight,
    background = BgPageLight,
    surface = Color.White,
    onPrimary = Color.White
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

            // Ép màu chữ trên thanh trạng thái (giờ, pin, wifi) luôn là màu trắng (để nổi bật trên nền Xanh ngọc)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Lấy từ file Type.kt có sẵn của bạn
        content = content
    )
}