package com.rentconnect.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rentconnect.app.R;
import com.rentconnect.app.Store;

/** FR10 – Tenant: báo cáo sự cố (BM06). */
public class TicketFormFragment extends BaseFragment {
    private String cat = Store.CATEGORIES.get(0);
    private String pri = Store.PRIORITIES.get(1);
    private final PhotoHelper photoHelper = new PhotoHelper(this);

    public TicketFormFragment() {
        super(R.layout.fragment_ticket_form);
    }

    @Override
    public String title() {
        return getString(R.string.s_bao_cao_su_co_bm06);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Ui.pills(v(R.id.cat_group), Store.CATEGORIES, s -> s.equals(cat), s -> cat = s);
        Ui.pills(v(R.id.pri_group), Store.PRIORITIES, s -> s.equals(pri), s -> pri = s);
        PhotoStrip photos = v(R.id.photos);
        photos.setHelper(photoHelper);
        TextView error = v(R.id.error);
        v(R.id.btn_submit).setOnClickListener(b -> {
            Store.TicketResult r = Store.createTicket(cat, pri, Ui.text(v(R.id.desc)), photos.getPaths());
            if (r.error == null && r.ticketId != null) {
                pop();
                push(BaseFragment.withId(new TicketSentFragment(), r.ticketId));
            } else {
                Ui.show(error, r.error != null ? r.error : "Lỗi không xác định.");
            }
        });
    }
}
