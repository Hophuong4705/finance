package com.example.cuoikyltdd.data.remote

import com.example.cuoikyltdd.SharedPrefsHelper
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 🔥 ĐÃ CHỈNH: Gọi hàm getToken() từ SharedPrefsHelper trong MainActivity của bạn
        val token = SharedPrefsHelper.getToken()

        // Nếu không có token thì gửi request gốc (cho Login/Register)
        if (token.isBlank()) {
            return chain.proceed(originalRequest)
        }

        // Nếu có token, "dán" vào Header với đúng định dạng Bearer
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(newRequest)
    }
}