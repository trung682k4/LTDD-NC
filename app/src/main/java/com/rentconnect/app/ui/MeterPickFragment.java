package com.rentconnect.app.ui;

import android.view.View;

import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.Contract;
import com.rentconnect.app.model.MeterReading;
import com.rentconnect.app.model.Room;
import com.rentconnect.app.model.User;

import java.util.List;

/** FR07 – Landlord: chọn phòng để chốt chỉ số điện nước. */
public class MeterPickFragment extends RecyclerFragment {
    @Override
    public String title() {
        return getString(R.string.s_chot_dien_nuoc_chon_phong);
    }

    @Override
    protected void build(List<Row> rows) {
        User me = me();
        if (me == null) return;
        if (!Store.isOnline()) {
            rows.add(Rows.info("Ngoại tuyến: dữ liệu lưu trên máy, tự đồng bộ khi có mạng.", Ui.Tone.AMBER));
        } else if (Store.pendingCount() > 0) {
            rows.add(Rows.info("Có " + Store.pendingCount() + " bản ghi chờ đồng bộ.", Ui.Tone.AMBER));
        }
        int shown = 0;
        for (Room r : Store.roomsOf(me.id)) {
            Contract k = Store.activeContractOfRoom(r.id);
            if (k == null) continue;
            shown++;
            rows.add(new Row(R.layout.item_meter_room, v -> {
                MeterReading last = Store.lastReading(r.id);
                User tenant = Store.user(k.tenantId);
                Rows.set(v, R.id.tv_title, r.title);
                Rows.set(v, R.id.tv_tenant, "Người thuê: " + (tenant == null ? "—" : tenant.name));
                Rows.set(v, R.id.tv_last, last == null ? "Chưa có chỉ số"
                        : "Chỉ số gần nhất kỳ " + last.period + ": điện " + last.elecNew + ", nước " + last.waterNew);
                if (last != null && last.period.equals(Store.currentPeriod())) {
                    Ui.setTag(v.findViewById(R.id.tag_status), "Đã chốt kỳ này", Ui.Tone.GREEN);
                } else {
                    v.findViewById(R.id.tag_status).setVisibility(View.GONE);
                }
                v.setOnClickListener(b -> push(BaseFragment.withId(new MeterFormFragment(), r.id)));
            }));
        }
        if (shown == 0) rows.add(Rows.empty(getString(R.string.s_chua_co_phong_nao_co_hop_dong_hieu_luc)));
    }
}
