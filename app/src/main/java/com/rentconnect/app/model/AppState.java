package com.rentconnect.app.model;

import java.util.ArrayList;
import java.util.List;

/** Toàn bộ dữ liệu ứng dụng; được tuần tự hóa bằng Gson rồi mã hóa AES-GCM. */
public class AppState {
    public long seq = 100;
    public List<User> users = new ArrayList<>();
    public List<Room> rooms = new ArrayList<>();
    public List<Viewing> viewings = new ArrayList<>();
    public List<Message> messages = new ArrayList<>();
    public List<SupportTicket> supports = new ArrayList<>();
    public List<Contract> contracts = new ArrayList<>();
    public List<MeterReading> readings = new ArrayList<>();
    public List<Invoice> invoices = new ArrayList<>();
    public List<Ticket> tickets = new ArrayList<>();
    public List<Notif> notifs = new ArrayList<>();

    public AppState() {
    }

    /** Sao chép nông: danh sách mới nhưng phần tử dùng chung. */
    public AppState copy() {
        AppState s = new AppState();
        s.seq = seq;
        s.users = new ArrayList<>(users);
        s.rooms = new ArrayList<>(rooms);
        s.viewings = new ArrayList<>(viewings);
        s.messages = new ArrayList<>(messages);
        s.supports = new ArrayList<>(supports);
        s.contracts = new ArrayList<>(contracts);
        s.readings = new ArrayList<>(readings);
        s.invoices = new ArrayList<>(invoices);
        s.tickets = new ArrayList<>(tickets);
        s.notifs = new ArrayList<>(notifs);
        return s;
    }
}
