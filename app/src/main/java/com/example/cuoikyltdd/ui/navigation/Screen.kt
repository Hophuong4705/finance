package com.example.cuoikyltdd.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard_screen")
    object Tax : Screen("tax_screen")
    object Report : Screen("report_screen")
}