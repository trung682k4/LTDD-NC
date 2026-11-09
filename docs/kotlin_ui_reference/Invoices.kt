package com.rentconnect.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private fun statusColor(s: InvoiceStatus) = when (s) {
    InvoiceStatus.DRAFT -> Primary
    InvoiceStatus.ISSUED -> Amber
    InvoiceStatus.PAID -> Green
}

// =============== danh sách hóa đơn (tab) ===============
@Composable
fun InvoiceListTab() {
    val nav = LocalNav.current
    val me = Store.me ?: return
    var filter by remember { mutableStateOf<InvoiceStatus?>(null) }
    val all = Store.invoicesFor(me)
    val list = all.filter { filter == null || it.status == filter }
    ScreenColumn {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill("Tất cả", filter == null) { filter = null }
            InvoiceStatus.values().filter { me.role == Role.LANDLORD || it != InvoiceStatus.DRAFT }.forEach { s ->
                Pill(if (s == InvoiceStatus.PAID) "Đã thu" else s.label, filter == s) { filter = s }
            }
        }
        if (list.isEmpty()) EmptyState("Chưa có hóa đơn.")
        list.forEach { inv ->
            ItemCard(onClick = { nav.push(Screen.InvoiceDetail(inv.id)) }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Hóa đơn kỳ ${inv.period}", fontWeight = FontWeight.Bold)
                    Text(formatMoney(inv.total), color = Primary, fontWeight = FontWeight.Bold)
                }
                Text("${Store.user(inv.tenantId)?.name ?: ""} · Phòng ${inv.roomId}", color = Muted)
                StatusTag(inv.status.label, statusColor(inv.status))
                if (me.role == Role.LANDLORD && inv.status == InvoiceStatus.ISSUED) {
                    PrimaryButton("Xác nhận đã nhận tiền", { Store.markPaid(inv.id) }, color = Green)
                }
            }
        }
    }
}

// =============== FR08 – chi tiết / duyệt hóa đơn ===============
@Composable
fun InvoiceDetailScreen(id: String) {
    val nav = LocalNav.current
    val ctx = LocalContext.current
    val me = Store.me ?: return
    val inv = Store.invoice(id)
    var adjust by remember { mutableStateOf("") }
    var negative by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Scaffold(topBar = {
        AppTopBar(if (inv == null) "Hóa đơn" else "Hóa đơn kỳ ${inv.period} (${if (inv.status == InvoiceStatus.DRAFT) "bản nháp" else "đã xuất"})", nav::pop)
    }) { pad ->
        if (inv == null) { EmptyState("Không tìm thấy hóa đơn."); return@Scaffold }
        val isDraft = inv.status == InvoiceStatus.DRAFT
        val adj = if (isDraft) (if (negative) -toLong(adjust) else toLong(adjust)) else inv.adjust
        val total = if (isDraft) Billing.total(inv.rentAmt, inv.elecAmt, inv.waterAmt, inv.feeAmt, adj) else inv.total
        ScreenColumn(Modifier.padding(pad)) {
            if (me.role == Role.TENANT && inv.status == InvoiceStatus.ISSUED) InfoBox("Bạn nhận được thông báo hóa đơn mới.")
            KeyValue("Tiền thuê phòng", formatMoney(inv.rentAmt))
            KeyValue("Tiền điện (${inv.elecUsed} kWh)", formatMoney(inv.elecAmt))
            KeyValue("Tiền nước (${inv.waterUsed} khối)", formatMoney(inv.waterAmt))
            KeyValue("Phí dịch vụ cố định", formatMoney(inv.feeAmt))
            if (!isDraft) KeyValue("Phát sinh / Giảm trừ", formatMoney(inv.adjust) + if (inv.adjustReason.isNotBlank()) " (${inv.adjustReason})" else "")
            if (isDraft && me.role == Role.LANDLORD) {
                Text("Khoản phát sinh / Giảm trừ (nếu có)", color = Muted, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill("Phát sinh (+)", !negative) { negative = false }
                    Pill("Giảm trừ (−)", negative) { negative = true }
                }
                LabeledField("", adjust, { adjust = digits(it).take(9) }, placeholder = "Số tiền", keyboard = KeyboardType.Number)
                LabeledField("Lý do", reason, { reason = it }, placeholder = "Nhập lý do")
            }
            KeyValue("Tổng tiền thanh toán", formatMoney(total), bold = true)
            if (error.isNotBlank()) InfoBox(error, RedBg, Red)

            if (isDraft && me.role == Role.LANDLORD) {
                PrimaryButton("Duyệt & xuất hóa đơn", {
                    val e = Store.issueInvoice(id, adj, reason)
                    if (e == null) { toast(ctx, "Đã xuất hóa đơn và thông báo cho người thuê."); nav.pop() } else error = e
                })
            }
            if (inv.status == InvoiceStatus.ISSUED && me.role == Role.TENANT) {
                PrimaryButton("Thanh toán bằng VietQR", { nav.push(Screen.Pay(id)) })
            }
            if (inv.status == InvoiceStatus.ISSUED && me.role == Role.LANDLORD) {
                PrimaryButton("Xác nhận đã nhận tiền", { Store.markPaid(id) }, color = Green)
                InfoBox("Sau khi xác nhận, hệ thống cập nhật trạng thái hóa đơn thành Đã thanh toán (Paid) và lưu lịch sử giao dịch.")
            }
            SectionTitle("Lịch sử trạng thái")
            inv.history.forEach { KeyValue(formatTime(it.ts), it.text) }
        }
    }
}

// =============== FR09 – Tenant: thanh toán VietQR ===============
@Composable
fun PayScreen(id: String) {
    val nav = LocalNav.current
    val inv = Store.invoice(id)
    val landlord = Store.user(inv?.landlordId)
    Scaffold(topBar = { AppTopBar("Thanh toán hóa đơn", nav::pop) }) { pad ->
        if (inv == null) { EmptyState("Không tìm thấy hóa đơn."); return@Scaffold }
        ScreenColumn(Modifier.padding(pad)) {
            Text("Mã hóa đơn: ${inv.code}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            KeyValue("Tổng tiền cần thanh toán", formatMoney(inv.total), bold = true)
            if (landlord == null || landlord.bankBin.isBlank() || landlord.bankAcc.isBlank()) {
                InfoBox("Chủ trọ chưa cập nhật tài khoản nhận tiền nên chưa thể sinh mã VietQR. Hãy nhắn tin cho chủ trọ.", AmberBg, Amber)
                OutlineButton("Nhắn tin cho chủ trọ", { landlord?.let { nav.push(Screen.Chat(it.id)) } })
            } else {
                val payload = VietQr.build(landlord.bankBin, landlord.bankAcc, inv.total, inv.code + " " + inv.period.replace("/", ""))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { QrImage(payload, Modifier.size(260.dp)) }
                Text("Mã VietQR sinh từ số tài khoản của Chủ trọ và tổng tiền; nội dung chuyển khoản gắn mã hóa đơn.",
                    color = Muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                KeyValue("Ngân hàng", landlord.bankName)
                KeyValue("Số tài khoản", landlord.bankAcc)
                KeyValue("Chủ tài khoản", landlord.name)
                KeyValue("Nội dung CK", inv.code + " " + inv.period.replace("/", ""))
            }
            KeyValue("Trạng thái hóa đơn", inv.status.label)
            if (inv.status == InvoiceStatus.ISSUED) InfoBox("Sau khi bạn chuyển khoản, chủ trọ sẽ xác nhận đã nhận tiền để chuyển hóa đơn sang Đã thanh toán.")
        }
    }
}
