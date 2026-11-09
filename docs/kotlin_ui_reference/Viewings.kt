@file:OptIn(ExperimentalLayoutApi::class)

package com.rentconnect.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate

// =============== FR04 – Tenant: đặt lịch (BM02) ===============
@Composable
fun BookingScreen(roomId: String) {
    val nav = LocalNav.current
    val ctx = LocalContext.current
    val me = Store.me ?: return
    val days = remember { (1..14).map { Store.fmtDate(LocalDate.now().plusDays(it.toLong())) } }
    var date by remember { mutableStateOf("") }
    var slot by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(me.phone) }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Scaffold(topBar = { AppTopBar("Đặt lịch xem phòng (BM02)", nav::pop) }) { pad ->
        ScreenColumn(Modifier.padding(pad)) {
            LabeledField("Mã phòng (Room ID)", "$roomId – ${Store.room(roomId)?.title ?: ""}", {}, enabled = false)
            DropdownField("Ngày hẹn", days, date, { date = it }, placeholder = "Chọn ngày")
            DropdownField("Khung giờ", Store.slots, slot, { slot = it }, placeholder = "Chọn khung giờ khả dụng")
            LabeledField("Số điện thoại liên hệ", phone, { phone = digits(it).take(11) }, placeholder = "Tự điền từ tài khoản (có thể sửa)",
                keyboard = KeyboardType.Phone)
            LabeledField("Ghi chú bổ sung", note, { note = it }, singleLine = false, minLines = 3)
            if (error.isNotBlank()) InfoBox(error, RedBg, Red)
            PrimaryButton("Gửi yêu cầu", {
                val e = Store.bookViewing(roomId, date, slot, phone, note)
                if (e == null) {
                    toast(ctx, "Đã gửi yêu cầu. Trạng thái: Chờ xác nhận.")
                    nav.pop()
                    nav.push(Screen.Viewings)
                } else error = e
            })
            InfoBox("Nếu trùng lịch, hệ thống báo xung đột và yêu cầu chọn khung giờ khác.", RedBg, Red)
        }
    }
}

// =============== FR04 – danh sách lịch hẹn (Tenant xem, Landlord xử lý) ===============
@Composable
fun ViewingsScreen() {
    val nav = LocalNav.current
    val me = Store.me ?: return
    var filter by remember { mutableStateOf(ViewingStatus.PENDING) }
    var declineId by remember { mutableStateOf<String?>(null) }
    var counter by remember { mutableStateOf("") }
    val list = Store.viewingsFor(me).filter { it.status == filter }

    Scaffold(topBar = { AppTopBar(if (me.role == Role.LANDLORD) "Lịch hẹn xem phòng" else "Lịch xem phòng của tôi", nav::pop) }) { pad ->
        ScreenColumn(Modifier.padding(pad)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ViewingStatus.values().forEach { s -> Pill(s.label, filter == s) { filter = s } }
            }
            if (list.isEmpty()) EmptyState("Chưa có lịch hẹn.")
            list.forEach { v ->
                val room = Store.room(v.roomId)
                ItemCard {
                    Text("${room?.title ?: v.roomId} (${v.roomId})", fontWeight = FontWeight.Bold)
                    Text("${Store.user(v.tenantId)?.name ?: ""} · ${v.date} · ${v.slot}", color = Muted)
                    if (v.phone.isNotBlank()) Text("SĐT: ${v.phone}", color = Muted)
                    if (v.note.isNotBlank()) Text("Ghi chú: ${v.note}", color = Muted)
                    if (v.response.isNotBlank()) Text("Phản hồi: ${v.response}", color = Red)
                    StatusTag(when (v.status) {
                        ViewingStatus.PENDING -> "Chờ xác nhận"; ViewingStatus.CONFIRMED -> "Đã xác nhận"
                        ViewingStatus.CANCELLED -> "Đã hủy/từ chối"; ViewingStatus.COMPLETED -> "Hoàn tất"
                    })
                    if (me.role == Role.LANDLORD && v.status == ViewingStatus.PENDING) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PrimaryButton("Xác nhận", { Store.respondViewing(v.id, ViewingStatus.CONFIRMED) }, Modifier.weight(1f), Green)
                            PrimaryButton("Từ chối", { Store.respondViewing(v.id, ViewingStatus.CANCELLED) }, Modifier.weight(1f), Red)
                        }
                        OutlineButton("Từ chối kèm đề xuất lịch khác", { declineId = v.id })
                    }
                    if (me.role == Role.LANDLORD && v.status == ViewingStatus.CONFIRMED) {
                        OutlineButton("Đánh dấu hoàn tất", { Store.respondViewing(v.id, ViewingStatus.COMPLETED) })
                    }
                    if (me.role == Role.TENANT && v.status == ViewingStatus.PENDING) {
                        OutlineButton("Hủy lịch", { Store.respondViewing(v.id, ViewingStatus.CANCELLED, "Người thuê hủy lịch") }, color = Red)
                    }
                }
            }
            if (me.role == Role.LANDLORD) InfoBox("Kết quả được cập nhật vào trạng thái lịch hẹn và gửi thông báo cho người thuê.")
        }
    }
    declineId?.let { id ->
        AlertDialog(
            onDismissRequest = { declineId = null },
            title = { Text("Đề xuất lịch khác") },
            text = { LabeledField("", counter, { counter = it }, placeholder = "VD: Em có thể xem vào 15:00 ngày 30/10", singleLine = false, minLines = 2) },
            confirmButton = {
                TextButton(onClick = {
                    Store.respondViewing(id, ViewingStatus.CANCELLED, counter.ifBlank { "Chủ trọ từ chối lịch này" })
                    counter = ""; declineId = null
                }) { Text("Gửi") }
            },
            dismissButton = { TextButton(onClick = { declineId = null }) { Text("Hủy") } }
        )
    }
}
