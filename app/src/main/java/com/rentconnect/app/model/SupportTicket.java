package com.rentconnect.app.model;

import java.util.*;

/** Dữ liệu SupportTicket (chuyển từ data class Kotlin; tên trường giữ nguyên để tương thích JSON/Gson). */
public class SupportTicket {
    public String id;
    public String userId;
    public String content;
    public List<String> attachments;
    public long ts;
    public String reply;
    public long replyTs;

    public SupportTicket(String id, String userId, String content, List<String> attachments) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.attachments = attachments;
        this.ts = System.currentTimeMillis();
        this.reply = "";
        this.replyTs = 0L;
    }

    public SupportTicket(String id, String userId, String content, List<String> attachments, long ts, String reply, long replyTs) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.attachments = attachments;
        this.ts = ts;
        this.reply = reply;
        this.replyTs = replyTs;
    }
}
