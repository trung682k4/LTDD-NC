package com.rentconnect.app.model;

public enum TicketStatus {
    OPEN("Chờ xử lý"),
    IN_PROGRESS("Đang xử lý"),
    RESOLVED("Đã giải quyết");

    public final String label;

    TicketStatus(String label) {
        this.label = label;
    }
}
