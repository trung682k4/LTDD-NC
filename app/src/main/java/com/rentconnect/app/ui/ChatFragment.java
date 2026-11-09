package com.rentconnect.app.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

import com.rentconnect.app.Fmt;
import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.Message;
import com.rentconnect.app.model.Role;
import com.rentconnect.app.model.User;

import java.util.List;

/** FR05 – màn hình trò chuyện. */
public class ChatFragment extends BaseFragment {
    private LinearLayout messages;
    private NestedScrollView scroll;
    private int lastCount = -1;

    public ChatFragment() {
        super(R.layout.fragment_chat);
    }

    @Override
    public String title() {
        User p = Store.user(arg());
        return p == null ? "Trò chuyện" : p.name;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        messages = v(R.id.messages);
        scroll = v(R.id.scroll);
        EditText input = v(R.id.input);
        View send = v(R.id.btn_send);

        User me = me();
        v(R.id.btn_support).setVisibility(me != null && me.role != Role.ADMIN ? View.VISIBLE : View.GONE);
        v(R.id.btn_support).setOnClickListener(b -> push(new SupportFormFragment()));

        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable e) {
                send.setEnabled(!e.toString().trim().isEmpty());
            }
        });
        send.setOnClickListener(b -> {
            Store.sendMessage(arg(), Ui.text(input));
            input.setText("");
        });
        bindMessages();
    }

    @Override
    public void onStoreChanged() {
        if (getView() != null) bindMessages();
    }

    private void bindMessages() {
        User me = me();
        if (me == null) return;
        List<Message> msgs = Store.thread(me.id, arg());
        messages.removeAllViews();
        LayoutInflater inf = LayoutInflater.from(ctx());
        for (Message m : msgs) {
            boolean mine = me.id.equals(m.fromId);
            LinearLayout item = (LinearLayout) inf.inflate(R.layout.item_message, messages, false);
            item.setGravity(mine ? Gravity.END : Gravity.START);
            TextView text = item.findViewById(R.id.msg_text);
            text.setText(m.text);
            text.setBackgroundResource(mine ? R.drawable.bg_bubble_mine : R.drawable.bg_bubble_other);
            ((TextView) item.findViewById(R.id.msg_time)).setText(Fmt.formatTime(m.ts));
            messages.addView(item);
        }
        if (msgs.size() != lastCount) {
            lastCount = msgs.size();
            scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
        }
    }
}
