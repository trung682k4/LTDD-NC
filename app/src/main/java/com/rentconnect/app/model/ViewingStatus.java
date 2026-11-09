package com.rentconnect.app.model;

public enum ViewingStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed");

    public final String label;

    ViewingStatus(String label) {
        this.label = label;
    }
}
