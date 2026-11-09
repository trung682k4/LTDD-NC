package com.rentconnect.app;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.rentconnect.app.model.AppState;
import com.rentconnect.app.model.Contract;
import com.rentconnect.app.model.ContractStatus;
import com.rentconnect.app.model.Invoice;
import com.rentconnect.app.model.InvoiceStatus;
import com.rentconnect.app.model.LogEntry;
import com.rentconnect.app.model.Message;
import com.rentconnect.app.model.MeterReading;
import com.rentconnect.app.model.Notif;
import com.rentconnect.app.model.Role;
import com.rentconnect.app.model.Room;
import com.rentconnect.app.model.RoomStatus;
import com.rentconnect.app.model.SupportTicket;
import com.rentconnect.app.model.Ticket;
import com.rentconnect.app.model.TicketStatus;
import com.rentconnect.app.model.User;
import com.rentconnect.app.model.Viewing;
import com.rentconnect.app.model.ViewingStatus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Kho dữ liệu trung tâm + toàn bộ nghiệp vụ P01–P09.
 * Dữ liệu lưu cục bộ (Local Storage) đã mã hóa AES-GCM; ứng dụng chạy được hoàn toàn ngoại tuyến.
 * Phần "máy chủ" (đồng bộ, định tuyến tin nhắn, thông báo) được mô phỏng trong cùng thiết bị.
 *
 * <p>Thay cho {@code mutableStateOf} của Compose: UI đăng ký {@link Listener} (thường ở onStart,
 * hủy ở onStop) để được gọi lại mỗi khi dữ liệu thay đổi. Mọi hàm nên gọi trên main thread.
 */
public final class Store {
    private Store() {
    }

    /** Được gọi sau mỗi lần dữ liệu / trạng thái đăng nhập / trạng thái mạng thay đổi. */
    public interface Listener {
        void onStoreChanged();
    }

    /** Kết quả tạo phiếu sự cố (thay cho Pair&lt;String?, String?&gt; của Kotlin). */
    public static final class TicketResult {
        public final String error;     // null nếu thành công
        public final String ticketId;  // null nếu lỗi

        TicketResult(String error, String ticketId) {
            this.error = error;
            this.ticketId = ticketId;
        }
    }

    /** Bộ lọc tìm phòng (P03). */
    public static final class Filter {
        public String keyword = "";
        public String province = "";
        public String district = "";
        public long minPrice = 0;
        public long maxPrice = Long.MAX_VALUE;
        public int minArea = 0;
        public Set<String> amenities = new HashSet<>();
    }

    private static final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private static Context appCtx;
    private static final Gson gson = new Gson();

    private static AppState state = new AppState();
    private static String currentUserId = null;
    private static boolean netOnline = true;
    private static boolean forceOffline = false;

    /** Mã OTP của lần đăng ký đang chờ (hiển thị ở chế độ demo vì chưa có dịch vụ gửi email/SMS). */
    private static String lastOtp = "";
    private static User pendingReg = null;

    // ---------- listener ----------
    public static void addListener(Listener l) {
        if (!listeners.contains(l)) listeners.add(l);
    }

    public static void removeListener(Listener l) {
        listeners.remove(l);
    }

    private static void fireChanged() {
        for (Listener l : listeners) l.onStoreChanged();
    }

    // ---------- trạng thái truy cập ----------
    public static AppState state() {
        return state;
    }

    public static String currentUserId() {
        return currentUserId;
    }

    public static String lastOtp() {
        return lastOtp;
    }

    public static boolean isNetOnline() {
        return netOnline;
    }

    public static void setNetOnline(boolean v) {
        netOnline = v;
        fireChanged();
    }

    public static boolean isForceOffline() {
        return forceOffline;
    }

    public static void setForceOffline(boolean v) {
        forceOffline = v;
        fireChanged();
    }

    public static boolean isOnline() {
        return netOnline && !forceOffline;
    }

