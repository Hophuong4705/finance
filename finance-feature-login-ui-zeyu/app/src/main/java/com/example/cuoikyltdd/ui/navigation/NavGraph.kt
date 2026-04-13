package com.example.cuoikyltdd.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cuoikyltdd.ui.screens.dashboard.DashboardScreen
import com.example.cuoikyltdd.ui.screens.report.ReportScreen
import com.example.cuoikyltdd.ui.screens.tax.TaxScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(route = Screen.Tax.route) {
            TaxScreen(navController = navController)
        }
        composable(route = Screen.Report.route) {
            ReportScreen(navController = navController)
        }
    }
}