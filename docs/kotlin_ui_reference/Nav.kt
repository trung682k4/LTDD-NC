package com.rentconnect.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

sealed interface Screen {
    data object RoomForm : Screen
    data class RoomDetail(val roomId: String) : Screen
    data class Moderate(val roomId: String) : Screen
    data class Booking(val roomId: String) : Screen
    data object Viewings : Screen
    data class ContractForm(val roomId: String) : Screen
    data object ContractList : Screen
    data class ContractView(val id: String) : Screen
    data object MeterPick : Screen
    data class MeterForm(val roomId: String) : Screen
    data object MyMeter : Screen
    data class InvoiceDetail(val id: String) : Screen
    data class Pay(val id: String) : Screen
    data object TicketForm : Screen
    data class TicketSent(val id: String) : Screen
    data class TicketDetail(val id: String) : Screen
    data class Chat(val partnerId: String) : Screen
    data object SupportForm : Screen
    data class SupportDetail(val id: String) : Screen
    data object Notifications : Screen
}

class Nav {
    val stack = mutableStateListOf<Screen>()
    var tab by mutableIntStateOf(0)
    fun push(s: Screen) { stack.add(s) }
    fun pop() { if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex) }
}

val LocalNav = staticCompositionLocalOf<Nav> { error("Nav not provided") }
