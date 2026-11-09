package com.rentconnect.app;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Các hàm định dạng dùng chung (trước đây nằm trong Ui.kt). */
public final class Fmt {
    private Fmt() {
    }

    private static final Locale VI = Locale.forLanguageTag("vi-VN");

    public static String formatMoney(long v) {
        return String.format(VI, "%,d", v) + " đ";
    }

    public static String formatTime(long ts) {
        return new SimpleDateFormat("HH:mm dd/MM/yyyy", VI).format(new Date(ts));
    }

    public static String digits(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (Character.isDigit(s.charAt(i))) sb.append(s.charAt(i));
        }
        return sb.toString();
    }

    /** Lấy tối đa 12 chữ số đầu của chuỗi và đổi sang long; rỗng/không hợp lệ → 0. */
    public static long toLong(String s) {
        String d = digits(s);
        if (d.length() > 12) d = d.substring(0, 12);
        if (d.isEmpty()) return 0L;
        try {
            return Long.parseLong(d);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
