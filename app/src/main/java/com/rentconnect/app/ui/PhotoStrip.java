package com.rentconnect.app.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.rentconnect.app.R;

import java.util.ArrayList;
import java.util.List;

/** Dải ảnh đính kèm: tối đa 2 ảnh xem trước + ô "+ Thêm" (chụp ảnh / chọn thư viện). Chạm ảnh để xóa. */
public class PhotoStrip extends LinearLayout {
    private final LinearLayout row;
    private final View addTile;
    private final TextView hint;
    private PhotoHelper helper;
    private final List<String> paths = new ArrayList<>();

    public PhotoStrip(Context c) {
        this(c, null);
    }

    public PhotoStrip(Context c, AttributeSet a) {
        super(c, a);
        setOrientation(VERTICAL);
        LayoutInflater.from(c).inflate(R.layout.view_photo_strip, this, true);
        row = findViewById(R.id.strip_row);
        addTile = findViewById(R.id.strip_add);
        hint = findViewById(R.id.strip_hint);
        addTile.setOnClickListener(v -> {
            PopupMenu m = new PopupMenu(getContext(), addTile);
            m.getMenu().add(0, 1, 0, "Chụp ảnh");
            m.getMenu().add(0, 2, 1, "Chọn từ thư viện");
            m.setOnMenuItemClickListener(item -> {
                if (helper == null) return true;
                if (item.getItemId() == 1) helper.camera(this::add);
                else helper.gallery(this::add);
                return true;
            });
            m.show();
        });
    }

    public void setHelper(PhotoHelper helper) {
        this.helper = helper;
    }

    public List<String> getPaths() {
        return new ArrayList<>(paths);
    }

    private void add(String path) {
        paths.add(path);
        render();
    }

    private void render() {
        row.removeView(addTile);
        row.removeAllViews();
        for (int i = 0; i < Math.min(2, paths.size()); i++) {
            final int idx = i;
            Ui.thumb(row, paths.get(i), "Ảnh " + (i + 1), 80, v -> {
                paths.remove(idx);
                render();
            });
        }
        row.addView(addTile);
        if (paths.size() > 2) {
            hint.setText("Đã đính kèm " + paths.size() + " ảnh (chạm vào ảnh để xóa).");
            hint.setVisibility(VISIBLE);
        } else {
            hint.setVisibility(GONE);
        }
    }
}
