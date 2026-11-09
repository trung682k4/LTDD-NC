package com.rentconnect.app.ui;

import android.graphics.Typeface;
import android.widget.TextView;

import com.rentconnect.app.Fmt;
import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.Notif;

import java.util.List;

public class NotificationsFragment extends RecyclerFragment {
    @Override
    public String title() {
        return getString(R.string.notifications);
    }

    @Override
    protected void build(List<Row> rows) {
        List<Notif> list = Store.myNotifs();
        rows.add(Rows.outline(getString(R.string.s_da_doc_het), b -> Store.markAllRead()));
        if (list.isEmpty()) rows.add(Rows.empty(getString(R.string.s_chua_co_thong_bao)));
        for (Notif n : list) {
            rows.add(new Row(R.layout.item_notif, v -> {
                TextView t = v.findViewById(R.id.tv_text);
                t.setText(n.text);
                t.setTypeface(null, n.read ? Typeface.NORMAL : Typeface.BOLD);
                Rows.set(v, R.id.tv_time, Fmt.formatTime(n.ts));
            }));
        }
    }
}
