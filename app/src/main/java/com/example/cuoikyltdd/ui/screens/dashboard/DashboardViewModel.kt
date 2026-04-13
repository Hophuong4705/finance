package com.example.cuoikyltdd.ui.screens.dashboard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuoikyltdd.data.local.entity.TransactionEntity
import com.example.cuoikyltdd.data.remote.dto.ExchangeRateResponse
import com.example.cuoikyltdd.data.repository.FinanceRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: FinanceRepositoryImpl
) : ViewModel() {

    // FIX: Gọi hàm getAllTransactions() thay vì biến allTransactions
    val transactions = repository.getAllTransactions().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _exchangeRate = mutableStateOf<ExchangeRateResponse?>(null)
    val exchangeRate: State<ExchangeRateResponse?> = _exchangeRate

    init {
        fetchRates()
        syncData()
    }

    private fun fetchRates() {
        viewModelScope.launch {
            val result = repository.fetchExchangeRates()
            if (result.isSuccess) {
                _exchangeRate.value = result.getOrNull()
            }
        }
    }

    private fun syncData() {
        viewModelScope.launch {
            repository.syncPendingTransactions()
        }
    }

    // ─── HÀM THÊM GIAO DỊCH MỚI ──────────────────────────────────────────────
    fun addTransaction(amount: Double, note: String, type: String, source: String) {
        viewModelScope.launch {
            // Lấy thời gian hiện tại dưới dạng Long (Timestamp)
            val currentTimestamp = System.currentTimeMillis()

            val newEntity = TransactionEntity(
                amount = amount,
                date = currentTimestamp,
                source = source,
                type = type,
                note = note,
                isSynced = false // Đảm bảo khớp với constructor của Entity
            )
            // Lưu vào DB qua Repository
            repository.addTransaction(newEntity)
        }
    }

    // ─── HÀM XÓA GIAO DỊCH ───────────────────────────────────────────────────
    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }
}