    // ---------- khởi tạo / lưu trữ ----------
    public static void init(Context ctx) {
        appCtx = ctx.getApplicationContext() != null ? ctx.getApplicationContext() : ctx;
        File f = new File(appCtx.getFilesDir(), "state.enc");
        AppState loaded = null;
        if (f.exists()) {
            try {
                String json = new String(Crypto.decrypt(Files.readAllBytes(f.toPath())), StandardCharsets.UTF_8);
                loaded = gson.fromJson(json, AppState.class);
            } catch (Exception e) {
                loaded = null;
            }
        }
        state = loaded != null ? loaded : Seed.build();
        if (loaded == null) save();
        fireChanged();
    }

    private static void save() {
        try {
            byte[] enc = Crypto.encrypt(gson.toJson(state).getBytes(StandardCharsets.UTF_8));
            Files.write(new File(appCtx.getFilesDir(), "state.enc").toPath(), enc);
        } catch (Exception ignored) {
        }
    }

    /** Lưu xuống đĩa rồi báo cho UI. Gọi sau mỗi lần sửa {@link #state}. */
    private static void commit() {
        save();
        fireChanged();
    }

    private static String nextNum() {
        state.seq += 1;
        return String.valueOf(state.seq);
    }

    private static String nextId(String prefix) {
        return prefix + nextNum();
    }

    private static SharedPreferences prefs() {
        return appCtx.getSharedPreferences("rc_prefs", Context.MODE_PRIVATE);
    }

