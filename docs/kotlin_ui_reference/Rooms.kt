@file:OptIn(ExperimentalLayoutApi::class)

package com.rentconnect.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =============== FR02 – Landlord: đăng tin (BM01) ===============
@Composable
fun RoomFormScreen() {
    val nav = LocalNav.current
    val ctx = LocalContext.current
    val me = Store.me ?: return
    var title by remember { mutableStateOf("") }
    var province by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var ward by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var cap by remember { mutableStateOf("") }
    var rent by remember { mutableStateOf("") }
    var deposit by remember { mutableStateOf("") }
    var elec by remember { mutableStateOf("") }
    var water by remember { mutableStateOf("") }
    var fees by remember { mutableStateOf(listOf<Fee>()) }
    var amen by remember { mutableStateOf(setOf<String>()) }
    var photos by remember { mutableStateOf(listOf<String>()) }
    var error by remember { mutableStateOf("") }
    var feeDialog by remember { mutableStateOf(false) }

    Scaffold(topBar = { AppTopBar("Đăng tin phòng trọ (BM01)", nav::pop) }) { pad ->
        ScreenColumn(Modifier.padding(pad)) {
            LabeledField("Tiêu đề tin đăng", title, { title = it }, placeholder = "Nhập tiêu đề")
            DropdownField("Tỉnh/Thành phố", Seed.provinces.keys.toList(), province,
                { province = it; district = ""; ward = "" })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DropdownField("Quận/Huyện", Seed.provinces[province]?.keys?.toList() ?: emptyList(), district,
                    { district = it; ward = "" }, Modifier.weight(1f))
                DropdownField("Phường/Xã", Seed.provinces[province]?.get(district) ?: emptyList(), ward,
                    { ward = it }, Modifier.weight(1f))
            }
            LabeledField("Địa chỉ chi tiết (số nhà, ngõ, ngách)", address, { address = it })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledField("Diện tích (m²)", area, { area = digits(it).take(4) }, Modifier.weight(1f), keyboard = KeyboardType.Number)
                LabeledField("Sức chứa tối đa (người)", cap, { cap = digits(it).take(2) }, Modifier.weight(1f), keyboard = KeyboardType.Number)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledField("Giá thuê (VNĐ/tháng)", rent, { rent = digits(it).take(10) }, Modifier.weight(1f), keyboard = KeyboardType.Number)
                LabeledField("Tiền cọc (VNĐ)", deposit, { deposit = digits(it).take(10) }, Modifier.weight(1f), keyboard = KeyboardType.Number)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledField("Điện (VNĐ/kWh)", elec, { elec = digits(it).take(6) }, Modifier.weight(1f), keyboard = KeyboardType.Number)
                LabeledField("Nước (VNĐ/khối)", water, { water = digits(it).take(6) }, Modifier.weight(1f), keyboard = KeyboardType.Number)
            }
            Text("Phí dịch vụ khác (Internet, vệ sinh, rác...)", color = Muted, fontWeight = FontWeight.Bold)
            fees.forEachIndexed { i, f ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("${f.name}: ${formatMoney(f.amount)}", Modifier.weight(1f))
                    TextButton(onClick = { fees = fees.filterIndexed { j, _ -> j != i } }) { Text("Xóa") }
                }
            }
            OutlineButton("+ Thêm khoản phí", { feeDialog = true })
            Text("Tiện ích", color = Muted, fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Seed.amenityList.forEach { a ->
                    Pill(a, a in amen) { amen = if (a in amen) amen - a else amen + a }
                }
            }
            Text("Hình ảnh / Video đính kèm", color = Muted, fontWeight = FontWeight.Bold)
            PhotoStrip(photos) { photos = it }
            if (error.isNotBlank()) InfoBox(error, RedBg, Red)
            PrimaryButton("Gửi duyệt", {
                val room = Room("", me.id, title.trim(), province, district, ward, address.trim(), area.toIntOrNull() ?: 0,
                    cap.toIntOrNull() ?: 0, toLong(rent), toLong(deposit), toLong(elec), toLong(water), fees,
                    Seed.amenityList.filter { it in amen }, photos, RoomStatus.PENDING)
                val e = Store.submitRoom(room)
                if (e == null) {
                    toast(ctx, "Đã gửi duyệt. Tin ở trạng thái Chờ duyệt (Pending).")
                    nav.pop()
                } else error = e
            })
        }
    }
    if (feeDialog) {
        var fn by remember { mutableStateOf("") }
        var fa by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { feeDialog = false },
            title = { Text("Thêm khoản phí") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabeledField("Tên khoản phí", fn, { fn = it }, placeholder = "Internet, rác, vệ sinh...")
                    LabeledField("Số tiền (VNĐ/tháng)", fa, { fa = digits(it).take(9) }, keyboard = KeyboardType.Number)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (fn.isNotBlank() && toLong(fa) > 0) fees = fees + Fee(fn.trim(), toLong(fa))
                    feeDialog = false
                }) { Text("Thêm") }
            },
            dismissButton = { TextButton(onClick = { feeDialog = false }) { Text("Hủy") } }
        )
    }
}

