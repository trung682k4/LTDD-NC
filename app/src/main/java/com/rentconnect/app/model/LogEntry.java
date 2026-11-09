package com.rentconnect.app.model;

/** Dữ liệu LogEntry (chuyển từ data class Kotlin; tên trường giữ nguyên để tương thích JSON/Gson). */
public class LogEntry {
    public long ts;
    public String text;

    public LogEntry(long ts, String text) {
        this.ts = ts;
        this.text = text;
    }
}
