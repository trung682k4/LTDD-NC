@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.rentconnect.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------- màu sắc theo wireframe ----------
val Primary = Color(0xFF3F5BD9)
val Soft = Color(0xFFE5EBFC)
val Green = Color(0xFF2E8B57)
val Red = Color(0xFFC0392B)
val Amber = Color(0xFFB9770E)
val AmberBg = Color(0xFFFFF3D6)
val RedBg = Color(0xFFFBE4E1)
val Muted = Color(0xFF777777)
val FieldBg = Color(0xFFEAEAEA)

@Composable
fun RentTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Primary, onPrimary = Color.White,
            primaryContainer = Soft, onPrimaryContainer = Primary,
            secondaryContainer = Soft, onSecondaryContainer = Primary,
            background = Color.White, surface = Color.White
        ),
        content = content
    )
}

// ---------- định dạng ----------
fun formatMoney(v: Long): String = "%,d".format(Locale("vi", "VN"), v) + " đ"
fun formatTime(ts: Long): String = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale("vi", "VN")).format(Date(ts))
fun digits(s: String): String = s.filter { it.isDigit() }
fun toLong(s: String): Long = digits(s).take(12).toLongOrNull() ?: 0L

// ---------- khung màn hình ----------
@Composable
fun AppTopBar(title: String, onBack: (() -> Unit)? = null, actions: @Composable () -> Unit = {}) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        navigationIcon = {
            if (onBack != null) IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Soft)
    )
}

@Composable
fun ScreenColumn(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) { content() }
}

// ---------- ô nhập ----------
@Composable
fun LabeledField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboard: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    password: Boolean = false
) {
    Column(modifier) {
        Text(label, color = Muted, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value, onValueChange = onChange, enabled = enabled,
            placeholder = { Text(placeholder, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine, minLines = minLines,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboard),
            visualTransformation = if (password) androidx.compose.ui.text.input.PasswordVisualTransformation()
            else androidx.compose.ui.text.input.VisualTransformation.None
        )
    }
}

@Composable
fun DropdownField(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit,
                  modifier: Modifier = Modifier, placeholder: String = "Chọn") {
    var open by remember { mutableStateOf(false) }
    Column(modifier) {
        Text(label, color = Muted, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(bottom = 4.dp))
        Box {
            OutlinedButton(
                onClick = { open = true }, modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color.Gray)
            ) {
                Text(selected.ifBlank { placeholder }, color = if (selected.isBlank()) Color.Gray else Color.Black,
                    modifier = Modifier.weight(1f), maxLines = 1)
                Text("▾", color = Color.Gray)
            }
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                options.forEach { o ->
                    DropdownMenuItem(text = { Text(o) }, onClick = { onSelect(o); open = false })
                }
            }
        }
    }
}

// ---------- nút ----------
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = Primary, enabled: Boolean = true) {
    Button(
        onClick = onClick, enabled = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) { Text(text, fontWeight = FontWeight.Bold) }
}

@Composable
fun OutlineButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = Primary) {
    OutlinedButton(
        onClick = onClick, modifier = modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color)
    ) { Text(text, fontWeight = FontWeight.Bold, color = color) }
}

// ---------- chip / nhãn ----------
@Composable
fun Pill(text: String, selected: Boolean, onClick: (() -> Unit)? = null) {
    val shape = RoundedCornerShape(50)
    Box(
        Modifier.clip(shape)
            .background(if (selected) Soft else Color.White)
            .border(1.dp, if (selected) Primary else Color(0xFFBBBBBB), shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) { Text(text, color = if (selected) Primary else Color.Black, fontSize = 14.sp) }
}

@Composable
fun PillRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { Pill(it, it == selected) { onSelect(it) } }
    }
}

@Composable
fun StatusTag(text: String, color: Color = Primary) {
    val shape = RoundedCornerShape(50)
    Box(Modifier.clip(shape).border(1.dp, color, shape).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(text, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun InfoBox(text: String, bg: Color = Soft, fg: Color = Primary) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(bg)
            .border(1.dp, fg, RoundedCornerShape(10.dp)).padding(12.dp)
    ) { Text(text, color = fg, fontSize = 14.sp) }
}

@Composable
fun KeyValue(key: String, value: String, bold: Boolean = false) {
    Column {
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(key, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            Text(value, color = if (bold) Color.Black else Muted, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1.3f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
        }
        HorizontalDivider(color = Color(0xFFE0E0E0))
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 6.dp))
}

@Composable
fun ItemCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFCCCCCC))
    ) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { content() } }
}

