package com.rentconnect.app.model;

/** Dữ liệu MeterReading (chuyển từ data class Kotlin; tên trường giữ nguyên để tương thích JSON/Gson). */
public class MeterReading {
    public String id;
    public String roomId;
    public String period;
    public int elecOld;
    public int elecNew;
    public int waterOld;
    public int waterNew;
    public String elecPhoto;
    public String waterPhoto;
    public long ts;
    public boolean synced;

    public MeterReading(String id, String roomId, String period, int elecOld, int elecNew, int waterOld, int waterNew, String elecPhoto, String waterPhoto, long ts, boolean synced) {
        this.id = id;
        this.roomId = roomId;
        this.period = period;
        this.elecOld = elecOld;
        this.elecNew = elecNew;
        this.waterOld = waterOld;
        this.waterNew = waterNew;
        this.elecPhoto = elecPhoto;
        this.waterPhoto = waterPhoto;
        this.ts = ts;
        this.synced = synced;
    }
}
