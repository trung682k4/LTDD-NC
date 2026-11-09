package com.rentconnect.app.ui;

import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.Role;
import com.rentconnect.app.model.Room;
import com.rentconnect.app.model.Ticket;
import com.rentconnect.app.model.TicketStatus;
import com.rentconnect.app.model.User;

import java.util.List;

/** Danh sách phiếu sự cố (tab). */
public class TicketListFragment extends RecyclerFragment {
    @Override
    public String title() {
        return getString(R.string.s_su_co);
    }

    @Override
    public boolean isTab() {
        return true;
    }

    static Ui.Tone tone(TicketStatus s) {
        switch (s) {
            case OPEN: return Ui.Tone.AMBER;
            case IN_PROGRESS: return Ui.Tone.PRIMARY;
            default: return Ui.Tone.GREEN;
        }
    }

    @Override
    protected void build(List<Row> rows) {
        User me = me();
        if (me == null) return;
        List<Ticket> list = Store.ticketsFor(me);
        if (me.role == Role.TENANT) rows.add(Rows.primary(getString(R.string.s_bao_cao_su_co), b -> push(new TicketFormFragment())));
        if (list.isEmpty()) rows.add(Rows.empty(getString(R.string.s_chua_co_phieu_su_co)));
        for (Ticket t : list) {
            rows.add(new Row(R.layout.item_ticket, v -> {
                Rows.set(v, R.id.tv_head, t.category + " · " + t.priority);
                Ui.setTag(v.findViewById(R.id.tag_status), t.status.label, tone(t.status));
                Rows.set(v, R.id.tv_desc, t.desc.length() > 80 ? t.desc.substring(0, 80) : t.desc);
                Room r = Store.room(t.roomId);
                User tenant = Store.user(t.tenantId);
                Rows.set(v, R.id.tv_sub, (r == null ? t.roomId : r.title) + " · " + (tenant == null ? "" : tenant.name));
                v.setOnClickListener(b -> push(BaseFragment.withId(new TicketDetailFragment(), t.id)));
            }));
        }
    }
}
