package com.rentconnect.app.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.rentconnect.app.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Các hàm dựng giao diện dùng chung (tương đương các Composable trong Ui.kt cũ). */
public final class Ui {
    private Ui() {
    }

    /** Tông màu cho nhãn trạng thái và hộp thông tin. */
    public enum Tone {
        PRIMARY(R.color.primary, R.drawable.bg_tag_primary, R.drawable.bg_info_soft),
        GREEN(R.color.green, R.drawable.bg_tag_green, R.drawable.bg_info_green),
        RED(R.color.red, R.drawable.bg_tag_red, R.drawable.bg_info_red),
        AMBER(R.color.amber, R.drawable.bg_tag_amber, R.drawable.bg_info_amber);

        @ColorRes final int color;
        @DrawableRes final int tagBg;
        @DrawableRes final int infoBg;

        Tone(int color, int tagBg, int infoBg) {
            this.color = color;
            this.tagBg = tagBg;
            this.infoBg = infoBg;
        }
    }

    // ---------- cơ bản ----------
    public static void toast(Context c, String msg) {
        Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
    }

    public static int dp(Context c, int v) {
        return (int) (v * c.getResources().getDisplayMetrics().density);
    }

    public static int color(Context c, @ColorRes int res) {
        return ContextCompat.getColor(c, res);
    }

    private static View inflate(ViewGroup parent, int layout) {
        return LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
    }

    public static String text(EditText e) {
        return e.getText() == null ? "" : e.getText().toString();
    }

    /** TextInputLayout bao quanh một ô nhập (để đổi hint động). */
    public static TextInputLayout layoutOf(View v) {
        ViewParent p = v.getParent();
        while (p != null && !(p instanceof TextInputLayout)) p = p.getParent();
        return (TextInputLayout) p;
    }

