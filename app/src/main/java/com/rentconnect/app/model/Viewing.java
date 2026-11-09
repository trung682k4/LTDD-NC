package com.rentconnect.app.model;

/** Dữ liệu Viewing (chuyển từ data class Kotlin; tên trường giữ nguyên để tương thích JSON/Gson). */
public class Viewing {
    public String id;
    public String roomId;
    public String tenantId;
    public String date;
    public String slot;
    public String phone;
    public String note;
    public ViewingStatus status;
    public String response;
    public long createdAt;

    public Viewing(String id, String roomId, String tenantId, String date, String slot, String phone, String note, ViewingStatus status) {
        this.id = id;
        this.roomId = roomId;
        this.tenantId = tenantId;
        this.date = date;
        this.slot = slot;
        this.phone = phone;
        this.note = note;
        this.status = status;
        this.response = "";
        this.createdAt = System.currentTimeMillis();
    }

    public Viewing(String id, String roomId, String tenantId, String date, String slot, String phone, String note, ViewingStatus status, String response, long createdAt) {
        this.id = id;
        this.roomId = roomId;
        this.tenantId = tenantId;
        this.date = date;
        this.slot = slot;
        this.phone = phone;
        this.note = note;
        this.status = status;
        this.response = response;
        this.createdAt = createdAt;
    }
}
