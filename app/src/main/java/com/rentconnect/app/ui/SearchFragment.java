package com.rentconnect.app.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.rentconnect.app.Fmt;
import com.rentconnect.app.R;
import com.rentconnect.app.Seed;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.Room;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** FR03 – Tenant: tìm kiếm & lọc phòng (có phân trang 5 phòng/trang). */
public class SearchFragment extends BaseFragment {
    private static final List<String> PRICES = Arrays.asList("Dưới 3 triệu", "3 – 5 triệu", "Trên 5 triệu");
    private static final List<String> AREAS = Arrays.asList("Từ 20 m²", "Từ 30 m²");
    private static final int PER_PAGE = 5;
    private static final String ALL = "Tất cả";

    // trạng thái (giữ khi quay lại từ màn hình chi tiết)
    private String keyword = "";
    private String province = "";
    private String priceRange = "";
    private String areaRange = "";
    private final Set<String> amen = new HashSet<>();
    private int page = 0;
    private boolean showFilters = false;

    private TextView pillArea, pillPrice, pillAreaRange, pillAmen, count, pageText;
    private View filterCard, pager;
    private RecyclerView results;
    private final RowAdapter adapter = new RowAdapter();

    public SearchFragment() {
        super(R.layout.fragment_search);
    }

    @Override
    public String title() {
        return getString(R.string.s_tim_phong);
    }

    @Override
    public boolean isTab() {
        return true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pillArea = v(R.id.pill_area);
        pillPrice = v(R.id.pill_price);
        pillAreaRange = v(R.id.pill_area_range);
        pillAmen = v(R.id.pill_amen);
        filterCard = v(R.id.filter_card);
        results = v(R.id.results);
        results.setLayoutManager(new LinearLayoutManager(requireContext()));
        results.setAdapter(adapter);
        count = v(R.id.count);
        pager = v(R.id.pager);
        pageText = v(R.id.page_text);

        TextInputEditText kw = v(R.id.keyword);
        kw.setText(keyword);
        kw.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable e) {
                keyword = e.toString();
                page = 0;
                update();
            }
        });

        View.OnClickListener toggle = b -> {
            showFilters = !showFilters;
            filterCard.setVisibility(showFilters ? View.VISIBLE : View.GONE);
        };
        pillArea.setOnClickListener(toggle);
        pillPrice.setOnClickListener(toggle);
        pillAreaRange.setOnClickListener(toggle);
        pillAmen.setOnClickListener(toggle);
        filterCard.setVisibility(showFilters ? View.VISIBLE : View.GONE);

        bindFilters();
        v(R.id.btn_clear).setOnClickListener(b -> {
            province = "";
            priceRange = "";
            areaRange = "";
            amen.clear();
            keyword = "";
            page = 0;
            kw.setText("");
            bindFilters();
            update();
        });
        v(R.id.btn_prev).setOnClickListener(b -> {
            if (page > 0) { page--; update(); }
        });
        v(R.id.btn_next).setOnClickListener(b -> {
            page++;
            update();
        });
        update();
    }

    /** Gắn dropdown + các nhóm pill trong thẻ bộ lọc theo trạng thái hiện tại. */
    private void bindFilters() {
        List<String> provinces = new ArrayList<>();
        provinces.add(ALL);
        provinces.addAll(Seed.PROVINCES.keySet());
        Ui.dropdown((AutoCompleteTextView) v(R.id.province), provinces, province.isEmpty() ? ALL : province, s -> {
            province = ALL.equals(s) ? "" : s;
            page = 0;
            update();
        });
        Ui.pills(v(R.id.price_group), PRICES, s -> s.equals(priceRange), s -> {
            priceRange = priceRange.equals(s) ? "" : s;
            page = 0;
            update();
        });
        Ui.pills(v(R.id.area_group), AREAS, s -> s.equals(areaRange), s -> {
            areaRange = areaRange.equals(s) ? "" : s;
            page = 0;
            update();
        });
        Ui.pills(v(R.id.amen_group), Seed.AMENITY_LIST, amen::contains, s -> {
            if (!amen.remove(s)) amen.add(s);
            page = 0;
            update();
        });
    }

    private Store.Filter buildFilter() {
        Store.Filter f = new Store.Filter();
        f.keyword = keyword;
        f.province = province;
        if (priceRange.equals("3 – 5 triệu")) f.minPrice = 3_000_000;
        else if (priceRange.equals("Trên 5 triệu")) f.minPrice = 5_000_001;
        if (priceRange.equals("Dưới 3 triệu")) f.maxPrice = 2_999_999;
        else if (priceRange.equals("3 – 5 triệu")) f.maxPrice = 5_000_000;
        if (areaRange.equals("Từ 20 m²")) f.minArea = 20;
        else if (areaRange.equals("Từ 30 m²")) f.minArea = 30;
        f.amenities = new HashSet<>(amen);
        return f;
    }

    /** Cập nhật nhãn bộ lọc + danh sách kết quả. */
    private void update() {
        if (getView() == null) return;
        pillArea.setText("Khu vực" + (province.isEmpty() ? "" : ": " + province));
        pillArea.setSelected(!province.isEmpty());
        pillPrice.setSelected(!priceRange.isEmpty());
        pillAreaRange.setSelected(!areaRange.isEmpty());
        pillAmen.setSelected(!amen.isEmpty());

        List<Room> all = Store.search(buildFilter());
        int pages = Math.max(1, (all.size() + PER_PAGE - 1) / PER_PAGE);
        if (page >= pages) page = 0;

        count.setText(all.size() + " phòng đang hiển thị");
        List<Row> rows = new ArrayList<>();
        if (all.isEmpty()) rows.add(Rows.empty(getString(R.string.s_khong_tim_thay_phong_phu_hop)));
        int from = page * PER_PAGE;
        int to = Math.min(all.size(), from + PER_PAGE);
        for (Room r : all.subList(from, to)) {
            rows.add(new Row(R.layout.item_room, item -> {
                Ui.bindThumb(item.findViewById(R.id.room_thumb), r.images.isEmpty() ? "" : r.images.get(0), "Ảnh");
                Rows.set(item, R.id.room_title, r.title);
                Rows.set(item, R.id.room_area, r.getAreaText() + " · " + r.area + " m²");
                Rows.set(item, R.id.room_price, Fmt.formatMoney(r.rent) + "/tháng");
                item.setOnClickListener(b -> push(BaseFragment.withId(new RoomDetailFragment(), r.id)));
            }));
        }
        adapter.submit(rows);
        pager.setVisibility(pages > 1 ? View.VISIBLE : View.GONE);
        pageText.setText("Trang " + (page + 1) + " / " + pages);
        v(R.id.btn_prev).setEnabled(page > 0);
        v(R.id.btn_next).setEnabled(page < pages - 1);
    }

    @Override
    public void onStoreChanged() {
        update();
    }
}
