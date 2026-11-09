package com.rentconnect.app.model;

public enum ContractStatus {
    DRAFT("Bản nháp"),
    ACTIVE("Có hiệu lực (Active)");

    public final String label;

    ContractStatus(String label) {
        this.label = label;
    }
}
