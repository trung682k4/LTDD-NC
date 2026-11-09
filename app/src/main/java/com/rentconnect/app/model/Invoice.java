package com.rentconnect.app.model;

import java.util.*;

/** Dữ liệu Invoice (chuyển từ data class Kotlin; tên trường giữ nguyên để tương thích JSON/Gson). */
public class Invoice {
    public String id;
    public String contractId;
    public String roomId;
    public String tenantId;
    public String landlordId;
    public String period;
    public long rentAmt;
    public int elecUsed;
    public long elecAmt;
    public int waterUsed;
    public long waterAmt;
    public long feeAmt;
    public long adjust;
    public String adjustReason;
    public long total;
    public InvoiceStatus status;
    public List<LogEntry> history;

    public Invoice(String id, String contractId, String roomId, String tenantId, String landlordId, String period, long rentAmt, int elecUsed, long elecAmt, int waterUsed, long waterAmt, long feeAmt, long adjust, String adjustReason, long total, InvoiceStatus status, List<LogEntry> history) {
        this.id = id;
        this.contractId = contractId;
        this.roomId = roomId;
        this.tenantId = tenantId;
        this.landlordId = landlordId;
        this.period = period;
        this.rentAmt = rentAmt;
        this.elecUsed = elecUsed;
        this.elecAmt = elecAmt;
        this.waterUsed = waterUsed;
        this.waterAmt = waterAmt;
        this.feeAmt = feeAmt;
        this.adjust = adjust;
        this.adjustReason = adjustReason;
        this.total = total;
        this.status = status;
        this.history = history;
    }

    /** Mã hóa đơn hiển thị: "HD" + các chữ số trong id. */
    public String getCode() {
        StringBuilder sb = new StringBuilder("HD");
        for (int i = 0; i < id.length(); i++) {
            if (Character.isDigit(id.charAt(i))) sb.append(id.charAt(i));
        }
        return sb.toString();
    }
}
