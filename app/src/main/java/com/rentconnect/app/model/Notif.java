package com.rentconnect.app.model;

/** Dữ liệu Notif (chuyển từ data class Kotlin; tên trường giữ nguyên để tương thích JSON/Gson). */
public class Notif {
    public String id;
    public String userId;
    public String text;
    public long ts;
    public boolean read;

    public Notif(String id, String userId, String text) {
        this.id = id;
        this.userId = userId;
        this.text = text;
        this.ts = System.currentTimeMillis();
        this.read = false;
    }

    public Notif(String id, String userId, String text, long ts, boolean read) {
        this.id = id;
        this.userId = userId;
        this.text = text;
        this.ts = ts;
        this.read = read;
    }
}
