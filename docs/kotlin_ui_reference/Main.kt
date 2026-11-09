package com.rentconnect.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class TabDef(val label: String, val icon: ImageVector, val content: @Composable () -> Unit)

@Composable
fun Root() {
    val user = Store.me
    if (user == null) AuthHost() else MainHost(user)
}

@Composable
fun MainHost(user: User) {
    val nav = remember(user.id) { Nav() }
    CompositionLocalProvider(LocalNav provides nav) {
        BackHandler(enabled = nav.stack.isNotEmpty()) { nav.pop() }
        val top = nav.stack.lastOrNull()
        if (top != null) ScreenHost(top) else TabsHost(user, nav)
    }
}

@Composable
private fun TabsHost(user: User, nav: Nav) {
    val tabs: List<TabDef> = when (user.role) {
        Role.TENANT -> listOf(
            TabDef("Tìm phòng", Icons.Filled.Search) { SearchTab() },
            TabDef("Hóa đơn", Icons.Filled.List) { InvoiceListTab() },
            TabDef("Sự cố", Icons.Filled.Warning) { TicketListTab() },
            TabDef("Tin nhắn", Icons.Filled.Email) { ChatListTab() },
            TabDef("Thêm", Icons.Filled.Menu) { MoreTab(user) }
        )
        Role.LANDLORD -> listOf(
            TabDef("Phòng", Icons.Filled.Home) { MyRoomsTab() },
            TabDef("Hóa đơn", Icons.Filled.List) { InvoiceListTab() },
            TabDef("Sự cố", Icons.Filled.Warning) { TicketListTab() },
            TabDef("Tin nhắn", Icons.Filled.Email) { ChatListTab() },
            TabDef("Thêm", Icons.Filled.Menu) { MoreTab(user) }
        )
        Role.ADMIN -> listOf(
            TabDef("Kiểm duyệt", Icons.Filled.Check) { ModerationTab() },
            TabDef("Hỗ trợ", Icons.Filled.Email) { ChatListTab() },
            TabDef("Thêm", Icons.Filled.Menu) { MoreTab(user) }
        )
    }
    val idx = nav.tab.coerceIn(0, tabs.lastIndex)
    Scaffold(
        topBar = {
            AppTopBar(tabs[idx].label, null) {
                IconButton(onClick = { nav.push(Screen.Notifications) }) {
                    val n = Store.unreadCount()
                    BadgedBox(badge = { if (n > 0) Badge { Text("$n") } }) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Thông báo")
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { i, t ->
                    NavigationBarItem(selected = i == idx, onClick = { nav.tab = i },
                        icon = { Icon(t.icon, t.label) }, label = { Text(t.label) })
                }
            }
        }
    ) { pad ->
        androidx.compose.foundation.layout.Column(Modifier.padding(pad)) {
            ConnectionBannerSlot()
            tabs[idx].content()
        }
    }
}

@Composable
private fun ConnectionBannerSlot() {
    if (!Store.isOnline) {
        androidx.compose.foundation.layout.Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            InfoBox("Ngoại tuyến – dữ liệu lưu trên máy, tự đồng bộ khi có mạng.", AmberBg, Amber)
        }
    }
}

@Composable
private fun ScreenHost(s: Screen) {
    when (s) {
        Screen.RoomForm -> RoomFormScreen()
        is Screen.RoomDetail -> RoomDetailScreen(s.roomId)
        is Screen.Moderate -> ModerateScreen(s.roomId)
        is Screen.Booking -> BookingScreen(s.roomId)
        Screen.Viewings -> ViewingsScreen()
        is Screen.ContractForm -> ContractFormScreen(s.roomId)
        Screen.ContractList -> ContractListScreen()
        is Screen.ContractView -> ContractViewScreen(s.id)
        Screen.MeterPick -> MeterPickScreen()
        is Screen.MeterForm -> MeterFormScreen(s.roomId)
        Screen.MyMeter -> MyMeterScreen()
        is Screen.InvoiceDetail -> InvoiceDetailScreen(s.id)
        is Screen.Pay -> PayScreen(s.id)
        Screen.TicketForm -> TicketFormScreen()
        is Screen.TicketSent -> TicketSentScreen(s.id)
        is Screen.TicketDetail -> TicketDetailScreen(s.id)
        is Screen.Chat -> ChatScreen(s.partnerId)
        Screen.SupportForm -> SupportFormScreen()
        is Screen.SupportDetail -> SupportDetailScreen(s.id)
        Screen.Notifications -> NotificationsScreen()
    }
}

