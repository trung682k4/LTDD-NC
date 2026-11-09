package com.rentconnect.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rentconnect.app.Fmt;
import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.Role;
import com.rentconnect.app.model.SupportTicket;
import com.rentconnect.app.model.User;

/** FR05 – chi tiết yêu cầu hỗ trợ; Admin nhập phản hồi (fragment_support_detail.xml). */
public class SupportDetailFragment extends BaseFragment {
    public SupportDetailFragment() {
        super(R.layout.fragment_support_detail);
    }

    @Override
    public String title() {
        return getString(R.string.s_yeu_cau_ho_tro);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        click(R.id.btn_reply, b -> {
            SupportTicket s = Store.support(arg());
            if (s == null) return;
            EditText input = v(R.id.reply_input);
            Store.replySupport(s.id, Ui.text(input));
            input.setText("");
        });
        bind();
    }

    @Override
    public void onStoreChanged() {
        if (getView() != null) bind();
    }

    private void bind() {
        User me = me();
        SupportTicket s = Store.support(arg());
        if (me == null || s == null) return;
        User from = Store.user(s.userId);
        text(R.id.tv_from, "Từ: " + (from == null ? "?" : from.name) + " · " + Fmt.formatTime(s.ts));
        text(R.id.tv_content, s.content);

        View t1 = v(R.id.thumb1), t2 = v(R.id.thumb2), t3 = v(R.id.thumb3);
        int n = s.attachments == null ? 0 : s.attachments.size();
        View[] thumbs = {t1, t2, t3};
        for (int i = 0; i < thumbs.length; i++) {
            thumbs[i].setVisibility(i < n ? View.VISIBLE : View.GONE);
            if (i < n) Ui.bindThumb(thumbs[i], s.attachments.get(i), "Ảnh");
        }

        boolean replied = s.reply != null && !s.reply.trim().isEmpty();
        visible(R.id.reply_box, replied);
        visible(R.id.reply_form, !replied && me.role == Role.ADMIN);
        visible(R.id.tv_none, !replied && me.role != Role.ADMIN);
        if (replied) {
            text(R.id.tv_reply, s.reply);
            text(R.id.tv_reply_time, Fmt.formatTime(s.replyTs));
        }
    }
}
