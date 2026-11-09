@file:OptIn(ExperimentalLayoutApi::class)

package com.rentconnect.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

// =============== FR06 – Landlord: lập hợp đồng (BM03) ===============
@Composable
fun ContractFormScreen(roomId: String) {
    val nav = LocalNav.current
    val ctx = LocalContext.current
    val me = Store.me ?: return
    val room = Store.room(roomId)
    val candidates = remember(roomId) {
        val booked = Store.state.viewings.filter { it.roomId == roomId && it.status != ViewingStatus.CANCELLED }.map { it.tenantId }.distinct()
        val ids = booked.ifEmpty { Store.state.users.filter { it.role == Role.TENANT }.map { it.id } }
        ids.mapNotNull { Store.user(it) }.filter { Store.activeContractOfTenant(it.id) == null }
    }
    var tenantLabel by remember { mutableStateOf("") }
    var start by remember { mutableStateOf(Store.fmtDate(LocalDate.now())) }
    var end by remember { mutableStateOf(Store.fmtDate(LocalDate.now().plusYears(1))) }
    var rent by remember { mutableStateOf(room?.rent?.toString() ?: "") }
    var deposit by remember { mutableStateOf(room?.deposit?.toString() ?: "") }
    var cycle by remember { mutableStateOf("1 tháng/lần") }
    var day by remember { mutableStateOf("5") }
    var terms by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val cycles = listOf("1 tháng/lần", "3 tháng/lần", "6 tháng/lần")

    Scaffold(topBar = { AppTopBar("Lập hợp đồng thuê (BM03)", nav::pop) }) { pad ->
        ScreenColumn(Modifier.padding(pad)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledField("Bên A (Chủ trọ)", me.name, {}, Modifier.weight(1f), enabled = false)
                DropdownField("Bên B (Người thuê)", candidates.map { "${it.name} · ${it.contact}" }, tenantLabel,
                    { tenantLabel = it }, Modifier.weight(1f), placeholder = "Chọn")
            }
            LabeledField("Mã phòng thuê", "$roomId – ${room?.title ?: ""}", {}, enabled = false)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledField("Ngày bắt đầu", start, { start = it }, Modifier.weight(1f), placeholder = "dd/mm/yyyy")
                LabeledField("Ngày kết thúc", end, { end = it }, Modifier.weight(1f), placeholder = "dd/mm/yyyy")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledField("Giá thuê (VNĐ)", rent, { rent = digits(it).take(10) }, Modifier.weight(1f), keyboard = KeyboardType.Number)
                LabeledField("Tiền cọc (VNĐ)", deposit, { deposit = digits(it).take(10) }, Modifier.weight(1f), keyboard = KeyboardType.Number)
            }
            DropdownField("Chu kỳ thanh toán", cycles, cycle, { cycle = it })
            DropdownField("Ngày chốt hóa đơn hàng tháng", (1..28).map { it.toString() }, day, { day = it })
            LabeledField("Điều khoản bổ sung (bồi thường, vi phạm, chấm dứt...)", terms, { terms = it }, singleLine = false, minLines = 4)
            if (error.isNotBlank()) InfoBox(error, RedBg, Red)
            PrimaryButton("Tạo bản nháp & gửi Người thuê", {
                val tenant = candidates.firstOrNull { "${it.name} · ${it.contact}" == tenantLabel }
                if (tenant == null) error = "Vui lòng chọn người thuê."
                else {
                    val e = Store.createContract(roomId, tenant.id, start, end, toLong(rent), toLong(deposit),
                        cycle.substringBefore(" ").toIntOrNull() ?: 1, day.toIntOrNull() ?: 5, terms)
                    if (e == null) { toast(ctx, "Đã gửi bản nháp hợp đồng cho người thuê."); nav.pop() } else error = e
                }
            })
        }
    }
}

// =============== FR06 – danh sách hợp đồng ===============
@Composable
fun ContractListScreen() {
    val nav = LocalNav.current
    val me = Store.me ?: return
    val list = Store.contractsFor(me)
    Scaffold(topBar = { AppTopBar("Hợp đồng", nav::pop) }) { pad ->
        ScreenColumn(Modifier.padding(pad)) {
            if (list.isEmpty()) EmptyState("Chưa có hợp đồng.")
            list.forEach { c ->
                ItemCard(onClick = { nav.push(Screen.ContractView(c.id)) }) {
                    Text(Store.room(c.roomId)?.title ?: c.roomId, fontWeight = FontWeight.Bold)
                    Text("${c.start} → ${c.end}", color = Muted)
                    Text("${Store.user(c.landlordId)?.name} ↔ ${Store.user(c.tenantId)?.name}", color = Muted)
                    StatusTag(c.status.label, if (c.status == ContractStatus.ACTIVE) Green else Primary)
                }
            }
        }
    }
}

// =============== FR06 – rà soát & xác nhận (Tenant) ===============
@Composable
fun ContractViewScreen(id: String) {
    val nav = LocalNav.current
    val ctx = LocalContext.current
    val me = Store.me ?: return
    val c = Store.contract(id)
    Scaffold(topBar = { AppTopBar("Xác nhận hợp đồng", nav::pop) }) { pad ->
        if (c == null) { EmptyState("Không tìm thấy hợp đồng."); return@Scaffold }
        ScreenColumn(Modifier.padding(pad)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(ContractStatus.DRAFT.label, c.status == ContractStatus.DRAFT)
                Pill(ContractStatus.ACTIVE.label, c.status == ContractStatus.ACTIVE)
            }
            KeyValue("Bên A / Bên B", "${Store.user(c.landlordId)?.name} / ${Store.user(c.tenantId)?.name}")
            KeyValue("Mã phòng thuê", "${c.roomId} – ${Store.room(c.roomId)?.title ?: ""}")
            KeyValue("Thời hạn thuê", "${c.start} → ${c.end}")
            KeyValue("Giá thuê / Tiền cọc", "${formatMoney(c.rent)} / ${formatMoney(c.deposit)}")
            KeyValue("Chu kỳ thanh toán", "${c.cycleMonths} tháng/lần")
            KeyValue("Ngày chốt hóa đơn", "Ngày ${c.billingDay} hàng tháng")
            KeyValue("Điều khoản bổ sung", c.terms.ifBlank { "—" })
            if (me.id == c.tenantId && c.status == ContractStatus.DRAFT) {
                PrimaryButton("Xác nhận hợp đồng", {
                    Store.confirmContract(c.id); toast(ctx, "Hợp đồng đã có hiệu lực.")
                }, color = Green)
                InfoBox("Sau khi xác nhận: hợp đồng chuyển Có hiệu lực (Active), khóa chỉnh sửa (Read-only) và thiết lập chu kỳ thu phí.")
            }
            if (c.status == ContractStatus.ACTIVE) InfoBox("Hợp đồng đang ở chế độ chỉ đọc (Read-only), không thể chỉnh sửa.")
            if (c.status == ContractStatus.DRAFT && me.id == c.landlordId) InfoBox("Đang chờ người thuê rà soát và xác nhận.", AmberBg, Amber)
        }
    }
}
