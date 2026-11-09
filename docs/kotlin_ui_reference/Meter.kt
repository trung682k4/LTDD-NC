package com.rentconnect.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

// =============== FR07 – chọn phòng để chốt chỉ số ===============
@Composable
fun MeterPickScreen() {
    val nav = LocalNav.current
    val me = Store.me ?: return
    val rooms = Store.roomsOf(me.id).filter { Store.activeContractOfRoom(it.id) != null }
    Scaffold(topBar = { AppTopBar("Chốt điện nước – chọn phòng", nav::pop) }) { pad ->
        ScreenColumn(Modifier.padding(pad)) {
            ConnectionBanner()
            if (rooms.isEmpty()) EmptyState("Chưa có phòng nào có hợp đồng hiệu lực.")
            rooms.forEach { r ->
                val last = Store.lastReading(r.id)
                ItemCard(onClick = { nav.push(Screen.MeterForm(r.id)) }) {
                    Text(r.title, fontWeight = FontWeight.Bold)
                    Text("Người thuê: ${Store.user(Store.activeContractOfRoom(r.id)?.tenantId)?.name ?: "—"}", color = Muted)
                    Text(if (last == null) "Chưa có chỉ số" else "Chỉ số gần nhất kỳ ${last.period}: điện ${last.elecNew}, nước ${last.waterNew}", color = Muted)
                    if (last?.period == Store.currentPeriod()) StatusTag("Đã chốt kỳ này", Green)
                }
            }
        }
    }
}

@Composable
fun ConnectionBanner() {
    if (!Store.isOnline) InfoBox("Ngoại tuyến: dữ liệu lưu trên máy, tự đồng bộ khi có mạng.", AmberBg, Amber)
    else if (Store.pendingCount() > 0) InfoBox("Có ${Store.pendingCount()} bản ghi chờ đồng bộ.", AmberBg, Amber)
}

// =============== FR07 – Landlord: chốt chỉ số (BM04) ===============
@Composable
fun MeterFormScreen(roomId: String) {
    val nav = LocalNav.current
    val ctx = LocalContext.current
    val room = Store.room(roomId)
    val last = Store.lastReading(roomId)
    var elec by remember { mutableStateOf("") }
    var water by remember { mutableStateOf("") }
    var elecPhoto by remember { mutableStateOf("") }
    var waterPhoto by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Scaffold(topBar = { AppTopBar("Chốt chỉ số điện nước (BM04)", nav::pop) }) { pad ->
        ScreenColumn(Modifier.padding(pad)) {
            ConnectionBanner()
            Row(Modifier.padding(0.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledField("Kỳ ghi nhận", Store.currentPeriod(), {}, Modifier.weight(1f), enabled = false)
                LabeledField("Mã phòng", roomId, {}, Modifier.weight(1f), enabled = false)
            }
            Text(room?.title ?: "", color = Muted)
            LabeledField("Chỉ số điện mới (cũ: ${last?.elecNew ?: 0})", elec, { elec = digits(it).take(7) },
                placeholder = "Nhập chỉ số", keyboard = KeyboardType.Number)
            PhotoSlot("Ảnh công tơ điện", elecPhoto, { elecPhoto = it })
            LabeledField("Chỉ số nước mới (cũ: ${last?.waterNew ?: 0})", water, { water = digits(it).take(7) },
                placeholder = "Nhập chỉ số", keyboard = KeyboardType.Number)
            PhotoSlot("Ảnh công tơ nước", waterPhoto, { waterPhoto = it })
            if (error.isNotBlank()) InfoBox(error, RedBg, Red)
            PrimaryButton("Lưu chỉ số", {
                val e = Store.submitReading(roomId, elec.toIntOrNull(), water.toIntOrNull(), elecPhoto, waterPhoto)
                if (e == null) {
                    toast(ctx, if (Store.isOnline) "Đã lưu và đồng bộ." else "Đã lưu ngoại tuyến, sẽ đồng bộ khi có mạng.")
                    nav.pop()
                } else error = e
            })
        }
    }
}

// =============== FR07 – Tenant: xem & đối chiếu ===============
@Composable
fun MyMeterScreen() {
    val nav = LocalNav.current
    val me = Store.me ?: return
    val c = Store.activeContractOfTenant(me.id)
    Scaffold(topBar = { AppTopBar("Điện nước của tôi", nav::pop) }) { pad ->
        ScreenColumn(Modifier.padding(pad)) {
            if (c == null) { EmptyState("Bạn chưa có hợp đồng còn hiệu lực."); return@ScreenColumn }
            val list = Store.readingsOf(c.roomId).filter { it.synced }
            val cur = list.firstOrNull()
            if (cur == null) { EmptyState("Chưa có chỉ số nào được ghi nhận."); return@ScreenColumn }
            SectionTitle("Kỳ ${cur.period}")
            KeyValue("Điện: số cũ → số mới", "${cur.elecOld} → ${cur.elecNew}")
            KeyValue("Điện tiêu thụ", "${Billing.consumption(cur.elecOld, cur.elecNew)} kWh", bold = true)
            KeyValue("Nước: số cũ → số mới", "${cur.waterOld} → ${cur.waterNew}")
            KeyValue("Nước tiêu thụ", "${Billing.consumption(cur.waterOld, cur.waterNew)} khối", bold = true)
            Text("Ảnh công tơ (đối chiếu)", color = Muted, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Thumb(cur.elecPhoto, Modifier.weight(1f).height(110.dp), "Ảnh điện")
                Thumb(cur.waterPhoto, Modifier.weight(1f).height(110.dp), "Ảnh nước")
            }
            SectionTitle("Lịch sử tiêu thụ")
            list.drop(1).forEach {
                KeyValue("Kỳ ${it.period}", "Điện ${Billing.consumption(it.elecOld, it.elecNew)} · Nước ${Billing.consumption(it.waterOld, it.waterNew)}")
            }
            if (list.size <= 1) Text("Chưa có kỳ trước.", color = Muted)
        }
    }
}

/** Dùng cho màn hình Thêm: công tắc mô phỏng mất mạng (kiểm thử chế độ ngoại tuyến). */
@Composable
fun OfflineToggle() {
    ItemCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Mô phỏng chế độ ngoại tuyến", Modifier.weight(1f))
            Switch(checked = Store.forceOffline, onCheckedChange = {
                Store.forceOffline = it
                if (!it) Store.syncPending()
            })
        }
        Text("Bật để thử ghi chỉ số khi mất mạng. Tắt để đồng bộ.", color = Muted)
    }
}
