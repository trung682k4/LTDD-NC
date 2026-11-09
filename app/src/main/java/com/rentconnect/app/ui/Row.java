package com.rentconnect.app.ui;

import android.view.View;

import androidx.annotation.LayoutRes;

import java.util.function.Consumer;

/** Một dòng trong RecyclerView: layout XML của dòng + hàm gắn dữ liệu vào view. */
public final class Row {
    @LayoutRes
    public final int layout;
    public final Consumer<View> binder;

    public Row(@LayoutRes int layout, Consumer<View> binder) {
        this.layout = layout;
        this.binder = binder;
    }
}
