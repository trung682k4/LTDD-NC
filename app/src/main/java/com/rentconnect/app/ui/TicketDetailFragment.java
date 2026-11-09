package com.rentconnect.app.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rentconnect.app.Fmt;
import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.LogEntry;
import com.rentconnect.app.model.Role;
import com.rentconnect.app.model.Room;
import com.rentconnect.app.model.Ticket;
import com.rentconnect.app.model.TicketStatus;
import com.rentconnect.app.model.User;

/** FR11 – xử lý / theo dõi phiếu sự cố (fragment_ticket_detail.xml), có thanh tiến trình trạng thái. */
public class TicketDetailFragment extends BaseFragment {
    private static final TicketStatus[] STEPS = {TicketStatus.OPEN, TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED};
    private static final int[] STEP_IDS = {R.id.step1, R.id.step2, R.id.step3};

    public TicketDetailFragment() {
        super(R.layout.fragment_ticket_detail);
    }

    @Override
    public String title() {
        User me = me();
        return me != null && me.role == Role.LANDLORD ? "Chi tiết phiếu sự cố" : "Theo dõi phiếu sự cố";
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bind();
    }

    @Override
    public void onStoreChanged() {
        if (getView() != null) bind();
    }

    private void bindStepper(TicketStatus status) {
        for (int i = 0; i < STEPS.length; i++) {
            View step = v(STEP_IDS[i]);
            boolean current = STEPS[i] == status;
            step.findViewById(R.id.dot).setSelected(STEPS[i].ordinal() <= status.ordinal());
            step.findViewById(R.id.line_left).setVisibility(i == 0 ? View.INVISIBLE : View.VISIBLE);
            step.findViewById(R.id.line_right).setVisibility(i == STEPS.length - 1 ? View.INVISIBLE : View.VISIBLE);
            TextView label = step.findViewById(R.id.step_label);
            label.setText(STEPS[i].label);
            label.setTypeface(null, current ? Typeface.BOLD : Typeface.NORMAL);
            label.setTextColor(Ui.color(ctx(), current ? R.color.black : R.color.muted));
        }
    }

    private void bind() {
        User me = me();
        Ticket t = Store.ticket(arg());
        if (me == null || t == null) return;
        Room room = Store.room(t.roomId);
        User tenant = Store.user(t.tenantId);

        bindStepper(t.status);
        kv(R.id.kv_cat, t.category + " / " + t.priority);
        kv(R.id.kv_room, room == null ? t.roomId : room.title);
        kv(R.id.kv_reporter, tenant == null ? "" : tenant.name);
        text(R.id.tv_desc, "Mô tả tình trạng: " + t.desc);
        Ui.gallery(v(R.id.thumb1), v(R.id.thumb2), t.photos, null);

        boolean party = me.id.equals(t.landlordId) || me.id.equals(t.tenantId);
        boolean open = t.status != TicketStatus.RESOLVED;
        visible(R.id.btn_accept, me.role == Role.LANDLORD && t.status == TicketStatus.OPEN);
        click(R.id.btn_accept, b -> Store.setTicketStatus(t.id, TicketStatus.IN_PROGRESS));
        visible(R.id.btn_close_landlord, party && open && me.role == Role.LANDLORD);
        visible(R.id.btn_close_tenant, party && open && me.role != Role.LANDLORD);
        View.OnClickListener close = b -> Store.setTicketStatus(t.id, TicketStatus.RESOLVED);
        click(R.id.btn_close_landlord, close);
        click(R.id.btn_close_tenant, close);

        LinearLayout history = v(R.id.history);
        history.removeAllViews();
        if (t.history != null) for (LogEntry l : t.history) Ui.keyValue(history, Fmt.formatTime(l.ts), l.text);
    }
}
