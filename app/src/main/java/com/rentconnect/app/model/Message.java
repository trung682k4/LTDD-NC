package com.rentconnect.app.model;

/** Dữ liệu Message (chuyển từ data class Kotlin; tên trường giữ nguyên để tương thích JSON/Gson). */
public class Message {
    public String id;
    public String fromId;
    public String toId;
    public String text;
    public long ts;

    public Message(String id, String fromId, String toId, String text) {
        this.id = id;
        this.fromId = fromId;
        this.toId = toId;
        this.text = text;
        this.ts = System.currentTimeMillis();
    }

    public Message(String id, String fromId, String toId, String text, long ts) {
        this.id = id;
        this.fromId = fromId;
        this.toId = toId;
        this.text = text;
        this.ts = ts;
    }
}
