package com.example.cuoikyltdd.ui.screens.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import com.example.cuoikyltdd.SharedPrefsHelper // 🔥 ĐÃ THÊM: Import để fix lỗi Unresolved reference
import com.example.cuoikyltdd.data.remote.dto.RegisterRequest
import com.example.cuoikyltdd.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

// ── BỘ MÀU SẮC GIAO DIỆN CHUẨN ───────────────────────────────────────────────
private val TealDark  = Color(0xFF006064)
private val TealLight = Color(0xFF00BFA5)
private val BgPage    = Color(0xFFF0F4F8)
private val TextSub   = Color(0xFF6B7A99)
private val ErrorRed  = Color(0xFFE53935)

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ── CÁC BIẾN QUẢN LÝ TRẠNG THÁI NHẬP LIỆU ─────────────────────────────────
    var name         by remember { mutableStateOf("") }
    var email        by remember { mutableStateOf("") }
    var phone        by remember { mutableStateOf("") }
    var pass         by remember { mutableStateOf("") }
    var confirmPass  by remember { mutableStateOf("") }
    var passVisible  by remember { mutableStateOf(false) }
    var errorMsg     by remember { mutableStateOf<String?>(null) }
    var isLoading    by remember { mutableStateOf(false) }

    // ── HÀM KIỂM TRA TÍNH HỢP LỆ CỦA DỮ LIỆU (VALIDATION) ────────────────────
    fun validate(): Boolean {
        errorMsg = when {
            name.isBlank()              -> "Vui lòng nhập họ tên"
            email.isBlank()             -> "Vui lòng nhập email"
            pass.length < 6             -> "Mật khẩu phải có ít nhất 6 ký tự"
            pass != confirmPass         -> "Mật khẩu xác nhận không khớp"
            else                        -> null
        }
        return errorMsg == null
    }

    // ── HÀM XỬ LÝ ĐĂNG KÝ VÀ BẮN DỮ LIỆU LÊN MONGOOSE ─────────────────────────
    fun doRegister() {
        if (!validate()) return

        isLoading = true
        errorMsg = null

        scope.launch {
            try {
                val registerRequest = RegisterRequest(
                    name = name,
                    email = email,
                    phone = phone,
                    password = pass
                )

                val response = viewModel.apiService.register(registerRequest)

                if (response.isSuccessful) {
                    Toast.makeText(context, "Đăng ký thành công! Hãy đăng nhập lại.", Toast.LENGTH_LONG).show()

                    // 🔥 ĐÃ FIX: Giờ đây SharedPrefsHelper đã được nhận diện
                    SharedPrefsHelper.saveLastEmail(email)

                    navController.navigate(Route.LOGIN) {
                        popUpTo(Route.REGISTER) { inclusive = true }
                    }
                } else {
                    errorMsg = "Lỗi từ Mongoose: Email đã tồn tại hoặc dữ liệu sai!"
                }

            } catch (e: Exception) {
                Log.e("RegisterScreen", "Lỗi kết nối mạng khi Đăng ký", e)
                errorMsg = "Lỗi kết nối mạng: Không gọi được Server!"
            } finally {
                isLoading = false
            }
        }
    }

    // ── GIAO DIỆN CHÍNH CỦA MÀN HÌNH ĐĂNG KÝ ──────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPage)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Brush.linearGradient(listOf(TealDark, TealLight)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            Text(
                text = "FinanceMe",
                color      = Color.White,
                fontSize   = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Tạo tài khoản mới trên hệ thống",
                color    = Color.White.copy(alpha = 0.75f),
                fontSize = 14.sp
            )

            Spacer(Modifier.height(28.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .shadow(16.dp, RoundedCornerShape(24.dp)),
                shape  = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(TealDark, TealLight))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    RegisterField(
                        value         = name,
                        onValueChange = { name = it },
                        label         = "Họ và tên",
                        icon          = Icons.Default.Person
                    )

                    RegisterField(
                        value         = email,
                        onValueChange = { email = it; errorMsg = null },
                        label         = "Email đăng nhập",
                        icon          = Icons.Default.Email,
                        keyboardType  = KeyboardType.Email
                    )

                    RegisterField(
                        value         = phone,
                        onValueChange = { phone = it },
                        label         = "Số điện thoại",
                        icon          = Icons.Default.Phone,
                        keyboardType  = KeyboardType.Phone
                    )

                    OutlinedTextField(
                        value         = pass,
                        onValueChange = { pass = it; errorMsg = null },
                        label         = { Text("Mật khẩu") },
                        leadingIcon   = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = TealLight)
                        },
                        trailingIcon  = {
                            IconButton(onClick = { passVisible = !passVisible }) {
                                Icon(
                                    imageVector = if (passVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextSub
                                )
                            }
                        },
                        visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = TealLight,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    OutlinedTextField(
                        value         = confirmPass,
                        onValueChange = { confirmPass = it; errorMsg = null },
                        label         = { Text("Xác nhận mật khẩu") },
                        leadingIcon   = {
                            Icon(Icons.Default.LockOpen, contentDescription = null, tint = TealLight)
                        },
                        visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = TealLight,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    AnimatedVisibility(visible = errorMsg != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ErrorRed.copy(alpha = 0.08f)
                        ) {
                            Text(
                                text = errorMsg ?: "",
                                color = ErrorRed,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }

                    Button(
                        onClick = { if (!isLoading) doRegister() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape  = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealLight)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text          = "TẠO TÀI KHOẢN MỚI",
                                fontWeight    = FontWeight.Bold,
                                fontSize      = 15.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Đã có tài khoản? ", color = TextSub, fontSize = 13.sp)
                        TextButton(
                            onClick = { navController.popBackStack() },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Đăng nhập", color = TealLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RegisterField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label) },
        leadingIcon   = {
            Icon(icon, contentDescription = null, tint = TealLight)
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = TealLight,
            unfocusedBorderColor = Color(0xFFE2E8F0)
        )
    )
}