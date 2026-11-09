package com.rentconnect.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.rentconnect.app.model.Fee;

import org.junit.Test;

import java.util.Arrays;

public class BillingTest {
    @Test public void consumption_normal() { assertEquals(35, Billing.consumption(100, 135)); }

    @Test public void consumption_neverNegative() { assertEquals(0, Billing.consumption(200, 150)); }

    @Test public void rent_multipliesByCycle() { assertEquals(9_000_000L, Billing.rent(3_000_000L, 3)); }

    @Test public void utility_formula() { assertEquals(122_500L, Billing.utility(100, 135, 3_500L)); }

    @Test public void fees_sumPerMonth() {
        assertEquals(400_000L, Billing.fees(Arrays.asList(new Fee("Rác", 50_000), new Fee("Wifi", 150_000)), 2));
    }

    @Test public void total_withDiscount() {
        assertEquals(3_972_500L, Billing.total(3_000_000L, 122_500L, 150_000L, 700_000L, 0L));
    }

    @Test public void total_withAdjust() { assertEquals(1_900_000L, Billing.total(2_000_000L, 0, 0, 0, -100_000L)); }

    @Test public void vietqr_crcKnownVector() { assertEquals("29B1", VietQr.crc16("123456789")); }

    @Test public void vietqr_endsWithValidCrc() {
        String p = VietQr.build("970422", "0123456789", 3_972_500L, "HD101");
        assertEquals(VietQr.crc16(p.substring(0, p.length() - 4)), p.substring(p.length() - 4));
        assertTrue(p.contains("A000000727"));
        assertTrue(p.contains("54073972500"));
    }
}
