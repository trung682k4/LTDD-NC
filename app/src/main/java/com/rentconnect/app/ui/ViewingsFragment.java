package com.rentconnect.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.Role;
import com.rentconnect.app.model.Room;
import com.rentconnect.app.model.User;
import com.rentconnect.app.model.Viewing;
import com.rentconnect.app.model.ViewingStatus;

import java.util.ArrayList;
import java.util.List;

/** FR04 – danh sách lịch hẹn (Tenant xem/hủy, Landlord xử lý). */
public class ViewingsFragment extends RecyclerFragment {
    private ViewingStatus filter = ViewingStatus.PENDING;

    @Override
    public String title() {
        User me = me();
        return me != null && me.role == Role.LANDLORD ? "Lịch hẹn xem phòng" : "Lịch xem phòng của tôi";
    }

    private static String statusText(ViewingStatus s) {
        switch (s) {
            case PENDING: return "Chờ xác nhận";
            case CONFIRMED: return "Đã xác nhận";
            case CANCELLED: return "Đã hủy/từ chối";
            default: return "Hoàn tất";
        }
    }

    private static void line(View root, int id, String prefix, String value) {
        boolean has = value != null && !value.trim().isEmpty();
        Rows.show(root, id, has);
        if (has) Rows.set(root, id, prefix + value);
    }

    @Override
    protected void build(List<Row> rows) {
        User me = me();
        if (me == null) return;
        List<String> labels = new ArrayList<>();
        for (ViewingStatus s : ViewingStatus.values()) labels.add(s.label);
        rows.add(Rows.filters(labels, l -> filter.label.equals(l), l -> {
            for (ViewingStatus s : ViewingStatus.values()) if (s.label.equals(l)) filter = s;
            refresh();
        }));

        int shown = 0;
        for (Viewing vw : Store.viewingsFor(me)) {
            if (vw.status != filter) continue;
            shown++;
            rows.add(new Row(R.layout.item_viewing, v -> {
                Room room = Store.room(vw.roomId);
                User tenant = Store.user(vw.tenantId);
                Rows.set(v, R.id.tv_title, (room == null ? vw.roomId : room.title) + " (" + vw.roomId + ")");
                Rows.set(v, R.id.tv_when, (tenant == null ? "" : tenant.name) + " · " + vw.date + " · " + vw.slot);
                line(v, R.id.tv_phone, "SĐT: ", vw.phone);
                line(v, R.id.tv_note, "Ghi chú: ", vw.note);
                line(v, R.id.tv_response, "Phản hồi: ", vw.response);
                Ui.setTag(v.findViewById(R.id.tag_status), statusText(vw.status), Ui.Tone.PRIMARY);

                boolean landlordPending = me.role == Role.LANDLORD && vw.status == ViewingStatus.PENDING;
                Rows.show(v, R.id.row_actions, landlordPending);
                Rows.show(v, R.id.btn_counter, landlordPending);
                Rows.show(v, R.id.btn_complete, me.role == Role.LANDLORD && vw.status == ViewingStatus.CONFIRMED);
                Rows.show(v, R.id.btn_cancel, me.role == Role.TENANT && vw.status == ViewingStatus.PENDING);
                v.findViewById(R.id.btn_confirm).setOnClickListener(b -> Store.respondViewing(vw.id, ViewingStatus.CONFIRMED));
                v.findViewById(R.id.btn_reject).setOnClickListener(b -> Store.respondViewing(vw.id, ViewingStatus.CANCELLED));
                v.findViewById(R.id.btn_counter).setOnClickListener(b -> askCounter(vw.id));
                v.findViewById(R.id.btn_complete).setOnClickListener(b -> Store.respondViewing(vw.id, ViewingStatus.COMPLETED));
                v.findViewById(R.id.btn_cancel).setOnClickListener(
                        b -> Store.respondViewing(vw.id, ViewingStatus.CANCELLED, "Người thuê hủy lịch"));
            }));
        }
        if (shown == 0) rows.add(Rows.empty(getString(R.string.s_chua_co_lich_hen)));
        if (me.role == Role.LANDLORD) {
            rows.add(Rows.info("Kết quả được cập nhật vào trạng thái lịch hẹn và gửi thông báo cho người thuê.", Ui.Tone.PRIMARY));
        }
    }

    private void askCounter(String id) {
        View form = LayoutInflater.from(ctx()).inflate(R.layout.dialog_note, null);
        EditText et = form.findViewById(R.id.et);
        et.setHint(getString(R.string.s_vd_em_co_the_xem_vao_15_00_ngay_30_10));
        new MaterialAlertDialogBuilder(ctx())
                .setTitle(getString(R.string.s_de_xuat_lich_khac))
                .setView(form)
                .setPositiveButton(R.string.send, (d, w) -> {
                    String t = Ui.text(et).trim();
                    Store.respondViewing(id, ViewingStatus.CANCELLED, t.isEmpty() ? "Chủ trọ từ chối lịch này" : t);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
