package com.rentconnect.app.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rentconnect.app.R;

/** Dòng "nhãn ..... giá trị". Khai báo trong XML: app:kvKey="@string/..." ; đặt giá trị bằng setValue(). */
public class KeyValueView extends LinearLayout {
    private final TextView key;
    private final TextView value;

    public KeyValueView(Context c) {
        this(c, null);
    }

    public KeyValueView(Context c, AttributeSet a) {
        super(c, a);
        setOrientation(VERTICAL);
        LayoutInflater.from(c).inflate(R.layout.view_key_value, this, true);
        key = findViewById(R.id.kv_key);
        value = findViewById(R.id.kv_value);
        if (a != null) {
            TypedArray t = c.obtainStyledAttributes(a, R.styleable.KeyValueView);
            String k = t.getString(R.styleable.KeyValueView_kvKey);
            if (k != null) key.setText(k);
            if (t.getBoolean(R.styleable.KeyValueView_kvBold, false)) {
                key.setTypeface(key.getTypeface(), Typeface.BOLD);
                value.setTypeface(value.getTypeface(), Typeface.BOLD);
                value.setTextColor(c.getColor(R.color.black));
            }
            t.recycle();
        }
    }

    public void setKey(String k) {
        key.setText(k);
    }

    public void setValue(String v) {
        value.setText(v);
    }
}
