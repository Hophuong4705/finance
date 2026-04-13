package com.example.cuoikyltdd

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Hàm hỗ trợ hiển thị hộp thoại đăng nhập bằng Vân tay / Khuôn mặt (Biometrics).
 * @param context Context của ứng dụng (phải ép kiểu được về FragmentActivity)
 * @param onSuccess Callback chạy khi quét vân tay đúng
 * @param onError Callback chạy khi có lỗi (chưa cài vân tay, sai vân tay...)
 */
fun authenticateWithBiometrics(
    context: Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    // Jetpack Biometric yêu cầu FragmentActivity để hiển thị UI
    val activity = context as? FragmentActivity
    if (activity == null) {
        onError("Lỗi: Context không phải là FragmentActivity. Hãy chắc chắn MainActivity kế thừa FragmentActivity.")
        return
    }

    val biometricManager = BiometricManager.from(context)
    val canAuthenticate = biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    )

    when (canAuthenticate) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            val executor = ContextCompat.getMainExecutor(context)

            val biometricPrompt = BiometricPrompt(activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        // Bỏ qua lỗi nếu người dùng tự bấm nút Hủy
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            onError("Lỗi: $errString")
                        }
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        // VÂN TAY ĐÚNG -> GỌI HÀM THÀNH CÔNG
                        onSuccess()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        onError("Vân tay không khớp, vui lòng thử lại!")
                    }
                })

            // Cấu hình giao diện hộp thoại Vân tay
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Đăng nhập FinanceMe")
                .setSubtitle("Chạm vào cảm biến vân tay để xác thực")
                .setNegativeButtonText("Sử dụng mật khẩu")
                .build()

            // Hiển thị hộp thoại
            biometricPrompt.authenticate(promptInfo)
        }
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            onError("Thiết bị của bạn không có cảm biến vân tay.")
        }
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            onError("Cảm biến vân tay hiện không khả dụng.")
        }
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            onError("Bạn chưa cài đặt vân tay nào trên thiết bị này.")
        }
        else -> {
            onError("Lỗi không xác định với hệ thống sinh trắc học.")
        }
    }
}