package com.rentconnect.app.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.Ticket;

public class TicketSentFragment extends BaseFragment {
    public TicketSentFragment() {
        super(R.layout.fragment_ticket_sent);
    }

    @Override
    public String title() {
        return getString(R.string.s_phieu_da_gui);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Ticket t = Store.ticket(arg());
        if (t != null) Ui.setTag(v(R.id.tag_status), t.status.label, TicketListFragment.tone(t.status));
        click(R.id.btn_follow, b -> {
            pop();
            push(BaseFragment.withId(new TicketDetailFragment(), arg()));
        });
    }
}