// =============== tab "Thêm" ===============
@Composable
private fun MoreTab(user: User) {
    val nav = LocalNav.current
    val ctx = LocalContext.current
    var bankDialog by remember { mutableStateOf(false) }
    ScreenColumn {
        ItemCard {
            Text(user.name, fontWeight = FontWeight.Bold)
            Text("${user.contact} · ${user.role.label}", color = Muted)
        }
        when (user.role) {
            Role.TENANT -> {
                MenuRow("Lịch xem phòng của tôi") { nav.push(Screen.Viewings) }
                MenuRow("Hợp đồng của tôi") { nav.push(Screen.ContractList) }
                MenuRow("Điện nước của tôi") { nav.push(Screen.MyMeter) }
            }
            Role.LANDLORD -> {
                MenuRow("Lịch hẹn xem phòng") { nav.push(Screen.Viewings) }
                MenuRow("Hợp đồng") { nav.push(Screen.ContractList) }
                MenuRow("Chốt chỉ số điện nước") { nav.push(Screen.MeterPick) }
                MenuRow("Tài khoản nhận tiền (VietQR)" + if (user.bankAcc.isBlank()) " – chưa cập nhật" else " – ${user.bankName} ${user.bankAcc}") { bankDialog = true }
            }
            Role.ADMIN -> {
                MenuRow("Tạo dữ liệu giả lập (50 phòng, 150 người dùng)") {
                    if (Store.state.users.size < 100) { Store.generateMock(); toast(ctx, "Đã tạo Mock dataset.") }
                    else toast(ctx, "Dữ liệu giả lập đã tồn tại.")
                }
            }
        }
        MenuRow("Thông báo") { nav.push(Screen.Notifications) }
        OfflineToggle()
        if (Store.pendingCount() > 0) PrimaryButton("Đồng bộ ngay (${Store.pendingCount()} bản ghi)", { Store.syncPending() })
        InfoBox("Dữ liệu được lưu cục bộ và mã hóa AES-256 (Android Keystore).")
        OutlineButton("Đăng xuất", { Store.logout() }, color = Red)
    }
    if (bankDialog) BankDialog(user) { bankDialog = false }
}

@Composable
private fun MenuRow(text: String, onClick: () -> Unit) {
    ItemCard(onClick = onClick) { Text(text, fontWeight = FontWeight.Medium) }
}

@Composable
private fun BankDialog(user: User, onClose: () -> Unit) {
    var bank by remember { mutableStateOf(user.bankName) }
    var acc by remember { mutableStateOf(user.bankAcc) }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Tài khoản nhận tiền") },
        text = {
            androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DropdownField("Ngân hàng", VietQr.banks.map { it.first }, bank, { bank = it })
                LabeledField("Số tài khoản", acc, { acc = digits(it).take(19) }, keyboard = androidx.compose.ui.text.input.KeyboardType.Number)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val bin = VietQr.banks.firstOrNull { it.first == bank }?.second ?: ""
                if (bin.isNotBlank() && acc.length >= 6) Store.updateBank(bank, bin, acc)
                onClose()
            }) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Hủy") } }
    )
}

// =============== thông báo ===============
@Composable
fun NotificationsScreen() {
    val nav = LocalNav.current
    val list = Store.myNotifs()
    Scaffold(topBar = {
        AppTopBar("Thông báo", nav::pop) { TextButton(onClick = { Store.markAllRead() }) { Text("Đã đọc hết") } }
    }) { pad ->
        ScreenColumn(Modifier.padding(pad)) {
            if (list.isEmpty()) EmptyState("Chưa có thông báo.")
            list.forEach { n ->
                ItemCard {
                    Text(n.text, fontWeight = if (n.read) FontWeight.Normal else FontWeight.Bold)
                    Text(formatTime(n.ts), color = Muted)
                }
            }
        }
    }
}
