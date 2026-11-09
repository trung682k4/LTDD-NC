package com.rentconnect.app.ui;

import android.view.View;

import com.rentconnect.app.Fmt;
import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.Invoice;
import com.rentconnect.app.model.InvoiceStatus;
import com.rentconnect.app.model.Role;
import com.rentconnect.app.model.User;

import java.util.ArrayList;
import java.util.List;

/** Danh sách hóa đơn (Tenant / Landlord). */
public class InvoiceListFragment extends RecyclerFragment {
    private static final String ALL = "Tất cả";
    private InvoiceStatus filter = null;

    @Override
    public String title() {
        return getString(R.string.s_hoa_don);
    }

    @Override
    public boolean isTab() {
        return true;
    }

    static Ui.Tone tone(InvoiceStatus s) {
        switch (s) {
            case DRAFT: return Ui.Tone.PRIMARY;
            case ISSUED: return Ui.Tone.AMBER;
            default: return Ui.Tone.GREEN;
        }
    }

    private static String label(InvoiceStatus s) {
        return s == InvoiceStatus.PAID ? "Đã thu" : s.label;
    }

    @Override
    protected void build(List<Row> rows) {
        User me = me();
        if (me == null) return;
        List<String> options = new ArrayList<>();
        options.add(ALL);
        for (InvoiceStatus s : InvoiceStatus.values()) {
            if (me.role != Role.LANDLORD && s == InvoiceStatus.DRAFT) continue;
            options.add(label(s));
        }
        rows.add(Rows.filters(options, o -> filter == null ? ALL.equals(o) : label(filter).equals(o), o -> {
            filter = null;
            for (InvoiceStatus s : InvoiceStatus.values()) if (label(s).equals(o)) filter = s;
            refresh();
        }));

        int shown = 0;
        for (Invoice inv : Store.invoicesFor(me)) {
            if (filter != null && inv.status != filter) continue;
            shown++;
            rows.add(new Row(R.layout.item_invoice, v -> {
                Rows.set(v, R.id.tv_title, "Hóa đơn kỳ " + inv.period);
                Rows.set(v, R.id.tv_value, Fmt.formatMoney(inv.total));
                User tenant = Store.user(inv.tenantId);
                Rows.set(v, R.id.tv_sub, (tenant == null ? "" : tenant.name) + " · Phòng " + inv.roomId);
                Ui.setTag(v.findViewById(R.id.tag_status), inv.status.label, tone(inv.status));
                boolean canConfirm = me.role == Role.LANDLORD && inv.status == InvoiceStatus.ISSUED;
                v.findViewById(R.id.btn_paid).setVisibility(canConfirm ? View.VISIBLE : View.GONE);
                v.findViewById(R.id.btn_paid).setOnClickListener(b -> Store.markPaid(inv.id));
                v.setOnClickListener(b -> push(BaseFragment.withId(new InvoiceDetailFragment(), inv.id)));
            }));
        }
        if (shown == 0) rows.add(Rows.empty(getString(R.string.s_chua_co_hoa_don)));
    }
}
