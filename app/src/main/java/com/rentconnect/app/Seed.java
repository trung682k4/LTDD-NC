package com.rentconnect.app;

import com.rentconnect.app.model.AppState;
import com.rentconnect.app.model.Contract;
import com.rentconnect.app.model.ContractStatus;
import com.rentconnect.app.model.Fee;
import com.rentconnect.app.model.Invoice;
import com.rentconnect.app.model.InvoiceStatus;
import com.rentconnect.app.model.LogEntry;
import com.rentconnect.app.model.MeterReading;
import com.rentconnect.app.model.Notif;
import com.rentconnect.app.model.Role;
import com.rentconnect.app.model.Room;
import com.rentconnect.app.model.RoomStatus;
import com.rentconnect.app.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

/** Dữ liệu mẫu ban đầu + bộ dữ liệu giả lập (Mock dataset) cho kiểm thử. */
public final class Seed {
    private Seed() {
    }

    public static final String DEMO_PASSWORD = "123456";
    public static final String ADMIN = "admin@rent.vn";
    public static final String LANDLORD = "chutro@rent.vn";
    public static final String TENANT = "thue@rent.vn";

    /** Tỉnh → Quận/Huyện → danh sách Phường/Xã (giữ thứ tự khai báo). */
    public static final Map<String, Map<String, List<String>>> PROVINCES = new LinkedHashMap<>();

    static {
        Map<String, List<String>> hn = new LinkedHashMap<>();
        hn.put("Cầu Giấy", Arrays.asList("Dịch Vọng", "Mai Dịch", "Nghĩa Tân"));
        hn.put("Thanh Xuân", Arrays.asList("Khương Đình", "Nhân Chính", "Hạ Đình"));
        hn.put("Hà Đông", Arrays.asList("Mộ Lao", "Văn Quán", "Phú La"));
        PROVINCES.put("Hà Nội", hn);

        Map<String, List<String>> hcm = new LinkedHashMap<>();
        hcm.put("Thủ Đức", Arrays.asList("Linh Trung", "Hiệp Bình Chánh"));
        hcm.put("Bình Thạnh", Arrays.asList("Phường 25", "Phường 13"));
        PROVINCES.put("TP. Hồ Chí Minh", hcm);

        Map<String, List<String>> dn = new LinkedHashMap<>();
        dn.put("Hải Châu", Arrays.asList("Thạch Thang", "Hòa Cường Bắc"));
        dn.put("Sơn Trà", Arrays.asList("An Hải Bắc", "Mân Thái"));
        PROVINCES.put("Đà Nẵng", dn);
    }

    public static final List<String> AMENITY_LIST =
            Collections.unmodifiableList(Arrays.asList("Điều hòa", "Nóng lạnh", "Chỗ để xe", "Máy giặt", "Thang máy"));

    private static String h(String pw, String id) {
        return Crypto.hash(pw, id);
    }

