package com.rentconnect.app.model;

/** Dữ liệu Contract (chuyển từ data class Kotlin; tên trường giữ nguyên để tương thích JSON/Gson). */
public class Contract {
    public String id;
    public String roomId;
    public String landlordId;
    public String tenantId;
    public String start;
    public String end;
    public long rent;
    public long deposit;
    public int cycleMonths;
    public int billingDay;
    public String terms;
    public ContractStatus status;
    public long createdAt;

    public Contract(String id, String roomId, String landlordId, String tenantId, String start, String end, long rent, long deposit, int cycleMonths, int billingDay, String terms, ContractStatus status) {
        this.id = id;
        this.roomId = roomId;
        this.landlordId = landlordId;
        this.tenantId = tenantId;
        this.start = start;
        this.end = end;
        this.rent = rent;
        this.deposit = deposit;
        this.cycleMonths = cycleMonths;
        this.billingDay = billingDay;
        this.terms = terms;
        this.status = status;
        this.createdAt = System.currentTimeMillis();
    }

    public Contract(String id, String roomId, String landlordId, String tenantId, String start, String end, long rent, long deposit, int cycleMonths, int billingDay, String terms, ContractStatus status, long createdAt) {
        this.id = id;
        this.roomId = roomId;
        this.landlordId = landlordId;
        this.tenantId = tenantId;
        this.start = start;
        this.end = end;
        this.rent = rent;
        this.deposit = deposit;
        this.cycleMonths = cycleMonths;
        this.billingDay = billingDay;
        this.terms = terms;
        this.status = status;
        this.createdAt = createdAt;
    }
}