    private static String lower(String s) {
        return s.toLowerCase(Locale.ROOT);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static boolean allDigits(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) return false;
        }
        return true;
    }

    private static int digitCount(String s) {
        return Fmt.digits(s).length();
    }

    private static String nameOf(String userId) {
        User u = user(userId);
        return u != null ? u.name : "";
    }

    // ---------- tra cứu ----------
    public static User user(String id) {
        if (id == null) return null;
        for (User u : state.users) if (u.id.equals(id)) return u;
        return null;
    }

    public static User me() {
        return user(currentUserId);
    }

    public static Room room(String id) {
        if (id == null) return null;
        for (Room r : state.rooms) if (r.id.equals(id)) return r;
        return null;
    }

    public static Contract contract(String id) {
        if (id == null) return null;
        for (Contract c : state.contracts) if (c.id.equals(id)) return c;
        return null;
    }

    public static Invoice invoice(String id) {
        if (id == null) return null;
        for (Invoice i : state.invoices) if (i.id.equals(id)) return i;
        return null;
    }

    public static Ticket ticket(String id) {
        if (id == null) return null;
        for (Ticket t : state.tickets) if (t.id.equals(id)) return t;
        return null;
    }

    public static SupportTicket support(String id) {
        if (id == null) return null;
        for (SupportTicket s : state.supports) if (s.id.equals(id)) return s;
        return null;
    }

    public static Contract activeContractOfTenant(String tenantId) {
        Contract found = null;
        for (Contract c : state.contracts) {
            if (c.tenantId.equals(tenantId) && c.status == ContractStatus.ACTIVE) found = c;
        }
        return found;
    }

    public static Contract activeContractOfRoom(String roomId) {
        Contract found = null;
        for (Contract c : state.contracts) {
            if (c.roomId.equals(roomId) && c.status == ContractStatus.ACTIVE) found = c;
        }
        return found;
    }

    // ---------- P01: tài khoản & phân quyền ----------
    private static boolean validContact(String c) {
        boolean email = c.contains("@") && c.contains(".") && c.length() >= 6;
        boolean phone = allDigits(c) && c.length() >= 9 && c.length() <= 11;
        return email || phone;
    }

    /** @return null nếu thành công, ngược lại là thông báo lỗi. */
    public static String startRegister(String name, String idNumber, String contact, String password, Role role) {
        if (isBlank(name)) return "Vui lòng nhập thông tin cá nhân (họ tên).";
        if (idNumber.length() < 9) return "Thông tin định danh (CCCD/CMND) không hợp lệ.";
        String c = lower(contact.trim());
        if (!validContact(c)) return "Email hoặc số điện thoại không hợp lệ.";
        if (password.length() < 6) return "Mật khẩu tối thiểu 6 ký tự.";
        if (role == Role.ADMIN) return "Không thể tự đăng ký vai trò Admin.";
        for (User u : state.users) {
            if (lower(u.contact).equals(c)) return "Tài khoản đã tồn tại.";
        }
        String id = nextId("u");
        pendingReg = new User(id, name.trim(), idNumber.trim(), c, Crypto.hash(password, id), role);
        lastOtp = String.valueOf(100000 + new SecureRandom().nextInt(900000));
        Notifier.show(appCtx, 9001, "Mã xác thực RentConnect của bạn: " + lastOtp);
        fireChanged();
        return null;
    }

    public static String verifyOtp(String code) {
        User p = pendingReg;
        if (p == null) return "Phiên đăng ký đã hết hạn, vui lòng đăng ký lại.";
        if (!code.equals(lastOtp)) return "Mã OTP không đúng.";
        state.users.add(p);
        pendingReg = null;
        lastOtp = "";
        commit();
        return null;
    }

    public static String login(String contact, String password) {
        String c = lower(contact.trim());
        User u = null;
        for (User x : state.users) {
            if (lower(x.contact).equals(c)) {
                u = x;
                break;
            }
        }
        if (u == null) return "Tài khoản không tồn tại.";
        if (!u.passHash.equals(Crypto.hash(password, u.id))) return "Sai mật khẩu.";
        currentUserId = u.id;
        prefs().edit().putString("bio_user", u.id).apply();
        syncPending();
        fireChanged();
        return null;
    }

    public static User biometricUser() {
        return user(prefs().getString("bio_user", null));
    }

    public static String loginBiometric() {
        User u = biometricUser();
        if (u == null) return "Hãy đăng nhập bằng mật khẩu một lần để bật vân tay/khuôn mặt.";
        currentUserId = u.id;
        syncPending();
        fireChanged();
        return null;
    }

    public static void logout() {
        currentUserId = null;
        fireChanged();
    }

    public static void updateBank(String bankName, String bin, String acc) {
        String id = currentUserId;
        if (id == null) return;
        User u = user(id);
        if (u == null) return;
        u.bankName = bankName;
        u.bankBin = bin;
        u.bankAcc = acc;
        commit();
    }

    // ---------- thông báo ----------
    public static void notify(String userId, String text) {
        state.notifs.add(new Notif(nextId("n"), userId, text));
        commit();
        if (userId != null && userId.equals(currentUserId)) Notifier.show(appCtx, (int) state.seq, text);
    }

    public static List<Notif> myNotifs() {
        List<Notif> out = new ArrayList<>();
        for (Notif n : state.notifs) if (n.userId.equals(currentUserId)) out.add(n);
        Collections.sort(out, (a, b) -> Long.compare(b.ts, a.ts));
        return out;
    }

    public static int unreadCount() {
        int c = 0;
        for (Notif n : state.notifs) if (n.userId.equals(currentUserId) && !n.read) c++;
        return c;
    }

    public static void markAllRead() {
        String id = currentUserId;
        if (id == null) return;
        for (Notif n : state.notifs) if (n.userId.equals(id)) n.read = true;
        commit();
    }

    // ---------- P02: đăng tin & kiểm duyệt ----------
    public static String submitRoom(Room r) {
        if (isBlank(r.title)) return "Vui lòng nhập tiêu đề tin đăng.";
        if (isBlank(r.province) || isBlank(r.district)) return "Vui lòng chọn Tỉnh/Thành phố và Quận/Huyện.";
        if (r.area <= 0 || r.capacity <= 0) return "Diện tích và sức chứa phải lớn hơn 0.";
        if (r.rent <= 0) return "Vui lòng nhập giá thuê.";
        if (r.elecPrice <= 0 || r.waterPrice <= 0) return "Vui lòng nhập đơn giá điện và nước.";
        r.id = nextId("r");
        r.status = RoomStatus.PENDING;
        state.rooms.add(r);
        commit();
        for (User u : new ArrayList<>(state.users)) {
            if (u.role == Role.ADMIN) notify(u.id, "Tin đăng mới \"" + r.title + "\" đang chờ kiểm duyệt.");
        }
        return null;
    }

    public static void moderate(String roomId, RoomStatus status, String note) {
        Room r = room(roomId);
        if (r == null) return;
        r.status = status;
        r.adminNote = note;
        commit();
        String msg;
        switch (status) {
            case ACTIVE:
                msg = "Tin đăng \"" + r.title + "\" đã được phê duyệt và đang hiển thị.";
                break;
            case REJECTED:
                msg = ("Tin đăng \"" + r.title + "\" bị từ chối. " + note).trim();
                break;
            case NEED_INFO:
                msg = ("Tin đăng \"" + r.title + "\" cần bổ sung: " + note).trim();
                break;
            default:
                return;
        }
        notify(r.ownerId, msg);
    }

    public static void moderate(String roomId, RoomStatus status) {
        moderate(roomId, status, "");
    }

    public static List<Room> roomsOf(String ownerId) {
        List<Room> out = new ArrayList<>();
        for (Room r : state.rooms) if (r.ownerId.equals(ownerId)) out.add(r);
        Collections.sort(out, (a, b) -> Long.compare(b.createdAt, a.createdAt));
        return out;
    }

    // ---------- P03: tìm kiếm, lọc ----------
    private static final class Scored {
        final Room room;
        final int score;

        Scored(Room room, int score) {
            this.room = room;
            this.score = score;
        }
    }

    public static List<Room> search(Filter f) {
        String kw = lower(f.keyword.trim());
        List<Scored> scored = new ArrayList<>();
        for (Room r : state.rooms) {
            if (r.status != RoomStatus.ACTIVE || activeContractOfRoom(r.id) != null) continue;
            if (!isBlank(f.province) && !r.province.equals(f.province)) continue;
            if (!isBlank(f.district) && !r.district.equals(f.district)) continue;
            if (r.rent < f.minPrice || r.rent > f.maxPrice) continue;
            if (r.area < f.minArea) continue;
            if (!r.amenities.containsAll(f.amenities)) continue;
            int score = 0;
            if (!kw.isEmpty()) {
                String hay = lower(r.title + " " + r.getFullAddress());
                for (String w : kw.split(" ")) {
                    if (!isBlank(w) && hay.contains(w)) score++;
                }
                if (score == 0) continue;
            }
            scored.add(new Scored(r, score));
        }
        Collections.sort(scored, new Comparator<Scored>() {
            @Override
            public int compare(Scored a, Scored b) {
                if (a.score != b.score) return Integer.compare(b.score, a.score);
                return Long.compare(b.room.createdAt, a.room.createdAt);
            }
        });
        List<Room> out = new ArrayList<>();
        for (Scored s : scored) out.add(s.room);
        return out;
    }

    // ---------- P04: đặt lịch xem phòng ----------
    public static final List<String> SLOTS = Collections.unmodifiableList(Arrays.asList(
            "08:00 - 09:00", "09:00 - 10:00", "10:00 - 11:00", "14:00 - 15:00", "15:00 - 16:00", "16:00 - 17:00"));

    public static String bookViewing(String roomId, String date, String slot, String phone, String note) {
        String tenant = currentUserId;
        if (tenant == null) return "Chưa đăng nhập.";
        if (isBlank(date) || isBlank(slot)) return "Vui lòng chọn ngày và khung giờ.";
        if (digitCount(phone) < 9) return "Số điện thoại liên hệ không hợp lệ.";
        for (Viewing v : state.viewings) {
            if (v.roomId.equals(roomId) && v.date.equals(date) && v.slot.equals(slot)
                    && (v.status == ViewingStatus.PENDING || v.status == ViewingStatus.CONFIRMED)) {
                return "Xung đột lịch: khung giờ này đã có người đặt. Vui lòng chọn khung giờ khác.";
            }
        }
        Room room = room(roomId);
        if (room == null) return "Không tìm thấy phòng.";
        state.viewings.add(new Viewing(nextId("v"), roomId, tenant, date, slot, phone, note, ViewingStatus.PENDING));
        commit();
        notify(room.ownerId, nameOf(tenant) + " đặt lịch xem \"" + room.title + "\" lúc " + slot + " ngày " + date + ".");
        return null;
    }

    public static void respondViewing(String id, ViewingStatus status, String response) {
        Viewing v = null;
        for (Viewing x : state.viewings) {
            if (x.id.equals(id)) {
                v = x;
                break;
            }
        }
        if (v == null) return;
        v.status = status;
        v.response = response;
        commit();
        Room room = room(v.roomId);
        String title = room != null ? room.title : "";
        String text;
        switch (status) {
            case CONFIRMED:
                text = "Lịch xem \"" + title + "\" ngày " + v.date + " (" + v.slot + ") đã được xác nhận.";
                break;
            case CANCELLED:
                text = ("Lịch xem \"" + title + "\" ngày " + v.date + " bị từ chối. " + response).trim();
                break;
            case COMPLETED:
                text = "Lịch xem \"" + title + "\" đã hoàn tất.";
                break;
            default:
                return;
        }
        notify(v.tenantId, text);
    }

    public static void respondViewing(String id, ViewingStatus status) {
        respondViewing(id, status, "");
    }

    public static List<Viewing> viewingsFor(User user) {
        List<Viewing> out = new ArrayList<>();
        for (Viewing v : state.viewings) {
            switch (user.role) {
                case TENANT:
                    if (v.tenantId.equals(user.id)) out.add(v);
                    break;
                case LANDLORD:
                    Room r = room(v.roomId);
                    if (r != null && r.ownerId.equals(user.id)) out.add(v);
                    break;
                default:
                    out.add(v);
            }
        }
        Collections.sort(out, (a, b) -> Long.compare(b.createdAt, a.createdAt));
        return out;
    }

    // ---------- P05: chat & hỗ trợ ----------
    public static void sendMessage(String toId, String text) {
        String from = currentUserId;
        if (from == null) return;
        if (isBlank(text)) return;
        String t = text.trim();
        state.messages.add(new Message(nextId("m"), from, toId, t));
        commit();
        notify(toId, nameOf(from) + ": " + (t.length() > 60 ? t.substring(0, 60) : t));
    }

    public static List<Message> thread(String a, String b) {
        List<Message> out = new ArrayList<>();
        for (Message m : state.messages) {
            if ((m.fromId.equals(a) && m.toId.equals(b)) || (m.fromId.equals(b) && m.toId.equals(a))) out.add(m);
        }
        Collections.sort(out, (x, y) -> Long.compare(x.ts, y.ts));
        return out;
    }

    public static List<String> partners(String me) {
        List<Message> mine = new ArrayList<>();
        for (Message m : state.messages) if (m.fromId.equals(me) || m.toId.equals(me)) mine.add(m);
        Collections.sort(mine, (x, y) -> Long.compare(y.ts, x.ts));
        Set<String> out = new LinkedHashSet<>();
        for (Message m : mine) out.add(m.fromId.equals(me) ? m.toId : m.fromId);
        return new ArrayList<>(out);
    }

    public static String createSupport(String content, List<String> attachments) {
        String uid = currentUserId;
        if (uid == null) return "Chưa đăng nhập.";
        if (isBlank(content)) return "Vui lòng nhập nội dung yêu cầu.";
        state.supports.add(new SupportTicket(nextId("s"), uid, content.trim(), attachments));
        commit();
        for (User u : new ArrayList<>(state.users)) {
            if (u.role == Role.ADMIN) notify(u.id, "Yêu cầu hỗ trợ mới từ " + nameOf(uid) + ".");
        }
        return null;
    }

    public static void replySupport(String id, String text) {
        SupportTicket s = support(id);
        if (s == null) return;
        if (isBlank(text)) return;
        s.reply = text.trim();
        s.replyTs = System.currentTimeMillis();
        commit();
        notify(s.userId, "Admin đã phản hồi yêu cầu hỗ trợ của bạn.");
    }

    // ---------- P06: hợp đồng ----------
    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** @return ngày hợp lệ hoặc null nếu sai định dạng dd/MM/yyyy. */
    public static LocalDate parseDate(String s) {
        try {
            return LocalDate.parse(s.trim(), DMY);
        } catch (Exception e) {
            return null;
        }
    }

    public static String fmtDate(LocalDate d) {
        return d.format(DMY);
    }

    public static String createContract(String roomId, String tenantId, String start, String end, long rent,
                                        long deposit, int cycle, int billingDay, String terms) {
        String landlord = currentUserId;
        if (landlord == null) return "Chưa đăng nhập.";
        Room room = room(roomId);
        if (room == null) return "Chưa chọn phòng.";
        if (user(tenantId) == null) return "Chưa chọn người thuê.";
        LocalDate s = parseDate(start);
        if (s == null) return "Ngày bắt đầu không hợp lệ (dd/mm/yyyy).";
        LocalDate e = parseDate(end);
        if (e == null) return "Ngày kết thúc không hợp lệ (dd/mm/yyyy).";
        if (!e.isAfter(s)) return "Ngày kết thúc phải sau ngày bắt đầu.";
        if (rent <= 0) return "Vui lòng nhập giá thuê.";
        if (activeContractOfRoom(roomId) != null) return "Phòng này đang có hợp đồng còn hiệu lực.";
        state.contracts.add(new Contract(nextId("c"), roomId, landlord, tenantId, start.trim(), end.trim(), rent,
                deposit, cycle, billingDay, terms.trim(), ContractStatus.DRAFT));
        commit();
        notify(tenantId, "Bạn có hợp đồng thuê \"" + room.title + "\" cần xác nhận.");
        return null;
    }

    public static void confirmContract(String id) {
        Contract c = contract(id);
        if (c == null) return;
        if (c.status != ContractStatus.DRAFT) return; // NFR09: đã xác nhận thì khóa chỉnh sửa
        c.status = ContractStatus.ACTIVE;
        commit();
        notify(c.landlordId, nameOf(c.tenantId) + " đã xác nhận hợp đồng. Hợp đồng có hiệu lực.");
    }

    public static List<Contract> contractsFor(User user) {
        List<Contract> out = new ArrayList<>();
        for (Contract c : state.contracts) {
            switch (user.role) {
                case TENANT:
                    if (c.tenantId.equals(user.id)) out.add(c);
                    break;
                case LANDLORD:
                    if (c.landlordId.equals(user.id)) out.add(c);
                    break;
                default:
                    out.add(c);
            }
        }
        Collections.sort(out, (a, b) -> Long.compare(b.createdAt, a.createdAt));
        return out;
    }

    // ---------- P07: ghi nhận điện nước (offline-first) ----------
    public static String currentPeriod() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("MM/yyyy"));
    }

    public static MeterReading lastReading(String roomId) {
        MeterReading best = null;
        for (MeterReading m : state.readings) {
            if (m.roomId.equals(roomId) && (best == null || m.ts > best.ts)) best = m;
        }
        return best;
    }

    public static List<MeterReading> readingsOf(String roomId) {
        List<MeterReading> out = new ArrayList<>();
        for (MeterReading m : state.readings) if (m.roomId.equals(roomId)) out.add(m);
        Collections.sort(out, (a, b) -> Long.compare(b.ts, a.ts));
        return out;
    }

    public static String submitReading(String roomId, Integer elecNew, Integer waterNew, String elecPhoto,
                                       String waterPhoto) {
        MeterReading last = lastReading(roomId);
        int eo = last != null ? last.elecNew : 0;
        int wo = last != null ? last.waterNew : 0;
        if (elecNew == null || waterNew == null) return "Vui lòng nhập chỉ số điện và nước mới.";
        if (elecNew < eo) return "Chỉ số điện mới (" + elecNew + ") nhỏ hơn chỉ số cũ (" + eo + ").";
        if (waterNew < wo) return "Chỉ số nước mới (" + waterNew + ") nhỏ hơn chỉ số cũ (" + wo + ").";
        if (isBlank(elecPhoto) || isBlank(waterPhoto)) return "Vui lòng chụp ảnh công tơ điện và nước làm minh chứng.";
        String period = currentPeriod();
        for (MeterReading m : state.readings) {
            if (m.roomId.equals(roomId) && m.period.equals(period)) return "Phòng đã được chốt chỉ số kỳ " + period + ".";
        }
        boolean online = isOnline();
        MeterReading r = new MeterReading(nextId("m"), roomId, period, eo, elecNew, wo, waterNew, elecPhoto,
                waterPhoto, System.currentTimeMillis(), online);
        state.readings.add(r);
        commit();
        if (online) finalizeReading(r);
        return null;
    }

    public static int pendingCount() {
        int c = 0;
        for (MeterReading m : state.readings) if (!m.synced) c++;
        return c;
    }

    /** Đồng bộ các bản ghi lưu ngoại tuyến lên "máy chủ" khi có mạng. */
    public static void syncPending() {
        if (!isOnline()) return;
        List<MeterReading> pending = new ArrayList<>();
        for (MeterReading m : state.readings) if (!m.synced) pending.add(m);
        if (pending.isEmpty()) return;
        for (MeterReading m : pending) m.synced = true;
        commit();
        for (MeterReading m : pending) finalizeReading(m);
        if (currentUserId != null) {
            notify(currentUserId, "Đã đồng bộ " + pending.size() + " bản ghi điện nước lưu ngoại tuyến.");
        }
    }

    private static void finalizeReading(MeterReading r) {
        Contract c = activeContractOfRoom(r.roomId);
        if (c == null) return;
        Room room = room(r.roomId);
        if (room == null) return;
        notify(c.tenantId, "Chỉ số điện nước kỳ " + r.period + " của \"" + room.title
                + "\" đã được cập nhật. Vui lòng đối chiếu.");
        createDraftInvoice(c, room, r);
    }

    // ---------- P08: hóa đơn & thanh toán ----------
    private static void createDraftInvoice(Contract c, Room room, MeterReading r) {
        for (Invoice i : state.invoices) {
            if (i.roomId.equals(room.id) && i.period.equals(r.period)) return;
        }
        int elecUsed = Billing.consumption(r.elecOld, r.elecNew);
        int waterUsed = Billing.consumption(r.waterOld, r.waterNew);
        long rent = Billing.rent(c.rent, c.cycleMonths);
        long elec = Billing.utility(r.elecOld, r.elecNew, room.elecPrice);
        long water = Billing.utility(r.waterOld, r.waterNew, room.waterPrice);
        long fees = Billing.fees(room.fees, c.cycleMonths);
        List<LogEntry> history = new ArrayList<>();
        history.add(new LogEntry(System.currentTimeMillis(), "Hệ thống tạo bản nháp hóa đơn"));
        state.invoices.add(new Invoice(nextId("i"), c.id, room.id, c.tenantId, c.landlordId, r.period, rent,
                elecUsed, elec, waterUsed, water, fees, 0, "", Billing.total(rent, elec, water, fees, 0),
                InvoiceStatus.DRAFT, history));
        commit();
        notify(c.landlordId, "Bản nháp hóa đơn kỳ " + r.period + " của \"" + room.title + "\" đã sẵn sàng để duyệt.");
    }

    public static String issueInvoice(String id, long adjust, String reason) {
        Invoice inv = invoice(id);
        if (inv == null) return "Không tìm thấy hóa đơn.";
        if (inv.status != InvoiceStatus.DRAFT) return "Hóa đơn đã được xuất.";
        if (adjust != 0L && isBlank(reason)) return "Vui lòng nhập lý do cho khoản phát sinh/giảm trừ.";
        long total = Billing.total(inv.rentAmt, inv.elecAmt, inv.waterAmt, inv.feeAmt, adjust);
        if (total < 0) return "Tổng tiền không được âm.";
        inv.adjust = adjust;
        inv.adjustReason = reason.trim();
        inv.total = total;
        inv.status = InvoiceStatus.ISSUED;
        inv.history.add(new LogEntry(System.currentTimeMillis(), "Landlord duyệt & xuất hóa đơn"));
        commit();
        notify(inv.tenantId, "Hóa đơn kỳ " + inv.period + " mới: " + Fmt.formatMoney(total) + ". Vui lòng thanh toán.");
        return null;
    }

    public static void markPaid(String id) {
        Invoice inv = invoice(id);
        if (inv == null) return;
        if (inv.status != InvoiceStatus.ISSUED) return;
        inv.status = InvoiceStatus.PAID;
        inv.history.add(new LogEntry(System.currentTimeMillis(), "Landlord xác nhận đã nhận tiền → Paid"));
        commit();
        notify(inv.tenantId, "Hóa đơn kỳ " + inv.period + " đã được xác nhận thanh toán. Cảm ơn bạn!");
    }

    private static long firstTs(List<LogEntry> history) {
        return history.isEmpty() ? 0L : history.get(0).ts;
    }

    public static List<Invoice> invoicesFor(User user) {
        List<Invoice> out = new ArrayList<>();
        for (Invoice i : state.invoices) {
            switch (user.role) {
                case TENANT:
                    if (i.tenantId.equals(user.id) && i.status != InvoiceStatus.DRAFT) out.add(i);
                    break;
                case LANDLORD:
                    if (i.landlordId.equals(user.id)) out.add(i);
                    break;
                default:
                    out.add(i);
            }
        }
        Collections.sort(out, (a, b) -> Long.compare(firstTs(b.history), firstTs(a.history)));
        return out;
    }

    // ---------- P09: sự cố ----------
    public static final List<String> CATEGORIES =
            Collections.unmodifiableList(Arrays.asList("Điện", "Nước", "Nội thất", "Kết cấu", "Khác"));
    public static final List<String> PRIORITIES =
            Collections.unmodifiableList(Arrays.asList("Bình thường", "Gấp", "Rất gấp"));

    public static TicketResult createTicket(String category, String priority, String desc, List<String> photos) {
        String tenant = currentUserId;
        if (tenant == null) return new TicketResult("Chưa đăng nhập.", null);
        Contract c = activeContractOfTenant(tenant);
        if (c == null) {
            return new TicketResult("Bạn chưa có hợp đồng thuê phòng còn hiệu lực để báo sự cố.", null);
        }
        if (isBlank(desc)) return new TicketResult("Vui lòng mô tả tình trạng sự cố.", null);
        List<LogEntry> history = new ArrayList<>();
        history.add(new LogEntry(System.currentTimeMillis(), "Tenant gửi phiếu – Chờ xử lý"));
        Ticket t = new Ticket(nextId("t"), c.roomId, tenant, c.landlordId, category, priority, desc.trim(), photos,
                TicketStatus.OPEN, history);
        state.tickets.add(t);
        commit();
        Room room = room(c.roomId);
        notify(c.landlordId, "Phiếu sự cố mới (" + category + " – " + priority + ") tại \""
                + (room != null ? room.title : "null") + "\".");
        return new TicketResult(null, t.id);
    }

    public static void setTicketStatus(String id, TicketStatus status) {
        Ticket t = ticket(id);
        if (t == null) return;
        User me = user(currentUserId);
        String actor = me != null ? me.name : "";
        t.status = status;
        t.history.add(new LogEntry(System.currentTimeMillis(), actor + " → " + status.label));
        commit();
        String other = t.tenantId.equals(currentUserId) ? t.landlordId : t.tenantId;
        notify(other, "Phiếu sự cố (" + t.category + ") chuyển sang: " + status.label + ".");
    }

    public static List<Ticket> ticketsFor(User user) {
        List<Ticket> out = new ArrayList<>();
        for (Ticket t : state.tickets) {
            switch (user.role) {
                case TENANT:
                    if (t.tenantId.equals(user.id)) out.add(t);
                    break;
                case LANDLORD:
                    if (t.landlordId.equals(user.id)) out.add(t);
                    break;
                default:
                    out.add(t);
            }
        }
        Collections.sort(out, (a, b) -> Long.compare(firstTs(b.history), firstTs(a.history)));
        return out;
    }

    // ---------- tiện ích quản trị ----------
    public static void generateMock() {
        AppState result = Seed.mock(state, Store::nextNum);
        result.seq = state.seq;
        state = result;
        commit();
    }

    /** Sao chép ảnh (từ Uri stream) vào bộ nhớ riêng của app, trả về đường dẫn tuyệt đối. */
    public static String fileCopy(InputStream in) throws IOException {
        File f = new File(appCtx.getFilesDir(), "img_" + System.currentTimeMillis() + ".jpg");
        try (InputStream input = in; OutputStream out = Files.newOutputStream(f.toPath())) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = input.read(buf)) > 0) out.write(buf, 0, n);
        }
        return f.getAbsolutePath();
    }
}
