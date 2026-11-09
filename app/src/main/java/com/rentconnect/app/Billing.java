package com.rentconnect.app;

import com.rentconnect.app.model.Fee;

import java.util.List;

/** Thuật toán tính tiền (BM05). Java thuần để kiểm thử tự động được. */
public final class Billing {
    private Billing() {
    }

    public static int consumption(int oldValue, int newValue) {
        return newValue >= oldValue ? newValue - oldValue : 0;
    }

    /** Tiền thuê phòng = giá thuê × số tháng của chu kỳ thanh toán. */
    public static long rent(long rentPerMonth, int cycleMonths) {
        return rentPerMonth * Math.max(cycleMonths, 1);
    }

    /** Tiền điện / nước = (số mới − số cũ) × đơn giá. */
    public static long utility(int oldValue, int newValue, long unitPrice) {
        return (long) consumption(oldValue, newValue) * unitPrice;
    }

    public static long fees(List<Fee> fees, int cycleMonths) {
        long sum = 0;
        for (Fee f : fees) sum += f.amount;
        return sum * Math.max(cycleMonths, 1);
    }

    public static long total(long rent, long elec, long water, long fees, long adjust) {
        return rent + elec + water + fees + adjust;
    }
}