// =============== Landlord: danh sách phòng của tôi ===============
@Composable
fun MyRoomsTab() {
    val nav = LocalNav.current
    val me = Store.me ?: return
    val rooms = Store.roomsOf(me.id)
    val st = Store.state
    val unpaid = st.invoices.filter { it.landlordId == me.id && it.status == InvoiceStatus.ISSUED }.sumOf { it.total }
    val revenue = st.invoices.filter { it.landlordId == me.id && it.status == InvoiceStatus.PAID }.sumOf { it.total }
    val rented = rooms.count { Store.activeContractOfRoom(it.id) != null }
    ScreenColumn {
        ItemCard {
            Text("Tổng quan", fontWeight = FontWeight.Bold)
            KeyValue("Phòng đang cho thuê / tổng", "$rented / ${rooms.size}")
            KeyValue("Doanh thu đã thu", formatMoney(revenue))
            KeyValue("Công nợ chưa thu", formatMoney(unpaid))
        }
        PrimaryButton("+ Đăng tin phòng mới", { nav.push(Screen.RoomForm) })
        if (rooms.isEmpty()) EmptyState("Bạn chưa có tin đăng nào.")
        rooms.forEach { r ->
            ItemCard(onClick = { nav.push(Screen.RoomDetail(r.id)) }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(r.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(formatMoney(r.rent), color = Primary, fontWeight = FontWeight.Bold)
                }
                Text("${r.areaText} · ${r.area} m²", color = Muted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusTag(r.status.label, when (r.status) {
                        RoomStatus.ACTIVE -> Green; RoomStatus.PENDING -> Primary; else -> Red
                    })
                    if (Store.activeContractOfRoom(r.id) != null) StatusTag("Đang cho thuê", Amber)
                }
                if (r.adminNote.isNotBlank()) Text("Admin: ${r.adminNote}", color = Red)
            }
        }
    }
}

