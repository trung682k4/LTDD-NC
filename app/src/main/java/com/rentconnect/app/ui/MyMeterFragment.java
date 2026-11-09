package com.rentconnect.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rentconnect.app.Billing;
import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.Contract;
import com.rentconnect.app.model.MeterReading;
import com.rentconnect.app.model.User;

import java.util.ArrayList;
import java.util.List;

/** FR07 – Tenant: xem & đối chiếu chỉ số điện nước (fragment_my_meter.xml). */
public class MyMeterFragment extends BaseFragment {
    public MyMeterFragment() {
        super(R.layout.fragment_my_meter);
    }

    @Override
    public String title() {
        return getString(R.string.s_dien_nuoc_cua_toi);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bind();
    }

    @Override
    public void onStoreChanged() {
        if (getView() != null) bind();
    }

    private void showEmpty(String msg) {
        visible(R.id.content_box, false);
        visible(R.id.tv_empty, true);
        text(R.id.tv_empty, msg);
    }

    private void bind() {
        User me = me();
        if (me == null) return;
        Contract k = Store.activeContractOfTenant(me.id);
        if (k == null) {
            showEmpty("Bạn chưa có hợp đồng còn hiệu lực.");
            return;
        }
        List<MeterReading> list = new ArrayList<>();
        for (MeterReading m : Store.readingsOf(k.roomId)) if (m.synced) list.add(m);
        if (list.isEmpty()) {
            showEmpty("Chưa có chỉ số nào được ghi nhận.");
            return;
        }
        visible(R.id.tv_empty, false);
        visible(R.id.content_box, true);

        MeterReading cur = list.get(0);
        text(R.id.tv_period, "Kỳ " + cur.period);
        kv(R.id.kv_elec_range, cur.elecOld + " → " + cur.elecNew);
        kv(R.id.kv_elec_use, Billing.consumption(cur.elecOld, cur.elecNew) + " kWh");
        kv(R.id.kv_water_range, cur.waterOld + " → " + cur.waterNew);
        kv(R.id.kv_water_use, Billing.consumption(cur.waterOld, cur.waterNew) + " khối");
        Ui.bindThumb(v(R.id.elec_photo), cur.elecPhoto, "Ảnh điện");
        Ui.bindThumb(v(R.id.water_photo), cur.waterPhoto, "Ảnh nước");

        LinearLayout history = v(R.id.history);
        history.removeAllViews();
        for (MeterReading m : list.subList(1, list.size())) {
            Ui.keyValue(history, "Kỳ " + m.period, "Điện " + Billing.consumption(m.elecOld, m.elecNew)
                    + " · Nước " + Billing.consumption(m.waterOld, m.waterNew));
        }
        visible(R.id.tv_no_prev, list.size() <= 1);
    }
}
