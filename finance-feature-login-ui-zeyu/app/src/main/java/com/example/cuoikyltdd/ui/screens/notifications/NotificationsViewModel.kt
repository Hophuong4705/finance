package com.example.cuoikyltdd.ui.screens.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuoikyltdd.data.remote.ApiService
import com.example.cuoikyltdd.data.remote.NotificationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    fun syncDeletedNotifications(dtos: List<NotificationDto>) {
        viewModelScope.launch {
            try {
                apiService.syncNotifications(dtos)
                Log.d("NOTIF_SYNC", "Đã đồng bộ xóa lên Mongoose")
            } catch (e: Exception) {
                Log.e("NOTIF_SYNC", "Lỗi đồng bộ xóa: ${e.message}")
            }
        }
    }

    fun deleteAllNotifications(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteAllNotifications()
                onResult(response.isSuccessful)
            } catch (e: Exception) {
                Log.e("NOTIF_DEL", "Lỗi xóa tất cả: ${e.message}")
                onResult(false)
            }
        }
    }
}