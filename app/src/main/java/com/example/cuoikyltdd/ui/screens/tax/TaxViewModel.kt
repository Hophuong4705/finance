// Hồ Sỹ Phương - 23CNTT3 - Final Project: FinanceMe

package com.example.cuoikyltdd.ui.screens.tax

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuoikyltdd.domain.model.TaxResult
import com.example.cuoikyltdd.domain.usecase.CalculateTaxUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaxViewModel @Inject constructor(
    private val calculateTaxUseCase: CalculateTaxUseCase
) : ViewModel() {

    // ─── QUẢN LÝ TRẠNG THÁI (STATE) ──────────────────────────────────────────
    private val _taxResult = MutableStateFlow<TaxResult?>(null)
    val taxResult: StateFlow<TaxResult?> = _taxResult.asStateFlow()

    // ─── LOGIC XỬ LÝ ──────────────────────────────────────────────────────────

    /**
     * Hàm thực hiện phân tích và tính toán thuế TNCN.
     * Được bọc trong viewModelScope để đảm bảo an toàn cho luồng UI (Main Thread),
     * tránh giật lag nếu thuật toán phức tạp hoặc sau này cần gọi API cập nhật luật thuế.
     */
    fun calculateTax(income: Double, dependents: Int) {
        viewModelScope.launch {
            try {
                val result = calculateTaxUseCase(
                    monthlyIncome = income,
                    numberOfDependents = dependents
                )
                _taxResult.value = result
            } catch (e: Exception) {
                e.printStackTrace()
                // Có thể thêm logic xử lý thông báo lỗi (Error State) ở đây nếu cần thiết
            }
        }
    }

    /**
     * Xóa dữ liệu kết quả (Hữu ích khi người dùng muốn làm lại từ đầu)
     */
    fun resetCalculation() {
        _taxResult.value = null
    }
}