package com.rentconnect.app.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.fragment.app.Fragment;

import com.rentconnect.app.MainActivity;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.User;

/** Lớp cha cho mọi màn hình: tiêu đề thanh trên, điều hướng, nhận thay đổi từ Store. */
public abstract class BaseFragment extends Fragment {
    private static final String ARG_ID = "id";

    protected BaseFragment(@LayoutRes int layout) {
        super(layout);
    }

    /** Tiêu đề hiển thị trên thanh trên cùng. */
    public abstract String title();

    /** Tab gốc (có thanh điều hướng dưới, không có nút quay lại). */
    public boolean isTab() {
        return false;
    }

    public boolean canGoBack() {
        return !isTab();
    }

    /** Store đổi (dữ liệu / mạng): màn hình động tự dựng lại. */
    public void onStoreChanged() {
    }

    protected MainActivity host() {
        return (MainActivity) requireActivity();
    }

    protected Context ctx() {
        return requireContext();
    }

    protected User me() {
        return Store.me();
    }

    protected <T extends View> T v(int id) {
        return requireView().findViewById(id);
    }

    protected void push(Fragment f) {
        host().push(f);
    }

    protected void pop() {
        host().pop();
    }

    // ---- gắn dữ liệu vào view có sẵn trong layout XML ----
    protected void text(int id, String s) {
        ((android.widget.TextView) v(id)).setText(s);
    }

    protected void kv(int id, String s) {
        ((KeyValueView) v(id)).setValue(s);
    }

    protected void visible(int id, boolean show) {
        v(id).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    protected void click(int id, View.OnClickListener l) {
        v(id).setOnClickListener(l);
    }

    protected void toast(String msg) {
        Ui.toast(ctx(), msg);
    }

    protected void updateChrome() {
        if (isResumed()) host().applyChrome(this);
    }

    protected String arg() {
        Bundle b = getArguments();
        return b == null ? "" : b.getString(ARG_ID, "");
    }

    public static <T extends Fragment> T withId(T f, String id) {
        Bundle b = new Bundle();
        b.putString(ARG_ID, id);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onResume() {
        super.onResume();
        host().applyChrome(this);
    }
}
