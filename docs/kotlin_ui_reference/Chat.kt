package com.rentconnect.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =============== FR05 – danh sách hội thoại ===============
@Composable
fun ChatListTab() {
    val nav = LocalNav.current
    val me = Store.me ?: return
    val partners = Store.partners(me.id)
    ScreenColumn {
        if (me.role != Role.ADMIN) {
            OutlineButton("Gửi yêu cầu hỗ trợ tới Admin", { nav.push(Screen.SupportForm) })
            val mySupports = Store.state.supports.filter { it.userId == me.id }.sortedByDescending { it.ts }
            if (mySupports.isNotEmpty()) {
                SectionTitle("Yêu cầu hỗ trợ của tôi")
                mySupports.forEach { s ->
                    ItemCard(onClick = { nav.push(Screen.SupportDetail(s.id)) }) {
                        Text(s.content.take(80), fontWeight = FontWeight.Bold)
                        StatusTag(if (s.reply.isBlank()) "Chờ phản hồi" else "Đã phản hồi", if (s.reply.isBlank()) Amber else Green)
                    }
                }
            }
        } else {
            val all = Store.state.supports.sortedByDescending { it.ts }
            SectionTitle("Yêu cầu hỗ trợ (${all.count { it.reply.isBlank() }} chờ)")
            if (all.isEmpty()) EmptyState("Chưa có yêu cầu hỗ trợ.")
            all.forEach { s ->
                ItemCard(onClick = { nav.push(Screen.SupportDetail(s.id)) }) {
                    Text("${Store.user(s.userId)?.name ?: "?"} · ${formatTime(s.ts)}", color = Muted)
                    Text(s.content.take(100), fontWeight = FontWeight.Bold)
                    StatusTag(if (s.reply.isBlank()) "Chờ phản hồi" else "Đã phản hồi", if (s.reply.isBlank()) Amber else Green)
                }
            }
        }
        SectionTitle("Trò chuyện")
        if (partners.isEmpty()) EmptyState("Chưa có cuộc trò chuyện. Mở một tin phòng và chọn \"Nhắn tin cho chủ trọ\".")
        partners.forEach { pid ->
            val u = Store.user(pid) ?: return@forEach
            val last = Store.thread(me.id, pid).lastOrNull()
            ItemCard(onClick = { nav.push(Screen.Chat(pid)) }) {
                Text("${u.name} (${u.role.label})", fontWeight = FontWeight.Bold)
                if (last != null) Text(last.text.take(60), color = Muted)
            }
        }
        // Liên hệ nhanh: chủ trọ ↔ người thuê đang có hợp đồng
        val contacts = Store.contractsFor(me).map { if (me.role == Role.TENANT) it.landlordId else it.tenantId }.distinct()
            .filter { it !in partners }
        if (contacts.isNotEmpty()) {
            SectionTitle("Bắt đầu trò chuyện")
            contacts.forEach { pid ->
                Store.user(pid)?.let { u ->
                    ItemCard(onClick = { nav.push(Screen.Chat(pid)) }) { Text("${u.name} (${u.role.label})", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

// =============== FR05 – màn hình trò chuyện ===============
@Composable
fun ChatScreen(partnerId: String) {
    val nav = LocalNav.current
    val me = Store.me ?: return
    val partner = Store.user(partnerId)
    val msgs = Store.thread(me.id, partnerId)
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    LaunchedEffect(msgs.size) { if (msgs.isNotEmpty()) listState.animateScrollToItem(msgs.lastIndex) }

    Scaffold(topBar = { AppTopBar(partner?.name ?: "Trò chuyện", nav::pop) }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp), state = listState, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(msgs) { m ->
                    val mine = m.fromId == me.id
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = if (mine) Alignment.End else Alignment.Start) {
                        Box(
                            Modifier.background(if (mine) Soft else Color(0xFFF2F2F2), RoundedCornerShape(18.dp))
                                .border(1.dp, if (mine) Primary else Color(0xFFCCCCCC), RoundedCornerShape(18.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) { Text(m.text) }
                        Text(formatTime(m.ts), color = Muted, fontSize = 11.sp)
                    }
                }
            }
            if (me.role != Role.ADMIN) {
                OutlineButton("Gửi yêu cầu hỗ trợ tới Admin", { nav.push(Screen.SupportForm) }, Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            }
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(text, { text = it }, Modifier.weight(1f), placeholder = { Text("Nhập tin nhắn...") },
                    shape = RoundedCornerShape(24.dp), maxLines = 3)
                Button(onClick = { Store.sendMessage(partnerId, text); text = "" }, enabled = text.isNotBlank()) { Text("Gửi") }
            }
        }
    }
}

// =============== FR05 – yêu cầu hỗ trợ (Support Ticket) ===============
@Composable
fun SupportFormScreen() {
    val nav = LocalNav.current
    val ctx = LocalContext.current
    var content by remember { mutableStateOf("") }
    var files by remember { mutableStateOf(listOf<String>()) }
    var error by remember { mutableStateOf("") }
    Scaffold(topBar = { AppTopBar("Yêu cầu hỗ trợ (Support Ticket)", nav::pop) }) { pad ->
        ScreenColumn(Modifier.padding(pad)) {
            Text("Gửi tới: Admin", color = Muted)
            LabeledField("Nội dung yêu cầu", content, { content = it }, singleLine = false, minLines = 6)
            Text("Tệp đính kèm", color = Muted, fontWeight = FontWeight.Bold)
            PhotoStrip(files) { files = it }
            if (error.isNotBlank()) InfoBox(error, RedBg, Red)
            PrimaryButton("Gửi yêu cầu", {
                val e = Store.createSupport(content, files)
                if (e == null) { toast(ctx, "Đã gửi yêu cầu tới Admin."); nav.pop() } else error = e
            })
        }
    }
}

@Composable
fun SupportDetailScreen(id: String) {
    val nav = LocalNav.current
    val me = Store.me ?: return
    val s = Store.support(id)
    var reply by remember { mutableStateOf("") }
    Scaffold(topBar = { AppTopBar("Yêu cầu hỗ trợ", nav::pop) }) { pad ->
        if (s == null) { EmptyState("Không tìm thấy yêu cầu."); return@Scaffold }
        ScreenColumn(Modifier.padding(pad)) {
            Text("Từ: ${Store.user(s.userId)?.name ?: "?"} · ${formatTime(s.ts)}", color = Muted)
            ItemCard { Text(s.content) }
            if (s.attachments.isNotEmpty()) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                s.attachments.take(3).forEach { Thumb(it, Modifier.weight(1f).height(80.dp)) }
            }
            SectionTitle("Phản hồi")
            if (s.reply.isNotBlank()) {
                ItemCard { Text(s.reply); Text(formatTime(s.replyTs), color = Muted) }
            } else if (me.role == Role.ADMIN) {
                LabeledField("", reply, { reply = it }, singleLine = false, minLines = 3, placeholder = "Nhập phản hồi của Admin")
                PrimaryButton("Gửi phản hồi", { Store.replySupport(id, reply); reply = "" })
            } else EmptyState("Admin chưa phản hồi.")
        }
    }
}
