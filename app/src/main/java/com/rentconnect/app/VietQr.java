package com.rentconnect.app;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Sinh payload VietQR (chuẩn EMVCo / NAPAS 247) để vẽ thành mã QR. */
public final class VietQr {
    private VietQr() {
    }

    public static final class Bank {
        public final String name;
        public final String bin;

        public Bank(String name, String bin) {
            this.name = name;
            this.bin = bin;
        }
    }

    public static final List<Bank> BANKS = Collections.unmodifiableList(Arrays.asList(
            new Bank("Vietcombank", "970436"),
            new Bank("MB Bank", "970422"),
            new Bank("Techcombank", "970407"),
            new Bank("VietinBank", "970415"),
            new Bank("BIDV", "970418"),
            new Bank("ACB", "970416"),
            new Bank("TPBank", "970423"),
            new Bank("Agribank", "970405")
    ));

    private static String tlv(String tag, String value) {
        return tag + String.format(Locale.ROOT, "%02d", value.length()) + value;
    }

    public static String sanitize(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length() && sb.length() < 25; i++) {
            char c = text.charAt(i);
            sb.append(c >= 32 && c <= 126 ? c : ' ');
        }
        return sb.toString();
    }

    public static String crc16(String input) {
        int crc = 0xFFFF;
        for (byte b : input.getBytes(StandardCharsets.US_ASCII)) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                crc = (crc & 0x8000) != 0 ? (crc << 1) ^ 0x1021 : crc << 1;
                crc &= 0xFFFF;
            }
        }
        String hex = Integer.toHexString(crc).toUpperCase(Locale.ROOT);
        while (hex.length() < 4) hex = "0" + hex;
        return hex;
    }

    public static String build(String bankBin, String account, long amount, String content) {
        String beneficiary = tlv("00", bankBin) + tlv("01", account);
        String merchant = tlv("00", "A000000727") + tlv("01", beneficiary) + tlv("02", "QRIBFTTA");
        String body = tlv("00", "01")
                + tlv("01", "12")
                + tlv("38", merchant)
                + tlv("53", "704")
                + tlv("54", String.valueOf(amount))
                + tlv("58", "VN")
                + tlv("62", tlv("08", sanitize(content)))
                + "6304";
        return body + crc16(body);
    }
}
