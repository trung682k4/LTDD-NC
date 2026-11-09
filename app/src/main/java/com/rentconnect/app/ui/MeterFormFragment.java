package com.rentconnect.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.MeterReading;
import com.rentconnect.app.model.Room;

/** FR07 – Landlord: chốt chỉ số điện nước (BM04), có chụp ảnh công tơ, lưu ngoại tuyến được. */
public class MeterFormFragment extends BaseFragment {
    private String elecPhoto = "";
    private String waterPhoto = "";
    private final PhotoHelper photoHelper = new PhotoHelper(this);

    public MeterFormFragment() {
        super(R.layout.fragment_meter_form);
    }

    @Override
    public String title() {
        return getString(R.string.s_chot_chi_so_dien_nuoc_bm04);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String roomId = arg();
        Room room = Store.room(roomId);
        MeterReading last = Store.lastReading(roomId);

        ((EditText) v(R.id.period)).setText(Store.currentPeriod());
        ((EditText) v(R.id.room_id)).setText(roomId);
        ((TextView) v(R.id.room_title)).setText(room == null ? "" : room.title);
        Ui.layoutOf(v(R.id.elec))
                .setHint("Chỉ số điện mới (cũ: " + (last == null ? 0 : last.elecNew) + ")");
        Ui.layoutOf(v(R.id.water))
                .setHint("Chỉ số nước mới (cũ: " + (last == null ? 0 : last.waterNew) + ")");

        View elecThumb = v(R.id.elec_photo);
        View waterThumb = v(R.id.water_photo);
        Ui.bindThumb(elecThumb, elecPhoto, "📷 Chụp ảnh");
        Ui.bindThumb(waterThumb, waterPhoto, "📷 Chụp ảnh");
        elecThumb.setOnClickListener(b -> photoHelper.camera(p -> {
            elecPhoto = p;
            Ui.bindThumb(elecThumb, p, "📷 Chụp ảnh");
        }));
        waterThumb.setOnClickListener(b -> photoHelper.camera(p -> {
            waterPhoto = p;
            Ui.bindThumb(waterThumb, p, "📷 Chụp ảnh");
        }));

        updateBanner();
        TextView error = v(R.id.error);
        v(R.id.btn_submit).setOnClickListener(b -> {
            String es = Ui.text(v(R.id.elec)).trim();
            String ws = Ui.text(v(R.id.water)).trim();
            Integer elec = es.isEmpty() ? null : Integer.valueOf(Ui.toInt(es));
            Integer water = ws.isEmpty() ? null : Integer.valueOf(Ui.toInt(ws));
            String e = Store.submitReading(roomId, elec, water, elecPhoto, waterPhoto);
            if (e == null) {
                toast(Store.isOnline() ? "Đã lưu và đồng bộ." : "Đã lưu ngoại tuyến, sẽ đồng bộ khi có mạng.");
                pop();
            } else {
                Ui.show(error, e);
            }
        });
    }

    private void updateBanner() {
        TextView banner = v(R.id.banner);
        if (!Store.isOnline()) {
            banner.setText("Ngoại tuyến: dữ liệu lưu trên máy, tự đồng bộ khi có mạng.");
            banner.setVisibility(View.VISIBLE);
        } else if (Store.pendingCount() > 0) {
            banner.setText("Có " + Store.pendingCount() + " bản ghi chờ đồng bộ.");
            banner.setVisibility(View.VISIBLE);
        } else {
            banner.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStoreChanged() {
        if (getView() != null) updateBanner();
    }
}
