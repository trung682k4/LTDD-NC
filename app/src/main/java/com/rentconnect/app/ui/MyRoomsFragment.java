package com.rentconnect.app.ui;

import android.view.View;

import com.rentconnect.app.Fmt;
import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.Invoice;
import com.rentconnect.app.model.InvoiceStatus;
import com.rentconnect.app.model.Room;
import com.rentconnect.app.model.User;

import java.util.List;

/** Landlord: tổng quan + danh sách phòng của tôi (RecyclerView). */
public class MyRoomsFragment extends RecyclerFragment {
    @Override
    public String title() {
        return getString(R.string.s_phong);
    }

    @Override
    public boolean isTab() {
        return true;
    }

    @Override
    protected void build(List<Row> rows) {
        User me = me();
        if (me == null) return;
        List<Room> rooms = Store.roomsOf(me.id);
        long unpaid = 0, revenue = 0;
        for (Invoice i : Store.state().invoices) {
            if (!me.id.equals(i.landlordId)) continue;
            if (i.status == InvoiceStatus.ISSUED) unpaid += i.total;
            else if (i.status == InvoiceStatus.PAID) revenue += i.total;
        }
        int rented = 0;
        for (Room r : rooms) if (Store.activeContractOfRoom(r.id) != null) rented++;
        final long fUnpaid = unpaid, fRevenue = revenue;
        final int fRented = rented, total = rooms.size();

        rows.add(new Row(R.layout.item_overview, v -> {
            Rows.kv(v, R.id.kv_rented, fRented + " / " + total);
            Rows.kv(v, R.id.kv_revenue, Fmt.formatMoney(fRevenue));
            Rows.kv(v, R.id.kv_debt, Fmt.formatMoney(fUnpaid));
        }));
        rows.add(Rows.primary(getString(R.string.s_dang_tin_phong_moi), b -> push(new RoomFormFragment())));
        if (rooms.isEmpty()) rows.add(Rows.empty(getString(R.string.s_ban_chua_co_tin_dang_nao)));
        for (Room r : rooms) {
            rows.add(new Row(R.layout.item_my_room, v -> {
                Rows.set(v, R.id.tv_title, r.title);
                Rows.set(v, R.id.tv_value, Fmt.formatMoney(r.rent));
                Rows.set(v, R.id.tv_area, r.getAreaText() + " · " + r.area + " m²");
                Ui.Tone tone;
                switch (r.status) {
                    case ACTIVE: tone = Ui.Tone.GREEN; break;
                    case PENDING: tone = Ui.Tone.PRIMARY; break;
                    default: tone = Ui.Tone.RED;
                }
                Ui.setTag(v.findViewById(R.id.tag_status), r.status.label, tone);
                boolean rentedNow = Store.activeContractOfRoom(r.id) != null;
                if (rentedNow) Ui.setTag(v.findViewById(R.id.tag_rented), "Đang cho thuê", Ui.Tone.AMBER);
                else v.findViewById(R.id.tag_rented).setVisibility(View.GONE);
                boolean note = r.adminNote != null && !r.adminNote.trim().isEmpty();
                Rows.show(v, R.id.tv_note, note);
                if (note) Rows.set(v, R.id.tv_note, "Admin: " + r.adminNote);
                v.setOnClickListener(b -> push(BaseFragment.withId(new RoomDetailFragment(), r.id)));
            }));
        }
    }
}
