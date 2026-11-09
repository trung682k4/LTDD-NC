package com.rentconnect.app.model;

public enum RoomStatus {
    PENDING("Chờ duyệt (Pending)"),
    ACTIVE("Đang hiển thị (Active)"),
    REJECTED("Bị từ chối"),
    NEED_INFO("Cần bổ sung");

    public final String label;

    RoomStatus(String label) {
        this.label = label;
    }
}