// =============== FR03 – Tenant: tìm kiếm & lọc ===============
@Composable
fun SearchTab() {
    val nav = LocalNav.current
    var keyword by remember { mutableStateOf("") }
    var province by remember { mutableStateOf("") }
    var priceRange by remember { mutableStateOf("") }
    var areaRange by remember { mutableStateOf("") }
    var amen by remember { mutableStateOf(setOf<String>()) }
    var page by remember { mutableIntStateOf(0) }
    var showFilters by remember { mutableStateOf(false) }

    val prices = listOf("Dưới 3 triệu", "3 – 5 triệu", "Trên 5 triệu")
    val areas = listOf("Từ 20 m²", "Từ 30 m²")
    val f = Store.Filter(
        keyword = keyword, province = province,
        minPrice = when (priceRange) { "3 – 5 triệu" -> 3_000_000; "Trên 5 triệu" -> 5_000_001; else -> 0 },
        maxPrice = when (priceRange) { "Dưới 3 triệu" -> 2_999_999; "3 – 5 triệu" -> 5_000_000; else -> Long.MAX_VALUE },
        minArea = when (areaRange) { "Từ 20 m²" -> 20; "Từ 30 m²" -> 30; else -> 0 },
        amenities = amen
    )
    val results = Store.search(f)
    val perPage = 5
    val pages = maxOf(1, (results.size + perPage - 1) / perPage)
    if (page >= pages) page = 0

    ScreenColumn {
        LabeledField("", keyword, { keyword = it; page = 0 }, placeholder = "Tìm theo từ khóa…")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill("Khu vực${if (province.isNotBlank()) ": $province" else ""}", province.isNotBlank()) { showFilters = !showFilters }
            Pill("Khoảng giá", priceRange.isNotBlank()) { showFilters = !showFilters }
            Pill("Diện tích", areaRange.isNotBlank()) { showFilters = !showFilters }
            Pill("Tiện ích", amen.isNotEmpty()) { showFilters = !showFilters }
        }
        if (showFilters) {
            ItemCard {
                DropdownField("Khu vực", listOf("") + Seed.provinces.keys, province, { province = it; page = 0 }, placeholder = "Tất cả")
                Text("Khoảng giá", color = Muted, fontWeight = FontWeight.Bold)
                PillRow(prices, priceRange) { priceRange = if (priceRange == it) "" else it; page = 0 }
                Text("Diện tích", color = Muted, fontWeight = FontWeight.Bold)
                PillRow(areas, areaRange) { areaRange = if (areaRange == it) "" else it; page = 0 }
                Text("Tiện ích", color = Muted, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Seed.amenityList.forEach { a -> Pill(a, a in amen) { amen = if (a in amen) amen - a else amen + a; page = 0 } }
                }
                OutlineButton("Xóa bộ lọc", { province = ""; priceRange = ""; areaRange = ""; amen = emptySet(); keyword = "" })
            }
        }
        Text("${results.size} phòng đang hiển thị", color = Muted)
        if (results.isEmpty()) EmptyState("Không tìm thấy phòng phù hợp.")
        results.drop(page * perPage).take(perPage).forEach { r ->
            ItemCard(onClick = { nav.push(Screen.RoomDetail(r.id)) }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Thumb(r.images.firstOrNull() ?: "", Modifier.size(84.dp))
                    Column(Modifier.weight(1f)) {
                        Text(r.title, fontWeight = FontWeight.Bold, maxLines = 2)
                        Text("${r.areaText} · ${r.area} m²", color = Muted)
                        Text(formatMoney(r.rent) + "/tháng", color = Primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        if (pages > 1) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { if (page > 0) page-- }, enabled = page > 0) { Text("‹") }
                Text("Trang ${page + 1} / $pages", color = Muted)
                TextButton(onClick = { if (page < pages - 1) page++ }, enabled = page < pages - 1) { Text("›") }
            }
        }
    }
}

// =============== FR03 – chi tiết phòng ===============
@Composable
fun RoomDetailScreen(roomId: String) {
    val nav = LocalNav.current
    val me = Store.me ?: return
    val r = Store.room(roomId)
    val owner = Store.user(r?.ownerId)
    Scaffold(topBar = { AppTopBar("Chi tiết phòng", nav::pop) }) { pad ->
        if (r == null) { EmptyState("Không tìm thấy phòng."); return@Scaffold }
        ScreenColumn(Modifier.padding(pad)) {
            if (r.images.isEmpty()) Thumb("", Modifier.fillMaxWidth().height(180.dp), "Hình ảnh / Video")
            else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                r.images.take(2).forEach { Thumb(it, Modifier.weight(1f).height(180.dp)) }
            }
            Text(r.title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            if (me.role != Role.TENANT) StatusTag(r.status.label)
            KeyValue("Giá thuê / Tiền cọc", "${formatMoney(r.rent)} / ${formatMoney(r.deposit)}")
            KeyValue("Diện tích / Sức chứa", "${r.area} m² / ${r.capacity} người")
            KeyValue("Địa chỉ", r.fullAddress)
            KeyValue("Điện / Nước", "${formatMoney(r.elecPrice)}/kWh / ${formatMoney(r.waterPrice)}/khối")
            KeyValue("Phí dịch vụ khác", if (r.fees.isEmpty()) "Không" else r.fees.joinToString("; ") { "${it.name} ${formatMoney(it.amount)}" })
            KeyValue("Tiện ích", if (r.amenities.isEmpty()) "—" else r.amenities.joinToString(", "))
            KeyValue("Liên hệ chủ trọ", owner?.let { "${it.name} · ${it.contact}" } ?: "—")
            if (me.role == Role.TENANT) {
                PrimaryButton("Đặt lịch xem phòng", { nav.push(Screen.Booking(r.id)) })
                owner?.let { OutlineButton("Nhắn tin cho chủ trọ", { nav.push(Screen.Chat(it.id)) }) }
            }
            if (me.role == Role.ADMIN && r.status != RoomStatus.ACTIVE) {
                PrimaryButton("Mở màn hình kiểm duyệt", { nav.push(Screen.Moderate(r.id)) })
            }
            if (me.role == Role.LANDLORD && r.ownerId == me.id && r.status == RoomStatus.ACTIVE && Store.activeContractOfRoom(r.id) == null) {
                PrimaryButton("Lập hợp đồng cho phòng này", { nav.push(Screen.ContractForm(r.id)) })
            }
            if (r.adminNote.isNotBlank()) InfoBox("Ghi chú của Admin: ${r.adminNote}", AmberBg, Amber)
        }
    }
}

