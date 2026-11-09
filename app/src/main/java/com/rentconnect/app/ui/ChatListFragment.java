package com.rentconnect.app.ui;

import android.view.View;

import com.rentconnect.app.Fmt;
import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.Contract;
import com.rentconnect.app.model.Message;
import com.rentconnect.app.model.Role;
import com.rentconnect.app.model.SupportTicket;
import com.rentconnect.app.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** FR05 – danh sách hội thoại + yêu cầu hỗ trợ. */
public class ChatListFragment extends RecyclerFragment {
    @Override
    public String title() {
        return me() != null && me().role == Role.ADMIN ? "Hỗ trợ" : "Tin nhắn";
    }

    @Override
    public boolean isTab() {
        return true;
    }

    private static String shorten(String s, int n) {
        return s.length() > n ? s.substring(0, n) : s;
    }

    private Row supportRow(SupportTicket s, boolean showSender) {
        return new Row(R.layout.item_support, v -> {
            Rows.show(v, R.id.tv_sender, showSender);
            if (showSender) {
                User u = Store.user(s.userId);
                Rows.set(v, R.id.tv_sender, (u == null ? "?" : u.name) + " · " + Fmt.formatTime(s.ts));
            }
            Rows.set(v, R.id.tv_content, shorten(s.content, showSender ? 100 : 80));
            boolean waiting = s.reply == null || s.reply.trim().isEmpty();
            Ui.setTag(v.findViewById(R.id.tag_status), waiting ? "Chờ phản hồi" : "Đã phản hồi",
                    waiting ? Ui.Tone.AMBER : Ui.Tone.GREEN);
            v.setOnClickListener(b -> push(BaseFragment.withId(new SupportDetailFragment(), s.id)));
        });
    }

    private Row chatRow(String pid, String lastText) {
        return new Row(R.layout.item_chat, v -> {
            User u = Store.user(pid);
            Rows.set(v, R.id.tv_name, u == null ? "" : u.name + " (" + u.role.label + ")");
            boolean has = lastText != null;
            Rows.show(v, R.id.tv_last, has);
            if (has) Rows.set(v, R.id.tv_last, lastText);
            v.setOnClickListener(b -> push(BaseFragment.withId(new ChatFragment(), pid)));
        });
    }

    @Override
    protected void build(List<Row> rows) {
        User me = me();
        if (me == null) return;
        List<String> partners = Store.partners(me.id);

        List<SupportTicket> supports = new ArrayList<>();
        for (SupportTicket s : Store.state().supports) {
            if (me.role == Role.ADMIN || me.id.equals(s.userId)) supports.add(s);
        }
        Collections.sort(supports, (a, b) -> Long.compare(b.ts, a.ts));

        if (me.role != Role.ADMIN) {
            rows.add(Rows.outline(getString(R.string.s_gui_yeu_cau_ho_tro_toi_admin), b -> push(new SupportFormFragment())));
            if (!supports.isEmpty()) {
                rows.add(Rows.section(getString(R.string.s_yeu_cau_ho_tro_cua_toi)));
                for (SupportTicket s : supports) rows.add(supportRow(s, false));
            }
        } else {
            int waiting = 0;
            for (SupportTicket s : supports) if (s.reply == null || s.reply.trim().isEmpty()) waiting++;
            rows.add(Rows.section("Yêu cầu hỗ trợ (" + waiting + " chờ)"));
            if (supports.isEmpty()) rows.add(Rows.empty(getString(R.string.s_chua_co_yeu_cau_ho_tro)));
            for (SupportTicket s : supports) rows.add(supportRow(s, true));
        }

        rows.add(Rows.section(getString(R.string.s_tro_chuyen)));
        if (partners.isEmpty()) {
            rows.add(Rows.empty("Chưa có cuộc trò chuyện. Mở một tin phòng và chọn \"Nhắn tin cho chủ trọ\"."));
        }
        for (String pid : partners) {
            if (Store.user(pid) == null) continue;
            List<Message> th = Store.thread(me.id, pid);
            String last = th.isEmpty() ? null : shorten(th.get(th.size() - 1).text, 60);
            rows.add(chatRow(pid, last));
        }

        Set<String> contacts = new LinkedHashSet<>();
        for (Contract k : Store.contractsFor(me)) {
            String other = me.role == Role.TENANT ? k.landlordId : k.tenantId;
            if (!partners.contains(other)) contacts.add(other);
        }
        if (!contacts.isEmpty()) {
            rows.add(Rows.section(getString(R.string.s_bat_dau_tro_chuyen)));
            for (String pid : contacts) if (Store.user(pid) != null) rows.add(chatRow(pid, null));
        }
    }
}
