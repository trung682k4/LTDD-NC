package com.rentconnect.app.model;

import java.util.*;

/** Dữ liệu Room (chuyển từ data class Kotlin; tên trường giữ nguyên để tương thích JSON/Gson). */
public class Room {
    public String id;
    public String ownerId;
    public String title;
    public String province;
    public String district;
    public String ward;
    public String address;
    public int area;
    public int capacity;
    public long rent;
    public long deposit;
    public long elecPrice;
    public long waterPrice;
    public List<Fee> fees;
    public List<String> amenities;
    public List<String> images;
    public RoomStatus status;
    public String adminNote;
    public long createdAt;

    public Room(String id, String ownerId, String title, String province, String district, String ward, String address, int area, int capacity, long rent, long deposit, long elecPrice, long waterPrice, List<Fee> fees, List<String> amenities, List<String> images, RoomStatus status) {
        this.id = id;
        this.ownerId = ownerId;
        this.title = title;
        this.province = province;
        this.district = district;
        this.ward = ward;
        this.address = address;
        this.area = area;
        this.capacity = capacity;
        this.rent = rent;
        this.deposit = deposit;
        this.elecPrice = elecPrice;
        this.waterPrice = waterPrice;
        this.fees = fees;
        this.amenities = amenities;
        this.images = images;
        this.status = status;
        this.adminNote = "";
        this.createdAt = System.currentTimeMillis();
    }

    public Room(String id, String ownerId, String title, String province, String district, String ward, String address, int area, int capacity, long rent, long deposit, long elecPrice, long waterPrice, List<Fee> fees, List<String> amenities, List<String> images, RoomStatus status, String adminNote, long createdAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.title = title;
        this.province = province;
        this.district = district;
        this.ward = ward;
        this.address = address;
        this.area = area;
        this.capacity = capacity;
        this.rent = rent;
        this.deposit = deposit;
        this.elecPrice = elecPrice;
        this.waterPrice = waterPrice;
        this.fees = fees;
        this.amenities = amenities;
        this.images = images;
        this.status = status;
        this.adminNote = adminNote;
        this.createdAt = createdAt;
    }

    private static String joinNonBlank(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p == null || p.trim().isEmpty()) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append(p);
        }
        return sb.toString();
    }

    public String getFullAddress() {
        return joinNonBlank(address, ward, district, province);
    }

    public String getAreaText() {
        return joinNonBlank(district, province);
    }
}
