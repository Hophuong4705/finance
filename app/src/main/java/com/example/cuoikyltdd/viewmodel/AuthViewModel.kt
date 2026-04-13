// Hồ Sỹ Phương - 23CNTT3 - Final Project

package com.example.cuoikyltdd.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.cuoikyltdd.AppGlobalState
import com.example.cuoikyltdd.Route
import com.example.cuoikyltdd.SharedPrefsHelper
import com.example.cuoikyltdd.data.remote.ApiService
import com.example.cuoikyltdd.data.repository.FinanceRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    // 1. ApiService dùng để gọi Đăng nhập/Đăng ký
    val apiService: ApiService,
    // 2. Inject thêm Repository để có thể gọi lệnh xóa sạch DB Local (Room)
    private val repository: FinanceRepositoryImpl
) : ViewModel() {

    /**
     * 🔥 HÀM QUAN TRỌNG: XỬ LÝ ĐUỔI NGƯỜI DÙNG (FORCE LOGOUT)
     * Dùng khi nhận được mã lỗi 401 từ Server (Tài khoản bị xóa hoặc Token hết hạn)
     */
    fun handleForceLogout(navController: NavController) {
        viewModelScope.launch {
            try {
                // Bước 1: Quét sạch mọi giao dịch trong Room DB (Xóa dữ liệu ma)
                repository.clearAllLocalData()

                // Bước 2: Xóa thông tin đăng nhập trong SharedPrefs (Xóa Token/Email)
                SharedPrefsHelper.logout()

                // Bước 3: Đưa trạng thái Global về mặc định
                AppGlobalState.isLoggedIn = false
                AppGlobalState.userName = "Người dùng"
                AppGlobalState.userEmail = ""

                // Bước 4: Đá người dùng về màn hình Login và xóa sạch lịch sử quay lại
                navController.navigate(Route.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            } catch (e: Exception) {
                // Nếu có lỗi khi xóa DB, vẫn cố gắng đẩy về màn hình Login
                navController.navigate(Route.LOGIN)
            }
        }
    }

    /**
     * Kiểm tra trạng thái đăng nhập nhanh từ máy
     */
    fun checkLocalSession(): Boolean {
        return SharedPrefsHelper.getToken().isNotEmpty()
    }
}