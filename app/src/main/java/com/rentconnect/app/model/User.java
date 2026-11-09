package com.rentconnect.app.model;

import java.util.*;

/** Dữ liệu User (chuyển từ data class Kotlin; tên trường giữ nguyên để tương thích JSON/Gson). */
public class User {
    public String id;
    public String name;
    public String idNumber;
    public String contact;
    public String passHash;
    public Role role;
    public String bankName;
    public String bankBin;
    public String bankAcc;

    public User(String id, String name, String idNumber, String contact, String passHash, Role role) {
        this.id = id;
        this.name = name;
        this.idNumber = idNumber;
        this.contact = contact;
        this.passHash = passHash;
        this.role = role;
        this.bankName = "";
        this.bankBin = "";
        this.bankAcc = "";
    }

    public User(String id, String name, String idNumber, String contact, String passHash, Role role, String bankName, String bankBin, String bankAcc) {
        this.id = id;
        this.name = name;
        this.idNumber = idNumber;
        this.contact = contact;
        this.passHash = passHash;
        this.role = role;
        this.bankName = bankName;
        this.bankBin = bankBin;
        this.bankAcc = bankAcc;
    }

    /** Số điện thoại nếu contact toàn là chữ số, ngược lại trả về chuỗi rỗng. */
    public String getPhone() {
        for (int i = 0; i < contact.length(); i++) {
            if (!Character.isDigit(contact.charAt(i))) return "";
        }
        return contact;
    }
}
