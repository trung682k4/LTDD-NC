package com.rentconnect.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rentconnect.app.Fmt;
import com.rentconnect.app.R;
import com.rentconnect.app.Seed;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.Fee;
import com.rentconnect.app.model.Room;
import com.rentconnect.app.model.RoomStatus;
import com.rentconnect.app.model.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** FR02 – Landlord: đăng tin phòng trọ (BM01). */
public class RoomFormFragment extends BaseFragment {
    private String province = "", district = "", ward = "";
    private final List<Fee> fees = new ArrayList<>();
    private final Set<String> amen = new HashSet<>();
    private final PhotoHelper photoHelper = new PhotoHelper(this);

    public RoomFormFragment() {
        super(R.layout.fragment_room_form);
    }

    @Override
    public String title() {
        return getString(R.string.s_dang_tin_phong_tro_bm01);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindProvince();
        bindDistrictWard();
        bindFees();
        Ui.pills(v(R.id.amen_group), Seed.AMENITY_LIST, amen::contains, s -> {
            if (!amen.remove(s)) amen.add(s);
        });
        PhotoStrip photos = v(R.id.photos);
        photos.setHelper(photoHelper);
        v(R.id.btn_add_fee).setOnClickListener(b -> showFeeDialog());
        v(R.id.btn_submit).setOnClickListener(b -> submit(photos.getPaths()));
    }

    private void bindProvince() {
        Ui.dropdown((AutoCompleteTextView) v(R.id.province), new ArrayList<>(Seed.PROVINCES.keySet()), province, s -> {
            province = s;
            district = "";
            ward = "";
            bindDistrictWard();
        });
    }

    private void bindDistrictWard() {
        Map<String, List<String>> districts = Seed.PROVINCES.get(province);
        List<String> dl = districts == null ? new ArrayList<String>() : new ArrayList<>(districts.keySet());
        Ui.dropdown((AutoCompleteTextView) v(R.id.district), dl, district, s -> {
            district = s;
            ward = "";
            bindDistrictWard();
        });
        List<String> wl = districts == null || districts.get(district) == null
                ? new ArrayList<String>() : districts.get(district);
        Ui.dropdown((AutoCompleteTextView) v(R.id.ward), wl, ward, s -> ward = s);
    }

    private void bindFees() {
        LinearLayout list = v(R.id.fee_list);
        list.removeAllViews();
        for (int i = 0; i < fees.size(); i++) {
            final int idx = i;
            Fee f = fees.get(i);
            LinearLayout row = Ui.row(list);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            Ui.text(row, f.name + ": " + Fmt.formatMoney(f.amount));
            Ui.weight(row.getChildAt(0));
            com.google.android.material.button.MaterialButton del = (com.google.android.material.button.MaterialButton)
                    LayoutInflater.from(ctx()).inflate(R.layout.btn_outline, row, false);
            del.setText(R.string.delete);
            del.setMinHeight(0);
            del.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            del.setOnClickListener(b -> {
                fees.remove(idx);
                bindFees();
            });
            row.addView(del);
        }
    }

    private void showFeeDialog() {
        View form = LayoutInflater.from(ctx()).inflate(R.layout.dialog_fee, null);
        EditText name = form.findViewById(R.id.fee_name);
        EditText amount = form.findViewById(R.id.fee_amount);
        new MaterialAlertDialogBuilder(ctx())
                .setTitle(getString(R.string.s_them_khoan_phi_2))
                .setView(form)
                .setPositiveButton(R.string.add, (d, w) -> {
                    String n = Ui.text(name).trim();
                    long a = Fmt.toLong(Ui.text(amount));
                    if (!n.isEmpty() && a > 0) {
                        fees.add(new Fee(n, a));
                        bindFees();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void submit(List<String> photos) {
        User me = me();
        if (me == null) return;
        List<String> amenities = new ArrayList<>();
        for (String a : Seed.AMENITY_LIST) if (amen.contains(a)) amenities.add(a);
        Room room = new Room("", me.id, Ui.text(v(R.id.title)).trim(), province, district, ward,
                Ui.text(v(R.id.address)).trim(), Ui.toInt(Ui.text(v(R.id.area))), Ui.toInt(Ui.text(v(R.id.capacity))),
                Fmt.toLong(Ui.text(v(R.id.rent))), Fmt.toLong(Ui.text(v(R.id.deposit))),
                Fmt.toLong(Ui.text(v(R.id.elec))), Fmt.toLong(Ui.text(v(R.id.water))),
                new ArrayList<>(fees), amenities, photos, RoomStatus.PENDING);
        String e = Store.submitRoom(room);
        if (e == null) {
            toast(getString(R.string.s_da_gui_duyet_tin_o_trang_thai_cho_duye));
            pop();
        } else {
            Ui.show((TextView) v(R.id.error), e);
        }
    }
}
