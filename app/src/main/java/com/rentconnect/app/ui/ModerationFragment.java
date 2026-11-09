package com.rentconnect.app.ui;

import com.rentconnect.app.Fmt;
import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.Room;
import com.rentconnect.app.model.RoomStatus;
import com.rentconnect.app.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** FR02 – Admin: danh sách tin chờ kiểm duyệt theo trạng thái. */
public class ModerationFragment extends RecyclerFragment {
    private static final List<RoomStatus> STATUSES =
            Arrays.asList(RoomStatus.PENDING, RoomStatus.ACTIVE, RoomStatus.NEED_INFO, RoomStatus.REJECTED);
    private RoomStatus filter = RoomStatus.PENDING;

    @Override
    public String title() {
        return getString(R.string.s_kiem_duyet);
    }

    @Override
    public boolean isTab() {
        return true;
    }

    @Override
    protected void build(List<Row> rows) {
        List<String> labels = new ArrayList<>();
        for (RoomStatus s : STATUSES) labels.add(s.label);
        rows.add(Rows.filters(labels, l -> filter.label.equals(l), l -> {
            for (RoomStatus s : STATUSES) if (s.label.equals(l)) filter = s;
            refresh();
        }));

        List<Room> list = new ArrayList<>();
        for (Room r : Store.state().rooms) if (r.status == filter) list.add(r);
        Collections.sort(list, (a, b) -> Long.compare(b.createdAt, a.createdAt));
        if (list.isEmpty()) rows.add(Rows.empty(getString(R.string.s_khong_co_tin_nao_o_trang_thai_nay)));
        for (Room r : list) {
            rows.add(new Row(R.layout.item_moderation, v -> {
                Rows.set(v, R.id.tv_title, r.title);
                Rows.set(v, R.id.tv_info, r.getAreaText() + " · " + r.area + " m² · " + Fmt.formatMoney(r.rent));
                User owner = Store.user(r.ownerId);
                Rows.set(v, R.id.tv_owner, "Chủ trọ: " + (owner == null ? "—" : owner.name));
                v.setOnClickListener(b -> push(BaseFragment.withId(new ModerateFragment(), r.id)));
            }));
        }
    }
}
