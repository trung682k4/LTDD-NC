package com.rentconnect.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rentconnect.app.Fmt;
import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.Room;
import com.rentconnect.app.model.Role;
import com.rentconnect.app.model.User;
import com.rentconnect.app.model.Viewing;
import com.rentconnect.app.model.ViewingStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** FR06 – Landlord: lập hợp đồng thuê (BM03). */
public class ContractFormFragment extends BaseFragment {
    private static final List<String> CYCLES = Arrays.asList("1 tháng/lần", "3 tháng/lần", "6 tháng/lần");
    private final List<User> candidates = new ArrayList<>();
    private String tenantLabel = "";
    private String cycle = CYCLES.get(0);
    private String day = "5";

    public ContractFormFragment() {
        super(R.layout.fragment_contract_form);
    }

    @Override
    public String title() {
        return getString(R.string.s_lap_hop_dong_thue_bm03);
    }

    private static String label(User u) {
        return u.name + " · " + u.contact;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        User me = me();
        if (me == null) return;
        String roomId = arg();
        Room room = Store.room(roomId);

        // Ứng viên: người đã đặt lịch xem phòng này; nếu chưa có ai thì lấy mọi người thuê. Loại người đã có hợp đồng hiệu lực.
        Set<String> ids = new LinkedHashSet<>();
        for (Viewing vw : Store.state().viewings) {
            if (vw.roomId.equals(roomId) && vw.status != ViewingStatus.CANCELLED) ids.add(vw.tenantId);
        }
        if (ids.isEmpty()) {
            for (User u : Store.state().users) if (u.role == Role.TENANT) ids.add(u.id);
        }
        candidates.clear();
        for (String id : ids) {
            User u = Store.user(id);
            if (u != null && Store.activeContractOfTenant(u.id) == null) candidates.add(u);
        }
        List<String> labels = new ArrayList<>();
        for (User u : candidates) labels.add(label(u));

        ((EditText) v(R.id.party_a)).setText(me.name);
        ((EditText) v(R.id.room)).setText(roomId + " – " + (room == null ? "" : room.title));
        ((EditText) v(R.id.start)).setText(Store.fmtDate(LocalDate.now()));
        ((EditText) v(R.id.end)).setText(Store.fmtDate(LocalDate.now().plusYears(1)));
        if (room != null) {
            ((EditText) v(R.id.rent)).setText(String.valueOf(room.rent));
            ((EditText) v(R.id.deposit)).setText(String.valueOf(room.deposit));
        }
        List<String> days = new ArrayList<>();
        for (int i = 1; i <= 28; i++) days.add(String.valueOf(i));

        Ui.dropdown((AutoCompleteTextView) v(R.id.party_b), labels, tenantLabel, s -> tenantLabel = s);
        Ui.dropdown((AutoCompleteTextView) v(R.id.cycle), CYCLES, cycle, s -> cycle = s);
        Ui.dropdown((AutoCompleteTextView) v(R.id.day), days, day, s -> day = s);

        TextView error = v(R.id.error);
        v(R.id.btn_submit).setOnClickListener(b -> {
            User tenant = null;
            for (User u : candidates) if (label(u).equals(tenantLabel)) tenant = u;
            if (tenant == null) {
                Ui.show(error, getString(R.string.s_vui_long_chon_nguoi_thue));
                return;
            }
            int months = Ui.toInt(cycle.split(" ")[0]);
            int billingDay = Ui.toInt(day);
            String e = Store.createContract(roomId, tenant.id, Ui.text(v(R.id.start)).trim(), Ui.text(v(R.id.end)).trim(),
                    Fmt.toLong(Ui.text(v(R.id.rent))), Fmt.toLong(Ui.text(v(R.id.deposit))),
                    months == 0 ? 1 : months, billingDay == 0 ? 5 : billingDay, Ui.text(v(R.id.terms)));
            if (e == null) {
                toast(getString(R.string.s_da_gui_ban_nhap_hop_dong_cho_nguoi_thu));
                pop();
            } else {
                Ui.show(error, e);
            }
        });
    }
}
