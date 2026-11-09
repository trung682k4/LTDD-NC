package com.rentconnect.app.model;

/** Dữ liệu Fee (chuyển từ data class Kotlin; tên trường giữ nguyên để tương thích JSON/Gson). */
public class Fee {
    public String name;
    public long amount;

    public Fee(String name, long amount) {
        this.name = name;
        this.amount = amount;
    }
}
