package com.rentconnect.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.Room;
import com.rentconnect.app.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** FR04 – Tenant: đặt lịch xem phòng (BM02). */
public class BookingFragment extends BaseFragment {
    private String date = "";
    private String slot = "";

    public BookingFragment() {
        super(R.layout.fragment_booking);
    }

    @Override
    public String title() {
        return getString(R.string.s_dat_lich_xem_phong_bm02);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        User me = me();
        if (me == null) return;
        String roomId = arg();
        Room room = Store.room(roomId);
        ((EditText) v(R.id.room)).setText(roomId + " – " + (room == null ? "" : room.title));
        ((EditText) v(R.id.phone)).setText(me.getPhone());

        List<String> days = new ArrayList<>();
        for (int i = 1; i <= 14; i++) days.add(Store.fmtDate(LocalDate.now().plusDays(i)));
        Ui.dropdown((AutoCompleteTextView) v(R.id.date), days, date, s -> date = s);
        Ui.dropdown((AutoCompleteTextView) v(R.id.slot), Store.SLOTS, slot, s -> slot = s);

        TextView error = v(R.id.error);
        v(R.id.btn_submit).setOnClickListener(b -> {
            String e = Store.bookViewing(roomId, date, slot, Ui.text(v(R.id.phone)), Ui.text(v(R.id.note)));
            if (e == null) {
                toast(getString(R.string.s_da_gui_yeu_cau_trang_thai_cho_xac_nhan));
                pop();
                push(new ViewingsFragment());
            } else {
                Ui.show(error, e);
            }
        });
    }
}
