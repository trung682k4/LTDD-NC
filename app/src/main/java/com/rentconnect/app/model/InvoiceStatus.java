package com.rentconnect.app.model;

public enum InvoiceStatus {
    DRAFT("Bản nháp"),
    ISSUED("Chưa thanh toán"),
    PAID("Đã thanh toán (Paid)");

    public final String label;

    InvoiceStatus(String label) {
        this.label = label;
    }
}
