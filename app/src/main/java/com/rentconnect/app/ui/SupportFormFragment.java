package com.rentconnect.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rentconnect.app.R;
import com.rentconnect.app.Store;

/** FR05 – gửi yêu cầu hỗ trợ tới Admin (Support Ticket). */
public class SupportFormFragment extends BaseFragment {
    private final PhotoHelper photoHelper = new PhotoHelper(this);

    public SupportFormFragment() {
        super(R.layout.fragment_support_form);
    }

    @Override
    public String title() {
        return getString(R.string.s_yeu_cau_ho_tro_support_ticket);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PhotoStrip files = v(R.id.files);
        files.setHelper(photoHelper);
        TextView error = v(R.id.error);
        v(R.id.btn_submit).setOnClickListener(b -> {
            String e = Store.createSupport(Ui.text(v(R.id.content)), files.getPaths());
            if (e == null) {
                toast(getString(R.string.s_da_gui_yeu_cau_toi_admin));
                pop();
            } else {
                Ui.show(error, e);
            }
        });
    }
}
