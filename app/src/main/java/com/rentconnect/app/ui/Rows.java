package com.rentconnect.app.ui;

import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.rentconnect.app.R;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Các dòng dùng chung cho nhiều danh sách (mỗi dòng có layout XML riêng). */
public final class Rows {
    private Rows() {
    }

    /** Gắn chữ vào TextView có id trong view của một dòng. */
    public static void set(View root, int id, String s) {
        ((TextView) root.findViewById(id)).setText(s);
    }

    public static void show(View root, int id, boolean visible) {
        root.findViewById(id).setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public static void kv(View root, int id, String value) {
        ((KeyValueView) root.findViewById(id)).setValue(value);
    }

    public static Row section(String text) {
        return new Row(R.layout.item_section, v -> ((TextView) v).setText(text));
    }

    public static Row empty(String text) {
        return new Row(R.layout.item_empty, v -> ((TextView) v).setText(text));
    }

    public static Row info(String text, Ui.Tone tone) {
        return new Row(R.layout.item_info, v -> {
            TextView t = (TextView) v;
            t.setText(text);
            t.setTextColor(Ui.color(v.getContext(), tone.color));
            t.setBackgroundResource(tone.infoBg);
        });
    }

    public static Row primary(String text, int colorRes, View.OnClickListener l) {
        return new Row(R.layout.item_button_primary, v -> {
            MaterialButton b = (MaterialButton) v;
            b.setText(text);
            b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Ui.color(v.getContext(), colorRes)));
            b.setOnClickListener(l);
        });
    }

    public static Row primary(String text, View.OnClickListener l) {
        return primary(text, R.color.primary, l);
    }

    public static Row outline(String text, int colorRes, View.OnClickListener l) {
        return new Row(R.layout.item_button_outline, v -> {
            MaterialButton b = (MaterialButton) v;
            int c = Ui.color(v.getContext(), colorRes);
            b.setText(text);
            b.setTextColor(c);
            b.setStrokeColor(android.content.res.ColorStateList.valueOf(c));
            b.setOnClickListener(l);
        });
    }

    public static Row outline(String text, View.OnClickListener l) {
        return outline(text, R.color.primary, l);
    }

    /** Hàng các nút lọc (pill). */
    public static Row filters(List<String> options, Predicate<String> selected, Consumer<String> onClick) {
        return new Row(R.layout.item_filters, v -> Ui.pills(v.findViewById(R.id.chips), options, selected, onClick));
    }

    public static Row menu(String text, View.OnClickListener l) {
        return new Row(R.layout.item_menu, v -> {
            ((TextView) v.findViewById(R.id.tv_title)).setText(text);
            v.setOnClickListener(l);
        });
    }
}
