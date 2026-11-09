@file:OptIn(ExperimentalLayoutApi::class)

package com.rentconnect.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private fun ticketColor(s: TicketStatus) = when (s) {
    TicketStatus.OPEN -> Amber
    TicketStatus.IN_PROGRESS -> Primary
    TicketStatus.RESOLVED -> Green
}

// =============== danh sách phiếu sự cố (tab) ===============
@Composable
fun TicketListTab() {
    val nav = LocalNav.current
    val me = Store.me ?: return
    val list = Store.ticketsFor(me)
    ScreenColumn {
        if (me.role == Role.TENANT) PrimaryButton("+ Báo cáo sự cố", { nav.push(Screen.TicketForm) })
        if (list.isEmpty()) EmptyState("Chưa có phiếu sự cố.")
        list.forEach { t ->
            ItemCard(onClick = { nav.push(Screen.TicketDetail(t.id)) }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${t.category} · ${t.priority}", fontWeight = FontWeight.Bold)
                    StatusTag(t.status.label, ticketColor(t.status))
                }
                Text(t.desc.take(80), color = Muted)
                Text("${Store.room(t.roomId)?.title ?: t.roomId} · ${Store.user(t.tenantId)?.name ?: ""}", color = Muted)
            }
        }
    }
}

// =============== FR10 – Tenant: tạo phiếu (BM06) ===============
@Composable
fun TicketFormScreen() {
    val nav = LocalNav.current
    var cat by remember { mutableStateOf(Store.categories.first()) }
    var pri by remember { mutableStateOf(Store.priorities[1]) }
    var desc by remember { mutableStateOf("") }
    var photos by remember { mutableStateOf(listOf<String>()) }
    var error by remember { mutableStateOf("") }
    Scaffold(topBar = { AppTopBar("Báo cáo sự cố (BM06)", nav::pop) }) { pad ->
        ScreenColumn(Modifier.padding(pad)) {
            Text("Phân loại sự cố", color = Muted, fontWeight = FontWeight.Bold)
            PillRow(Store.categories, cat) { cat = it }
            Text("Mức độ ưu tiên", color = Muted, fontWeight = FontWeight.Bold)
            PillRow(Store.priorities, pri) { pri = it }
            LabeledField("Mô tả tình trạng", desc, { desc = it }, placeholder = "Mô tả chi tiết sự cố...", singleLine = false, minLines = 5)
            Text("Hình ảnh / Video đính kèm", color = Muted, fontWeight = FontWeight.Bold)
            PhotoStrip(photos) { photos = it }
            if (error.isNotBlank()) InfoBox(error, RedBg, Red)
            PrimaryButton("Gửi phiếu sự cố", {
                val (e, id) = Store.createTicket(cat, pri, desc, photos)
                if (e == null && id != null) { nav.pop(); nav.push(Screen.TicketSent(id)) } else error = e ?: "Lỗi không xác định."
            })
        }
    }
}

@Composable
fun TicketSentScreen(id: String) {
    val nav = LocalNav.current
    val t = Store.ticket(id)
    Scaffold(topBar = { AppTopBar("Phiếu đã gửi", nav::pop) }) { pad ->
        ScreenColumn(Modifier.padding(pad)) {
            Text("Phiếu sự cố đã được ghi nhận", fontWeight = FontWeight.Bold, fontSize = 20.sp,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 40.dp))
            t?.let { StatusTag(it.status.label, ticketColor(it.status)) }
            InfoBox("Hệ thống ghi nhận phiếu ở trạng thái Chờ xử lý và gửi thông báo tới Chủ trọ.")
            OutlineButton("Theo dõi phiếu", { nav.pop(); nav.push(Screen.TicketDetail(id)) })
        }
    }
}

// =============== FR11 – xử lý / theo dõi phiếu ===============
@Composable
fun StatusStepper(status: TicketStatus) {
    val steps = TicketStatus.values()
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        steps.forEachIndexed { i, s ->
            val done = s.ordinal <= status.ordinal
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (i > 0) HorizontalDivider(Modifier.weight(1f), color = Color.Gray) else Box(Modifier.weight(1f))
                    Box(Modifier.size(22.dp).background(if (done) Primary else Color.White, CircleShape).border(1.dp, if (done) Primary else Color.Gray, CircleShape))
                    if (i < steps.lastIndex) HorizontalDivider(Modifier.weight(1f), color = Color.Gray) else Box(Modifier.weight(1f))
                }
                Text(s.label, fontSize = 12.sp, fontWeight = if (s == status) FontWeight.Bold else FontWeight.Normal,
                    color = if (s == status) Color.Black else Muted, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun TicketDetailScreen(id: String) {
    val nav = LocalNav.current
    val me = Store.me ?: return
    val t = Store.ticket(id)
    Scaffold(topBar = { AppTopBar(if (me.role == Role.LANDLORD) "Chi tiết phiếu sự cố" else "Theo dõi phiếu sự cố", nav::pop) }) { pad ->
        if (t == null) { EmptyState("Không tìm thấy phiếu."); return@Scaffold }
        ScreenColumn(Modifier.padding(pad)) {
            StatusStepper(t.status)
            KeyValue("Phân loại / Mức độ", "${t.category} / ${t.priority}")
            KeyValue("Phòng", Store.room(t.roomId)?.title ?: t.roomId)
            KeyValue("Người báo", Store.user(t.tenantId)?.name ?: "")
            Text("Mô tả tình trạng: ${t.desc}", color = Muted)
            if (t.photos.isNotEmpty()) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                t.photos.take(2).forEach { Thumb(it, Modifier.weight(1f).height(110.dp)) }
            }
            if (me.role == Role.LANDLORD && t.status == TicketStatus.OPEN) {
                PrimaryButton("Tiếp nhận & chuyển Đang xử lý", { Store.setTicketStatus(id, TicketStatus.IN_PROGRESS) })
            }
            if ((me.id == t.landlordId || me.id == t.tenantId) && t.status != TicketStatus.RESOLVED) {
                if (me.role == Role.LANDLORD) OutlineButton("Đóng phiếu (Đã giải quyết)", { Store.setTicketStatus(id, TicketStatus.RESOLVED) }, color = Green)
                else PrimaryButton("Đóng phiếu (Đã giải quyết)", { Store.setTicketStatus(id, TicketStatus.RESOLVED) }, color = Green)
            }
            SectionTitle("Lịch sử xử lý")
            t.history.forEach { KeyValue(formatTime(it.ts), it.text) }
        }
    }
}
