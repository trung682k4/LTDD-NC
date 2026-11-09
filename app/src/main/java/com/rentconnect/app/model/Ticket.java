package com.rentconnect.app.model;

import java.util.*;

/** Dữ liệu Ticket (chuyển từ data class Kotlin; tên trường giữ nguyên để tương thích JSON/Gson). */
public class Ticket {
    public String id;
    public String roomId;
    public String tenantId;
    public String landlordId;
    public String category;
    public String priority;
    public String desc;
    public List<String> photos;
    public TicketStatus status;
    public List<LogEntry> history;

    public Ticket(String id, String roomId, String tenantId, String landlordId, String category, String priority, String desc, List<String> photos, TicketStatus status, List<LogEntry> history) {
        this.id = id;
        this.roomId = roomId;
        this.tenantId = tenantId;
        this.landlordId = landlordId;
        this.category = category;
        this.priority = priority;
        this.desc = desc;
        this.photos = photos;
        this.status = status;
        this.history = history;
    }
}
