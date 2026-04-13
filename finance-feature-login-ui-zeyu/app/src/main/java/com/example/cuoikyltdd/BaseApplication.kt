package com.example.cuoikyltdd

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Khởi tạo SharedPreferences trước để các thành phần khác có thể lấy Token/Data
        SharedPrefsHelper.init(this)

        // 2. Khởi tạo hệ thống thông báo toàn cục
        AppGlobalState.initNotifications(this)

    }
}