// =============== FR02 – Admin: kiểm duyệt ===============
@Composable
fun ModerationTab() {
    val nav = LocalNav.current
    var filter by remember { mutableStateOf(RoomStatus.PENDING) }
    val rooms = Store.state.rooms.filter { it.status == filter }.sortedByDescending { it.createdAt }
    ScreenColumn {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(RoomStatus.PENDING, RoomStatus.ACTIVE).forEach { s ->
                Pill(s.label, filter == s) { filter = s }
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(RoomStatus.NEED_INFO, RoomStatus.REJECTED).forEach { s -> Pill(s.label, filter == s) { filter = s } }
        }
        if (rooms.isEmpty()) EmptyState("Không có tin nào ở trạng thái này.")
        rooms.forEach { r ->
            ItemCard(onClick = { nav.push(Screen.Moderate(r.id)) }) {
                Text(r.title, fontWeight = FontWeight.Bold)
                Text("${r.areaText} · ${r.area} m² · ${formatMoney(r.rent)}", color = Muted)
                Text("Chủ trọ: ${Store.user(r.ownerId)?.name ?: "—"}", color = Muted)
            }
        }
    }
}

@Composable
fun ModerateScreen(roomId: String) {
    val nav = LocalNav.current
    val ctx = LocalContext.current
    val r = Store.room(roomId)
    var dialog by remember { mutableStateOf<RoomStatus?>(null) }
    var note by remember { mutableStateOf("") }
    Scaffold(topBar = { AppTopBar("Kiểm duyệt tin đăng", nav::pop) }) { pad ->
        if (r == null) { EmptyState("Không tìm thấy tin."); return@Scaffold }
        ScreenColumn(Modifier.padding(pad)) {
            StatusTag(r.status.label)
            if (r.images.isEmpty()) Thumb("", Modifier.fillMaxWidth().height(160.dp), "Hình ảnh phòng")
            else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { r.images.take(2).forEach { Thumb(it, Modifier.weight(1f).height(160.dp)) } }
            Text(r.title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            KeyValue("Địa chỉ", r.fullAddress)
            KeyValue("Diện tích / Sức chứa", "${r.area} m² / ${r.capacity} người")
            KeyValue("Giá thuê / Tiền cọc", "${formatMoney(r.rent)} / ${formatMoney(r.deposit)}")
            KeyValue("Điện / Nước", "${formatMoney(r.elecPrice)} / ${formatMoney(r.waterPrice)}")
            KeyValue("Phí dịch vụ khác", if (r.fees.isEmpty()) "Không" else r.fees.joinToString("; ") { "${it.name} ${formatMoney(it.amount)}" })
            KeyValue("Tiện ích", r.amenities.joinToString(", ").ifBlank { "—" })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PrimaryButton("Phê duyệt", {
                    Store.moderate(r.id, RoomStatus.ACTIVE); toast(ctx, "Đã phê duyệt."); nav.pop()
                }, Modifier.weight(1f), Green)
                PrimaryButton("Từ chối", { dialog = RoomStatus.REJECTED }, Modifier.weight(1f), Red)
            }
            OutlineButton("Yêu cầu bổ sung", { dialog = RoomStatus.NEED_INFO })
            InfoBox("Phê duyệt: trạng thái chuyển Đang hiển thị (Active) và đưa vào luồng tìm kiếm.")
        }
    }
    dialog?.let { st ->
        AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text(if (st == RoomStatus.REJECTED) "Lý do từ chối" else "Nội dung cần bổ sung") },
            text = { LabeledField("", note, { note = it }, singleLine = false, minLines = 3, placeholder = "Nhập nội dung gửi cho chủ trọ") },
            confirmButton = {
                TextButton(onClick = {
                    Store.moderate(roomId, st, note); note = ""; dialog = null; nav.pop()
                }) { Text("Gửi") }
            },
            dismissButton = { TextButton(onClick = { dialog = null }) { Text("Hủy") } }
        )
    }
}
