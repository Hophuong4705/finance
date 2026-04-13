package com.example.cuoikyltdd.ui.screens.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuoikyltdd.AppGlobalState
import com.example.cuoikyltdd.Route
import com.example.cuoikyltdd.SharedPrefsHelper // Gọi từ MainActivity
import com.example.cuoikyltdd.executeBiometricAuth
import com.example.cuoikyltdd.data.remote.dto.LoginRequest
import com.example.cuoikyltdd.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

// ── BỘ MÀU CHỦ ĐẠO (UED STYLE) ───────────────────────────────────────────────
private val TealDark  = Color(0xFF006064)
private val TealLight = Color(0xFF00BFA5)
private val BgPage    = Color(0xFFF0F4F8)
private val TextMain  = Color(0xFF1A2340)
private val TextSub   = Color(0xFF6B7A99)
private val ErrorRed  = Color(0xFFE53935)

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    // Khởi tạo state với Email đăng nhập gần nhất
    var email       by remember { mutableStateOf<String>(SharedPrefsHelper.getLastEmail()) }
    var pass        by remember { mutableStateOf("") }
    var passVisible by remember { mutableStateOf(false) }
    var errorMsg    by remember { mutableStateOf<String?>(null) }
    var isLoading   by remember { mutableStateOf(false) }

    // Kiểm tra điều kiện hiện nút Vân tay
    val canUseBiometric = email.isNotBlank() &&
            email == SharedPrefsHelper.getLastEmail() &&
            AppGlobalState.useBiometric

    // ── HÀM XỬ LÝ ĐĂNG NHẬP CHÍNH ─────────────────────────────────────────────
    fun executeLoginProcess() {
        if (email.isBlank() || !email.contains("@")) {
            errorMsg = "Vui lòng nhập Email hợp lệ!"
            return
        }
        if (pass.isBlank()) {
            errorMsg = "Mật khẩu không được để trống!"
            return
        }

        isLoading = true
        errorMsg = null

        scope.launch {
            try {
                // 1. THỬ ĐĂNG NHẬP ONLINE (Gọi API Mongoose)
                val loginRequest = LoginRequest(email = email, password = pass)
                val response = viewModel.apiService.login(loginRequest)

                if (response.isSuccessful && response.body() != null) {
                    val body  = response.body()!!
                    val token = body.token ?: ""
                    val realName = body.name ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }

                    // Lưu session mới nhất vào máy
                    SharedPrefsHelper.saveUserSession(realName, email, token)

                    // Cập nhật trạng thái Global
                    AppGlobalState.isLoggedIn = true
                    AppGlobalState.userName   = realName
                    AppGlobalState.userEmail  = email

                    Toast.makeText(context, "Chào mừng $realName đã trở lại!", Toast.LENGTH_SHORT).show()
                    navController.navigate(Route.DASHBOARD) {
                        popUpTo(Route.LOGIN) { inclusive = true }
                    }
                } else if (response.code() == 401 || response.code() == 404) {
                    // ❌ SERVER BÁO SAI: Không cho phép vào app (Kể cả offline)
                    errorMsg = "Tài khoản không tồn tại hoặc sai mật khẩu!"
                } else {
                    errorMsg = "Lỗi máy chủ (${response.code()}). Vui lòng thử lại!"
                }
            } catch (e: Exception) {
                Log.e("AUTH_ERROR", "Lỗi mạng hoặc Server sập", e)

                // ⚠️ CHẾ ĐỘ OFFLINE: Chỉ cho vào nếu Email khớp với tài khoản đã lưu thành công trước đó
                val savedEmail = SharedPrefsHelper.getLastEmail()
                if (savedEmail.isNotBlank() && email == savedEmail) {

                    // 🔥 FIX: Nạp lại toàn bộ dữ liệu từ SharedPrefs vào AppGlobalState
                    SharedPrefsHelper.loadUserSession(email)

                    Toast.makeText(context, "⚡ Đang chạy chế độ Ngoại tuyến", Toast.LENGTH_LONG).show()
                    navController.navigate(Route.DASHBOARD) {
                        popUpTo(Route.LOGIN) { inclusive = true }
                    }
                } else {
                    errorMsg = "Mất kết nối! Không thể xác thực tài khoản lần đầu."
                }
            } finally {
                isLoading = false
            }
        }
    }

    // ── GIAO DIỆN NGƯỜI DÙNG (UI) ─────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize().background(BgPage)) {
        // Header Gradient
        Box(
            modifier = Modifier.fillMaxWidth().height(320.dp)
                .background(Brush.verticalGradient(listOf(TealDark, TealLight)))
        )

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            Text("FinanceMe", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
            Text("QUẢN LÝ THUẾ & CHI TIÊU — UED", color = Color.White.copy(0.8f), fontSize = 13.sp)

            Spacer(Modifier.height(45.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).shadow(20.dp, RoundedCornerShape(32.dp)),
                shape  = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                    // Avatar Placeholder
                    Box(
                        modifier = Modifier.size(90.dp).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(TealDark, TealLight))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (email.isNotBlank()) email.first().uppercaseChar().toString() else "U",
                            color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(Modifier.height(25.dp))

                    // Ô nhập Email
                    OutlinedTextField(
                        value = email, onValueChange = { email = it; errorMsg = null },
                        label = { Text("Địa chỉ Email") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = TealLight) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealLight)
                    )

                    Spacer(Modifier.height(18.dp))

                    // Ô nhập Mật khẩu
                    OutlinedTextField(
                        value = pass, onValueChange = { pass = it; errorMsg = null },
                        label = { Text("Mật khẩu") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = TealLight) },
                        trailingIcon = {
                            IconButton(onClick = { passVisible = !passVisible }) {
                                Icon(if (passVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                            }
                        },
                        visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealLight)
                    )

                    // Hiển thị thông báo lỗi (nếu có)
                    AnimatedVisibility(visible = errorMsg != null) {
                        Text(text = errorMsg ?: "", color = ErrorRed, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
                    }

                    Spacer(Modifier.height(30.dp))

                    // Nút Đăng nhập
                    Button(
                        onClick = { if (!isLoading) executeLoginProcess() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealLight)
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("XÁC THỰC", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                    }

                    // Nút Vân tay (Chỉ hiện khi đủ điều kiện)
                    if (canUseBiometric) {
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                executeBiometricAuth(
                                    context = context,
                                    onSuccess = {
                                        // Nạp lại dữ liệu cũ khi quét vân tay thành công
                                        SharedPrefsHelper.loadUserSession(email)
                                        navController.navigate(Route.DASHBOARD) {
                                            popUpTo(Route.LOGIN) { inclusive = true }
                                        }
                                    },
                                    onError = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp), border = BorderStroke(1.5.dp, TealLight)
                        ) {
                            Icon(Icons.Default.Fingerprint, null, tint = TealLight)
                            Spacer(Modifier.width(8.dp))
                            Text("Đăng nhập nhanh", color = TealLight, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(25.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Chưa có tài khoản? ", color = TextSub, fontSize = 14.sp)
                        TextButton(onClick = { navController.navigate(Route.REGISTER) }) {
                            Text("Đăng ký", color = TealLight, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
            Spacer(Modifier.height(60.dp))
        }
    }
}