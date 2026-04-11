package com.example.cuoikyltdd.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cuoikyltdd.AppGlobalState
import com.example.cuoikyltdd.Route
import com.example.cuoikyltdd.SharedPrefsHelper
import com.example.cuoikyltdd.authenticateWithBiometrics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── Palette ──────────────────────────────────────────────────────────────────
private val BgPage      = Color(0xFFF0F4F8)
private val BgCard      = Color(0xFFFFFFFF)
private val TealPrime   = Color(0xFF00BFA5)
private val TextMain    = Color(0xFF1A2340)
private val TextSub     = Color(0xFF6B7A99)
private val BorderColor = Color(0xFFE2E8F0)
private val ErrorRed    = Color(0xFFFF5252)

@Composable
fun LoginScreen(navController: NavController) {
    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var pwVisible    by remember { mutableStateOf(false) }
    var errorMsg     by remember { mutableStateOf<String?>(null) }
    var isLoading    by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(BgPage)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(Brush.linearGradient(listOf(Color(0xFF00BFA5), Color(0xFF006064))))
        )

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.AccountBalance,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("FinanceApp", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text("Trợ lý tài chính cá nhân", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))

            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Đăng nhập", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextMain)
                    Text("Chào mừng bạn quay lại!", fontSize = 13.sp, color = TextSub)

                    Spacer(Modifier.height(24.dp))

                    AuthTextField(
                        value = email,
                        onValueChange = { email = it; errorMsg = null },
                        label = "Email hoặc số điện thoại",
                        leadingIcon = Icons.Outlined.Email,
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(Modifier.height(12.dp))

                    AuthTextField(
                        value = password,
                        onValueChange = { password = it; errorMsg = null },
                        label = "Mật khẩu",
                        leadingIcon = Icons.Outlined.Lock,
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        pwVisible = pwVisible,
                        onTogglePw = { pwVisible = !pwVisible }
                    )

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        TextButton(onClick = { }) {
                            Text("Quên mật khẩu?", color = TealPrime, fontSize = 13.sp)
                        }
                    }

                    AnimatedVisibility(visible = errorMsg != null) {
                        errorMsg?.let {
                            Text(it, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                        }
                    }

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMsg = "Vui lòng nhập đầy đủ thông tin"
                            } else if (email == AppGlobalState.userEmail && password == AppGlobalState.savedPassword) {
                                // Mật khẩu đúng -> Lưu Session và vô Dashboard
                                SharedPrefsHelper.saveUserSession(
                                    AppGlobalState.userName,
                                    email,
                                    AppGlobalState.userPhone,
                                    password,
                                    AppGlobalState.joinDate
                                )
                                navController.navigate(Route.DASHBOARD) { popUpTo(0) { inclusive = true } }
                            } else {
                                errorMsg = "Tài khoản hoặc mật khẩu không chính xác!"
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrime)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("ĐĂNG NHẬP", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        }
                    }

                    // HIỂN THỊ NÚT VÂN TAY NẾU ĐÃ BẬT TRONG CÀI ĐẶT
                    if (AppGlobalState.useBiometric && AppGlobalState.userEmail.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HorizontalDivider(Modifier.weight(1f), color = BorderColor)
                            Text("  hoặc  ", fontSize = 12.sp, color = TextSub)
                            HorizontalDivider(Modifier.weight(1f), color = BorderColor)
                        }
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                authenticateWithBiometrics(context,
                                    onSuccess = {
                                        SharedPrefsHelper.saveUserSession(
                                            AppGlobalState.userName,
                                            AppGlobalState.userEmail,
                                            AppGlobalState.userPhone,
                                            AppGlobalState.savedPassword,
                                            AppGlobalState.joinDate
                                        )
                                        navController.navigate(Route.DASHBOARD) { popUpTo(0) { inclusive = true } }
                                    },
                                    onError = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, TealPrime.copy(alpha = 0.4f))
                        ) {
                            Icon(Icons.Outlined.Fingerprint, null, tint = TealPrime, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Đăng nhập bằng vân tay", color = TealPrime, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Chưa có tài khoản?", color = TextSub, fontSize = 14.sp)
                TextButton(onClick = { navController.navigate(Route.REGISTER) }) {
                    Text("Đăng ký ngay", color = TealPrime, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun RegisterScreen(navController: NavController) {
    var fullName     by remember { mutableStateOf("") }
    var email        by remember { mutableStateOf("") }
    var phone        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var confirmPw    by remember { mutableStateOf("") }
    var pwVisible    by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var errorMsg     by remember { mutableStateOf<String?>(null) }
    var agreed       by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(BgPage)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Brush.linearGradient(listOf(Color(0xFF00BFA5), Color(0xFF006064))))
        )

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Tạo tài khoản", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text("Bắt đầu quản lý tài chính ngay hôm nay", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // ĐÃ TRẢ LẠI STEP INDICATOR 3 BƯỚC ĐẦY ĐỦ
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StepDot(1, true, "Thông tin")
                        StepLine()
                        StepDot(2, false, "Mật khẩu")
                        StepLine()
                        StepDot(3, false, "Xác nhận")
                    }

                    Spacer(Modifier.height(24.dp))

                    AuthTextField(
                        value = fullName,
                        onValueChange = { fullName = it; errorMsg = null },
                        label = "Họ và tên đầy đủ",
                        leadingIcon = Icons.Outlined.Person,
                        keyboardType = KeyboardType.Text
                    )
                    Spacer(Modifier.height(12.dp))
                    AuthTextField(
                        value = email,
                        onValueChange = { email = it; errorMsg = null },
                        label = "Email",
                        leadingIcon = Icons.Outlined.Email,
                        keyboardType = KeyboardType.Email
                    )
                    Spacer(Modifier.height(12.dp))
                    AuthTextField(
                        value = phone,
                        onValueChange = { phone = it; errorMsg = null },
                        label = "Số điện thoại",
                        leadingIcon = Icons.Outlined.Phone,
                        keyboardType = KeyboardType.Phone
                    )
                    Spacer(Modifier.height(12.dp))
                    AuthTextField(
                        value = password,
                        onValueChange = { password = it; errorMsg = null },
                        label = "Mật khẩu",
                        leadingIcon = Icons.Outlined.Lock,
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        pwVisible = pwVisible,
                        onTogglePw = { pwVisible = !pwVisible }
                    )
                    Spacer(Modifier.height(12.dp))
                    AuthTextField(
                        value = confirmPw,
                        onValueChange = { confirmPw = it; errorMsg = null },
                        label = "Xác nhận mật khẩu",
                        leadingIcon = Icons.Outlined.LockOpen,
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        pwVisible = confirmVisible,
                        onTogglePw = { confirmVisible = !confirmVisible }
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { agreed = !agreed }
                    ) {
                        Checkbox(
                            checked = agreed,
                            onCheckedChange = { agreed = it },
                            colors = CheckboxDefaults.colors(checkedColor = TealPrime)
                        )
                        Text("Tôi đồng ý với ", fontSize = 13.sp, color = TextSub)
                        Text("Điều khoản & Chính sách", fontSize = 13.sp, color = TealPrime, fontWeight = FontWeight.SemiBold)
                    }

                    AnimatedVisibility(visible = errorMsg != null) {
                        errorMsg?.let {
                            Text(it, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            when {
                                fullName.isBlank() || email.isBlank() || password.isBlank() -> errorMsg = "Vui lòng điền đầy đủ thông tin"
                                password != confirmPw -> errorMsg = "Mật khẩu xác nhận không khớp"
                                !agreed -> errorMsg = "Bạn chưa đồng ý điều khoản"
                                else -> {
                                    // TẠO TÀI KHOẢN THÀNH CÔNG VÀ LƯU VÀO KÉT SẮT (SHARED PREFERENCES)
                                    val realTimeDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                                    SharedPrefsHelper.saveUserSession(fullName, email, phone, password, realTimeDate)

                                    navController.navigate(Route.DASHBOARD) {
                                        popUpTo(0) { inclusive = true } // Về thẳng trang chủ, không back lại được
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrime)
                    ) {
                        Text("TẠO TÀI KHOẢN", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Đã có tài khoản?", color = TextSub, fontSize = 14.sp)
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Đăng nhập", color = TealPrime, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── ĐÃ TRẢ LẠI HÀM VẼ GIAO DIỆN STEPDOT & STEPLINE ─────────────────────────
@Composable
private fun StepDot(num: Int, active: Boolean, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (active) TealPrime else BorderColor),
            contentAlignment = Alignment.Center
        ) {
            Text("$num", color = if (active) Color.White else TextSub, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Text(label, fontSize = 10.sp, color = if (active) TealPrime else TextSub)
    }
}

@Composable
private fun StepLine() {
    Box(modifier = Modifier.width(32.dp).height(2.dp).background(BorderColor))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    pwVisible: Boolean = false,
    onTogglePw: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        leadingIcon = { Icon(leadingIcon, null, tint = TealPrime, modifier = Modifier.size(20.dp)) },
        trailingIcon = if (isPassword) ({
            IconButton(onClick = { onTogglePw?.invoke() }) {
                Icon(
                    if (pwVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = null,
                    tint = TextSub,
                    modifier = Modifier.size(20.dp)
                )
            }
        }) else null,
        visualTransformation = if (isPassword && !pwVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}