    @SafeVarargs
    private static <T> List<T> listOf(T... items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    public static AppState build() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yyyy");
        LocalDate now = LocalDate.now();
        String prev = now.minusMonths(1).format(fmt);
        String prev2 = now.minusMonths(2).format(fmt);
        long ts = System.currentTimeMillis();
        long dayMs = 86_400_000L;

        List<User> users = listOf(
                new User("u1", "Quản trị viên", "000000000001", ADMIN, h(DEMO_PASSWORD, "u1"), Role.ADMIN),
                new User("u2", "Nguyễn Văn Chủ", "001090000002", LANDLORD, h(DEMO_PASSWORD, "u2"), Role.LANDLORD,
                        "MB Bank", "970422", "0123456789"),
                new User("u3", "Trần Thị Thuê", "001200000003", TENANT, h(DEMO_PASSWORD, "u3"), Role.TENANT),
                new User("u4", "Lê Minh Khách", "001200000004", "0912345678", h(DEMO_PASSWORD, "u4"), Role.TENANT)
        );
        List<Fee> fees = listOf(new Fee("Rác", 30_000), new Fee("Wifi", 100_000), new Fee("Gửi xe", 70_000));
        List<Room> rooms = listOf(
                new Room("r1", "u2", "Phòng 301 – Khép kín, gần ĐH Mở Hà Nội", "Hà Nội", "Hai Bà Trưng", "Bạch Mai",
                        "Số 12, ngõ 85 Đại Cồ Việt", 25, 3, 3_200_000, 3_200_000, 3_500, 20_000,
                        new ArrayList<>(fees),
                        listOf("Điều hòa", "Nóng lạnh", "Chỗ để xe"), new ArrayList<String>(), RoomStatus.ACTIVE),
                new Room("r2", "u2", "Phòng 202 – Gác xép, có ban công", "Hà Nội", "Cầu Giấy", "Dịch Vọng",
                        "Số 5, ngõ 120 Trần Thái Tông", 20, 2, 2_800_000, 2_800_000, 3_800, 25_000,
                        listOf(new Fee("Rác", 30_000), new Fee("Wifi", 100_000)),
                        listOf("Điều hòa", "Máy giặt"), new ArrayList<String>(), RoomStatus.ACTIVE),
                new Room("r3", "u2", "Căn hộ mini 401 – Có thang máy", "Hà Nội", "Thanh Xuân", "Nhân Chính",
                        "Số 30 Lê Văn Lương", 32, 4, 4_500_000, 4_500_000, 3_500, 22_000,
                        new ArrayList<>(fees),
                        listOf("Điều hòa", "Nóng lạnh", "Thang máy", "Máy giặt", "Chỗ để xe"), new ArrayList<String>(),
                        RoomStatus.PENDING)
        );
        LocalDate m2 = now.minusMonths(2);
        Contract contract = new Contract("c1", "r1", "u2", "u3",
                String.format(Locale.ROOT, "01/%02d/%d", m2.getMonthValue(), m2.getYear()),
                "31/12/" + (now.getYear() + 1), 3_200_000, 3_200_000, 1, 5,
                "Không nuôi thú cưng. Giữ yên tĩnh sau 23h.", ContractStatus.ACTIVE);
        List<MeterReading> readings = listOf(
                new MeterReading("m1", "r1", prev2, 0, 120, 0, 8, "", "", ts - 60 * dayMs, true),
                new MeterReading("m2", "r1", prev, 120, 245, 8, 17, "", "", ts - 30 * dayMs, true)
        );
        Invoice inv = new Invoice("i1", "c1", "r1", "u3", "u2", prev, 3_200_000, 125, 437_500, 9, 180_000, 200_000,
                0, "", 3_200_000L + 437_500L + 180_000L + 200_000L, InvoiceStatus.PAID,
                listOf(new LogEntry(ts - 29 * dayMs, "Xuất hóa đơn"),
                        new LogEntry(ts - 25 * dayMs, "Đã thanh toán (Paid)")));
        List<Notif> notifs = listOf(
                new Notif("n1", "u3", "Chào mừng đến RentConnect! Hợp đồng phòng 301 đang có hiệu lực.", ts - 2 * dayMs, false),
                new Notif("n2", "u1", "Tin đăng \"Căn hộ mini 401\" đang chờ kiểm duyệt.", ts - dayMs, false)
        );

        AppState s = new AppState();
        s.seq = 100;
        s.users = users;
        s.rooms = rooms;
        s.contracts = listOf(contract);
        s.readings = readings;
        s.invoices = listOf(inv);
        s.notifs = notifs;
        return s;
    }

    /** Mock dataset: 50 tài sản + 150 người dùng (Mục 1.4). Trả về AppState mới, không sửa state truyền vào. */
    public static AppState mock(AppState state, Supplier<String> nextId) {
        AppState result = state.copy();
        List<User> users = result.users;
        List<Room> rooms = result.rooms;
        Random rnd = new Random(42);
        List<String> owners = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            String id = "u" + nextId.get();
            owners.add(id);
            users.add(new User(id, "Chủ trọ mẫu " + (i + 1), "0010" + (1000 + i), "chu" + (i + 1) + "@mock.vn",
                    h(DEMO_PASSWORD, id), Role.LANDLORD));
        }
        for (int i = 0; i < 130; i++) {
            String id = "u" + nextId.get();
            users.add(new User(id, "Người thuê mẫu " + (i + 1), "0011" + (1000 + i), "09" + (10000000 + i),
                    h(DEMO_PASSWORD, id), Role.TENANT));
        }

        List<String> provinceNames = new ArrayList<>(PROVINCES.keySet());
        for (int i = 0; i < 50; i++) {
            String p = provinceNames.get(rnd.nextInt(provinceNames.size()));
            Map<String, List<String>> dMap = PROVINCES.get(p);
            List<String> dKeys = new ArrayList<>(dMap.keySet());
            String d = dKeys.get(rnd.nextInt(dMap.size()));
            List<String> wards = dMap.get(d);
            String w = wards.get(rnd.nextInt(wards.size()));
            long rent = (2 + rnd.nextInt(7)) * 1_000_000L + rnd.nextInt(2) * 500_000L;
            List<String> am = new ArrayList<>();
            for (String a : AMENITY_LIST) {
                if (rnd.nextBoolean()) am.add(a);
            }
            if (am.isEmpty()) am.add("Điều hòa");
            String roomId = "r" + nextId.get();
            String address = "Số " + (1 + rnd.nextInt(200)) + " đường " + w;
            int area = 15 + rnd.nextInt(30);
            int capacity = 1 + rnd.nextInt(4);
            rooms.add(new Room(roomId, owners.get(i % owners.size()), "Phòng trọ mẫu #" + (i + 1) + " – " + d,
                    p, d, w, address, area, capacity, rent, rent, 3_500, 20_000,
                    listOf(new Fee("Rác", 30_000), new Fee("Wifi", 100_000)), am, new ArrayList<String>(),
                    i % 7 == 0 ? RoomStatus.PENDING : RoomStatus.ACTIVE));
        }
        return result;
    }
}