    public static int toInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception ex) {
            return 0;
        }
    }

    public static void show(TextView errorView, String error) {
        if (error == null || error.trim().isEmpty()) {
            errorView.setVisibility(View.GONE);
        } else {
            errorView.setText(error);
            errorView.setVisibility(View.VISIBLE);
        }
    }

    // ---------- khối nội dung ----------
    public static TextView keyValue(ViewGroup parent, String key, String value) {
        return keyValue(parent, key, value, false);
    }

    public static TextView keyValue(ViewGroup parent, String key, String value, boolean bold) {
        View v = inflate(parent, R.layout.view_key_value);
        TextView k = v.findViewById(R.id.kv_key);
        TextView val = v.findViewById(R.id.kv_value);
        k.setText(key);
        val.setText(value);
        if (bold) {
            k.setTypeface(k.getTypeface(), android.graphics.Typeface.BOLD);
            val.setTypeface(val.getTypeface(), android.graphics.Typeface.BOLD);
            val.setTextColor(color(parent.getContext(), R.color.black));
        }
        parent.addView(v);
        return val;
    }

    public static TextView info(ViewGroup parent, String text) {
        return info(parent, text, Tone.PRIMARY);
    }

    public static TextView info(ViewGroup parent, String text, Tone tone) {
        TextView t = (TextView) inflate(parent, R.layout.view_info_box);
        t.setText(text);
        t.setTextColor(color(parent.getContext(), tone.color));
        t.setBackgroundResource(tone.infoBg);
        parent.addView(t);
        return t;
    }

    public static TextView tag(ViewGroup parent, String text, Tone tone) {
        TextView t = (TextView) inflate(parent, R.layout.view_status_tag);
        t.setText(text);
        t.setTextColor(color(parent.getContext(), tone.color));
        t.setBackgroundResource(tone.tagBg);
        parent.addView(t);
        return t;
    }

    /** Gắn nội dung + màu cho một nhãn trạng thái đã có sẵn trong layout (item_*.xml). */
    public static void setTag(TextView t, String text, Tone tone) {
        t.setText(text);
        t.setTextColor(color(t.getContext(), tone.color));
        t.setBackgroundResource(tone.tagBg);
        t.setVisibility(View.VISIBLE);
    }

    public static TextView label(ViewGroup parent, String text) {
        TextView t = (TextView) inflate(parent, R.layout.view_label);
        t.setText(text);
        parent.addView(t);
        return t;
    }

    public static TextView section(ViewGroup parent, String text) {
        TextView t = (TextView) inflate(parent, R.layout.view_section_title);
        t.setText(text);
        parent.addView(t);
        return t;
    }

    public static TextView empty(ViewGroup parent, String text) {
        TextView t = (TextView) inflate(parent, R.layout.view_empty);
        t.setText(text);
        parent.addView(t);
        return t;
    }

    /** Dòng chữ thường (đen). */
    public static TextView text(ViewGroup parent, String text) {
        TextView t = (TextView) inflate(parent, R.layout.view_text);
        t.setText(text);
        parent.addView(t);
        return t;
    }

    public static TextView bold(ViewGroup parent, String text) {
        TextView t = text(parent, text);
        t.setTypeface(t.getTypeface(), android.graphics.Typeface.BOLD);
        return t;
    }

    public static TextView muted(ViewGroup parent, String text) {
        TextView t = text(parent, text);
        t.setTextColor(color(parent.getContext(), R.color.muted));
        return t;
    }

    public static TextView colored(ViewGroup parent, String text, @ColorRes int color) {
        TextView t = text(parent, text);
        t.setTextColor(color(parent.getContext(), color));
        return t;
    }

    /** Dòng "tiêu đề ..... giá trị" (tiêu đề đậm, giá trị màu chủ đạo). */
    public static void titleValue(ViewGroup parent, String title, String value) {
        View v = inflate(parent, R.layout.view_title_value);
        ((TextView) v.findViewById(R.id.tv_title)).setText(title);
        ((TextView) v.findViewById(R.id.tv_value)).setText(value);
        parent.addView(v);
    }

    /** Hàng ngang, các con cách nhau 12dp. */
    public static LinearLayout row(ViewGroup parent) {
        LinearLayout l = (LinearLayout) inflate(parent, R.layout.view_row);
        parent.addView(l);
        return l;
    }

    /** Thẻ viền bo; trả về chính thẻ để thêm nội dung vào. */
    public static LinearLayout card(ViewGroup parent, View.OnClickListener onClick) {
        LinearLayout c = (LinearLayout) inflate(parent, R.layout.item_card);
        if (onClick != null) c.setOnClickListener(onClick);
        parent.addView(c);
        return c;
    }

    public static LinearLayout card(ViewGroup parent) {
        return card(parent, null);
    }

    // ---------- nút ----------
    public static MaterialButton primary(ViewGroup parent, String text, View.OnClickListener l) {
        return primary(parent, text, R.color.primary, l);
    }

    public static MaterialButton primary(ViewGroup parent, String text, @ColorRes int color, View.OnClickListener l) {
        MaterialButton b = (MaterialButton) inflate(parent, R.layout.btn_primary);
        b.setText(text);
        b.setBackgroundTintList(ColorStateList.valueOf(color(parent.getContext(), color)));
        b.setOnClickListener(l);
        parent.addView(b);
        return b;
    }

    public static MaterialButton outline(ViewGroup parent, String text, View.OnClickListener l) {
        return outline(parent, text, R.color.primary, l);
    }

    public static MaterialButton outline(ViewGroup parent, String text, @ColorRes int color, View.OnClickListener l) {
        MaterialButton b = (MaterialButton) inflate(parent, R.layout.btn_outline);
        int c = color(parent.getContext(), color);
        b.setText(text);
        b.setTextColor(c);
        b.setStrokeColor(ColorStateList.valueOf(c));
        b.setOnClickListener(l);
        parent.addView(b);
        return b;
    }

    /** Cho nút chiếm đều chiều ngang trong hàng ngang. */
    public static void weight(View v) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
        lp.width = 0;
        lp.weight = 1;
        v.setLayoutParams(lp);
    }

    // ---------- pill ----------
    public static TextView pill(ViewGroup parent, String text, boolean selected, View.OnClickListener l) {
        TextView t = (TextView) inflate(parent, R.layout.view_pill);
        t.setText(text);
        t.setSelected(selected);
        if (l != null) t.setOnClickListener(l);
        else t.setClickable(false);
        parent.addView(t);
        return t;
    }

    /** Dựng lại cả nhóm pill; bấm vào pill thì gọi onClick rồi tự cập nhật trạng thái chọn. */
    public static void pills(ViewGroup group, List<String> options, Predicate<String> selected, Consumer<String> onClick) {
        group.removeAllViews();
        for (String o : options) {
            pill(group, o, selected.test(o), v -> {
                onClick.accept(o);
                pills(group, options, selected, onClick);
            });
        }
    }

    // ---------- dropdown ----------
    public static void dropdown(AutoCompleteTextView v, List<String> options, String selected, Consumer<String> onSelect) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(v.getContext(), android.R.layout.simple_list_item_1,
                new ArrayList<>(options));
        v.setAdapter(adapter);
        v.setText(selected == null ? "" : selected, false);
        v.setOnItemClickListener((p, view, pos, id) -> onSelect.accept(adapter.getItem(pos)));
    }

    // ---------- ảnh ----------
    /** Gắn ảnh vào khung view_thumb: có ảnh thì hiện ảnh, không thì hiện nhãn. */
    public static void bindThumb(View thumb, String path, String label) {
        ImageView img = thumb.findViewById(R.id.thumb_img);
        TextView lb = thumb.findViewById(R.id.thumb_label);
        Bitmap bmp = null;
        if (path != null && !path.isEmpty()) {
            try {
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inSampleSize = 4;
                bmp = BitmapFactory.decodeFile(path, o);
            } catch (Exception ignored) {
            }
        }
        img.setImageBitmap(bmp);
        lb.setText(label);
        lb.setVisibility(bmp == null ? View.VISIBLE : View.GONE);
    }

    /** Gắn tối đa 2 ảnh vào hai khung có sẵn; emptyLabel == null thì ẩn khung khi không có ảnh. */
    public static void gallery(View t1, View t2, List<String> images, String emptyLabel) {
        boolean has = images != null && !images.isEmpty();
        t1.setVisibility(has || emptyLabel != null ? View.VISIBLE : View.GONE);
        bindThumb(t1, has ? images.get(0) : "", has ? "Ảnh" : emptyLabel == null ? "" : emptyLabel);
        boolean second = images != null && images.size() > 1;
        t2.setVisibility(second ? View.VISIBLE : View.GONE);
        if (second) bindThumb(t2, images.get(1), "Ảnh");
    }

    /** Thêm một ô ảnh vào hàng ngang (chia đều chiều ngang). */
    public static View thumb(ViewGroup row, String path, String label, int heightDp, View.OnClickListener l) {
        View v = inflate(row, R.layout.view_thumb);
        v.getLayoutParams().height = (int) (heightDp * row.getResources().getDisplayMetrics().density);
        weight(v);
        bindThumb(v, path, label);
        if (l != null) v.setOnClickListener(l);
        row.addView(v);
        return v;
    }

    // ---------- mã QR ----------
    public static Bitmap qrBitmap(String content, int size) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix m = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints);
            int[] px = new int[size * size];
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) px[y * size + x] = m.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
            }
            Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            bmp.setPixels(px, 0, size, 0, 0, size, size);
            return bmp;
        } catch (Exception e) {
            return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        }
    }

    public static ImageView qr(ViewGroup parent, String payload) {
        ImageView iv = (ImageView) inflate(parent, R.layout.view_qr);
        iv.setImageBitmap(qrBitmap(payload, 640));
        parent.addView(iv);
        return iv;
    }

    public static <T> List<T> orEmpty(List<T> l) {
        return l == null ? Collections.<T>emptyList() : l;
    }
}