@Composable
fun EmptyState(text: String) {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(text, color = Muted)
    }
}

// ---------- ảnh / camera ----------
@Composable
fun Thumb(path: String, modifier: Modifier = Modifier, label: String = "Ảnh") {
    val bmp: Bitmap? = remember(path) {
        if (path.isBlank()) null else try {
            val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
            BitmapFactory.decodeFile(path, opts)
        } catch (_: Exception) { null }
    }
    Box(modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFEDEDED)), contentAlignment = Alignment.Center) {
        if (bmp != null) Image(bmp.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Text(label, color = Muted, fontSize = 13.sp)
    }
}

/** Mở camera hệ thống (cảm biến máy ảnh) và trả về đường dẫn ảnh đã lưu. */
@Composable
fun rememberCamera(onPhoto: (String) -> Unit): () -> Unit {
    val ctx = LocalContext.current
    var pending by remember { mutableStateOf<File?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val f = pending
        if (ok && f != null) onPhoto(f.absolutePath) else f?.delete()
    }
    return {
        val f = File(ctx.filesDir, "img_${System.currentTimeMillis()}.jpg")
        pending = f
        try {
            val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", f)
            launcher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(ctx, "Không mở được camera trên thiết bị này.", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun rememberGallery(onPhoto: (String) -> Unit): () -> Unit {
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                ctx.contentResolver.openInputStream(uri)?.let { onPhoto(Store.fileCopy(it)) }
            } catch (_: Exception) {
            }
        }
    }
    return { launcher.launch("image/*") }
}

/** Một ô chụp ảnh đơn (ảnh công tơ). */
@Composable
fun PhotoSlot(label: String, path: String, onPhoto: (String) -> Unit, modifier: Modifier = Modifier) {
    val shoot = rememberCamera(onPhoto)
    Column(modifier) {
        Text(label, color = Muted, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(bottom = 4.dp))
        Thumb(path, Modifier.fillMaxWidth().height(110.dp).clickable { shoot() }, label = "📷 Chụp ảnh")
    }
}

/** Danh sách ảnh đính kèm (chụp hoặc chọn từ thư viện). */
@Composable
fun PhotoStrip(paths: List<String>, onChange: (List<String>) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    val cam = rememberCamera { onChange(paths + it) }
    val gal = rememberGallery { onChange(paths + it) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        paths.take(2).forEachIndexed { i, p ->
            Thumb(p, Modifier.weight(1f).height(80.dp).clickable { onChange(paths.filterIndexed { j, _ -> j != i }) }, "Ảnh ${i + 1}")
        }
        Box(Modifier.weight(1f)) {
            Box(Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFEDEDED)).clickable { menu = true },
                contentAlignment = Alignment.Center) { Text("+ Thêm", color = Muted) }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text("Chụp ảnh") }, onClick = { menu = false; cam() })
                DropdownMenuItem(text = { Text("Chọn từ thư viện") }, onClick = { menu = false; gal() })
            }
        }
    }
    if (paths.size > 2) Text("Đã đính kèm ${paths.size} ảnh (chạm vào ảnh để xóa).", color = Muted, fontSize = 12.sp)
}

// ---------- mã QR ----------
fun qrBitmap(content: String, size: Int = 640): Bitmap {
    val matrix = com.google.zxing.qrcode.QRCodeWriter().encode(content, com.google.zxing.BarcodeFormat.QR_CODE, size, size,
        mapOf(com.google.zxing.EncodeHintType.MARGIN to 1))
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) for (y in 0 until size) bmp.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
    return bmp
}

@Composable
fun QrImage(content: String, modifier: Modifier = Modifier.size(240.dp)) {
    val bmp = remember(content) { qrBitmap(content) }
    Image(bmp.asImageBitmap(), "Mã VietQR", modifier.border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(12.dp)).padding(6.dp))
}

fun toast(ctx: android.content.Context, msg: String) = Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()

val contentPad = PaddingValues(16.dp)
