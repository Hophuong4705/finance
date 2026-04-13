package com.example.cuoikyltdd

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AppNotification(
    val title:   String,
    val message: String,
    val time:    String,
    var isRead:  Boolean = false
)

object AppGlobalState {
    var isLoggedIn    by mutableStateOf(false)
    var userName      by mutableStateOf("Người dùng")
    var userEmail     by mutableStateOf("")
    var userPhone     by mutableStateOf("")
    var savedPassword by mutableStateOf("")
    var joinDate      by mutableStateOf("")
    var useBiometric  by mutableStateOf(false)
    var userAvatarUri by mutableStateOf("")

    val notifications = mutableStateListOf<AppNotification>()

    private lateinit var prefs: SharedPreferences
    private const val KEY_NOTIFS = "saved_notifications"
    private const val MAX_NOTIFS = 100 // Giữ tối đa 100 thông báo để tránh nặng máy

    /** Gọi 1 lần trong MainActivity.onCreate() hoặc lớp BaseApplication */
    fun initNotifications(context: Context) {
        prefs = context.getSharedPreferences("NotifPrefs", Context.MODE_PRIVATE)
        loadFromPrefs()
    }

    fun addNotification(title: String, message: String) {
        // 🔥 NÂNG CẤP: Màng lọc chống Spam (Chặn các thông báo giống hệt nhau bị gọi đúp)
        val isSpam = notifications.take(3).any {
            it.title == title && it.message == message
        }
        if (isSpam) return

        val time = SimpleDateFormat("HH:mm dd/MM", Locale.getDefault()).format(Date())
        val notif = AppNotification(title, message, time, false)

        notifications.add(0, notif)

        // Giới hạn số lượng để tối ưu RAM
        while (notifications.size > MAX_NOTIFS) {
            notifications.removeAt(notifications.size - 1)
        }
        saveToPrefs()
    }

    // Hàm overload hỗ trợ truyền thêm context (dành cho các class ngoài gọi vào)
    fun addNotification(context: Context, title: String, message: String) {
        addNotification(title, message)
    }

    fun markAllRead() {
        var isChanged = false
        notifications.forEachIndexed { i, n ->
            if (!n.isRead) {
                notifications[i] = n.copy(isRead = true)
                isChanged = true
            }
        }
        if (isChanged) saveToPrefs()
    }

    // 🔥 Hàm xóa 1 thông báo và cập nhật xuống bộ nhớ máy ngay lập tức
    fun removeNotification(notif: AppNotification) {
        if (notifications.remove(notif)) {
            saveToPrefs()
        }
    }

    // 🔥 HÀM NÀY DÙNG ĐỂ DỌN RÁC (USER MA HOẶC ẤN NÚT XÓA TẤT CẢ)
    fun clearNotifications() {
        notifications.clear()
        saveToPrefs()
    }

    // ── Persistence (Lưu trữ xuống máy) ──────────────────────────────────────

    private fun saveToPrefs() {
        if (!::prefs.isInitialized) return
        try {
            val arr = JSONArray()
            notifications.forEach { n ->
                val obj = JSONObject().apply {
                    put("title",   n.title)
                    put("message", n.message)
                    put("time",    n.time)
                    put("isRead",  n.isRead)
                }
                arr.put(obj)
            }
            prefs.edit().putString(KEY_NOTIFS, arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadFromPrefs() {
        val json = prefs.getString(KEY_NOTIFS, null) ?: return
        try {
            val arr = JSONArray(json)
            val loaded = mutableListOf<AppNotification>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                loaded.add(
                    AppNotification(
                        title   = obj.getString("title"),
                        message = obj.getString("message"),
                        time    = obj.getString("time"),
                        isRead  = obj.getBoolean("isRead")
                    )
                )
            }
            notifications.clear()
            notifications.addAll(loaded)
        } catch (_: Exception) {
            // Bỏ qua lỗi parse JSON nếu có để app không bị crash
        }
    }
}