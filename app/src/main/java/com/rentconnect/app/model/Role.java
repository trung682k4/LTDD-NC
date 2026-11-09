package com.rentconnect.app.model;

public enum Role {
    TENANT("Người thuê (Tenant)"),
    LANDLORD("Chủ trọ (Landlord)"),
    ADMIN("Quản trị (Admin)");

    public final String label;

    Role(String label) {
        this.label = label;
    }
}
