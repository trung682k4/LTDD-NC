package com.rentconnect.app.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rentconnect.app.R;

import java.util.ArrayList;
import java.util.List;

/** Màn hình dạng danh sách: RecyclerView + RowAdapter. Lớp con chỉ cần dựng danh sách Row trong build(). */
public abstract class RecyclerFragment extends BaseFragment {
    protected RecyclerView recycler;
    private final RowAdapter adapter = new RowAdapter();

    protected RecyclerFragment() {
        super(R.layout.fragment_list);
    }

    protected abstract void build(List<Row> rows);

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recycler = view.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);
        refresh();
    }

    protected void refresh() {
        if (recycler == null || !isAdded()) return;
        List<Row> rows = new ArrayList<>();
        build(rows);
        adapter.submit(rows);
        updateChrome();
    }

    @Override
    public void onStoreChanged() {
        refresh();
    }

    @Override
    public void onDestroyView() {
        recycler = null;
        super.onDestroyView();
    }
}
