package com.rentconnect.app

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

private enum class AuthPage { LOGIN, REGISTER, OTP }

@Composable
fun AuthHost() {
    var page by remember { mutableStateOf(AuthPage.LOGIN) }
    when (page) {
        AuthPage.LOGIN -> LoginScreen(onRegister = { page = AuthPage.REGISTER })
        AuthPage.REGISTER -> RegisterScreen(onBack = { page = AuthPage.LOGIN }, onNext = { page = AuthPage.OTP })
        AuthPage.OTP -> OtpScreen(onBack = { page = AuthPage.REGISTER }, onDone = { page = AuthPage.LOGIN })
    }
}

@Composable
fun LoginScreen(onRegister: () -> Unit) {
    val ctx = LocalContext.current
    var contact by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    fun biometric() {
        val act = ctx as? FragmentActivity ?: return
        if (Store.biometricUser == null) {
            error = "Hãy đăng nhập bằng mật khẩu một lần để bật vân tay/khuôn mặt."
            return
        }
        val ok = BiometricManager.from(ctx).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        if (ok != BiometricManager.BIOMETRIC_SUCCESS) {
            error = "Thiết bị chưa có hoặc chưa cài đặt vân tay/khuôn mặt."
            return
        }
        val prompt = BiometricPrompt(act, ContextCompat.getMainExecutor(ctx), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                Store.loginBiometric()?.let { error = it }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                error = errString.toString()
            }
        })
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Đăng nhập RentConnect")
                .setSubtitle("Dùng vân tay hoặc khuôn mặt")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .setNegativeButtonText("Hủy")
                .build()
        )
    }

    Scaffold(topBar = { AppTopBar("Đăng nhập") }) { pad ->
        ScreenColumn(Modifier.padding(pad)) {
            Spacer(Modifier.height(24.dp))
            LabeledField("Email / Số điện thoại", contact, { contact = it }, placeholder = "name@email.com")
            LabeledField("Mật khẩu", pass, { pass = it }, password = true, placeholder = "••••••••")
            if (error.isNotBlank()) InfoBox(error, RedBg, Red)
            PrimaryButton("Đăng nhập", { Store.login(contact, pass)?.let { error = it } })
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(Modifier.weight(1f))
                Text("  hoặc  ", color = Muted)
                HorizontalDivider(Modifier.weight(1f))
            }
            OutlineButton("Vân tay / Khuôn mặt", { biometric() })
            Text("Chưa có tài khoản? Đăng ký", color = Primary, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(12.dp).clickable { onRegister() })
            InfoBox(
                "Tài khoản demo (mật khẩu ${Seed.DEMO_PASSWORD}):\n" +
                    "• Chủ trọ: ${Seed.LANDLORD}\n• Người thuê: ${Seed.TENANT}\n• Admin: ${Seed.ADMIN}",
                AmberBg, Amber
            )
        }
    }
}

@Composable
fun RegisterScreen(onBack: () -> Unit, onNext: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var idNo by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(Role.TENANT) }
    var error by remember { mutableStateOf("") }

    Scaffold(topBar = { AppTopBar("Đăng ký tài khoản", onBack) }) { pad ->
        ScreenColumn(Modifier.padding(pad)) {
            LabeledField("Thông tin cá nhân (họ và tên)", name, { name = it }, placeholder = "Nhập thông tin cá nhân")
            LabeledField("Thông tin định danh (CCCD/CMND)", idNo, { idNo = digits(it).take(12) }, placeholder = "Nhập thông tin định danh",
                keyboard = KeyboardType.Number)
            LabeledField("Email / Số điện thoại", contact, { contact = it }, placeholder = "name@email.com")
            LabeledField("Mật khẩu", pass, { pass = it }, password = true, placeholder = "Tối thiểu 6 ký tự")
            Text("Vai trò mục tiêu", color = Muted, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill("Người thuê (Tenant)", role == Role.TENANT) { role = Role.TENANT }
                Pill("Chủ trọ (Landlord)", role == Role.LANDLORD) { role = Role.LANDLORD }
            }
            if (error.isNotBlank()) InfoBox(error, RedBg, Red)
            PrimaryButton("Đăng ký", {
                val e = Store.startRegister(name, idNo, contact, pass, role)
                if (e == null) onNext() else error = e
            })
            Text("Đã có tài khoản? Đăng nhập", color = Primary, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(12.dp).clickable { onBack() })
        }
    }
}

@Composable
fun OtpScreen(onBack: () -> Unit, onDone: () -> Unit) {
    val ctx = LocalContext.current
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    Scaffold(topBar = { AppTopBar("Xác thực tài khoản", onBack) }) { pad ->
        ScreenColumn(Modifier.padding(pad)) {
            Spacer(Modifier.height(16.dp))
            Text("Nhập mã xác thực (OTP/Email) đã gửi tới email hoặc số điện thoại của bạn.", textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            BasicTextField(
                value = code,
                onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) code = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.focusRequester(focus),
                decorationBox = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(6) { i ->
                            Box(
                                Modifier.weight(1f).height(56.dp).border(1.dp, Color.Gray, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) { Text(code.getOrNull(i)?.toString() ?: "", style = MaterialTheme.typography.headlineSmall) }
                        }
                    }
                }
            )
            if (error.isNotBlank()) InfoBox(error, RedBg, Red)
            PrimaryButton("Xác thực", {
                val e = Store.verifyOtp(code)
                if (e == null) {
                    toast(ctx, "Tạo tài khoản thành công. Hãy đăng nhập.")
                    onDone()
                } else error = e
            })
            InfoBox("Sau khi xác thực thành công, hệ thống khởi tạo tài khoản và phân quyền theo vai trò đã chọn.", AmberBg, Amber)
            if (Store.lastOtp.isNotBlank()) {
                InfoBox("Chế độ demo (chưa nối dịch vụ gửi email/SMS): mã OTP của bạn là ${Store.lastOtp}", Soft, Primary)
            }
        }
    }
}
