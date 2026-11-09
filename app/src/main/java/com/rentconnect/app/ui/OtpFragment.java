package com.rentconnect.app.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rentconnect.app.R;
import com.rentconnect.app.Store;

public class OtpFragment extends BaseFragment {
    public OtpFragment() {
        super(R.layout.fragment_otp);
    }

    @Override
    public String title() {
        return getString(R.string.s_xac_thuc_tai_khoan);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText otp = v(R.id.otp);
        TextView error = v(R.id.error);

        String demo = Store.lastOtp();
        if (demo != null && !demo.trim().isEmpty()) {
            TextView t = v(R.id.otp_demo);
            t.setText(getString(R.string.otp_demo, demo));
            t.setVisibility(View.VISIBLE);
        }

        v(R.id.btn_verify).setOnClickListener(b -> {
            String e = Store.verifyOtp(Ui.text(otp));
            if (e == null) {
                toast(getString(R.string.s_tao_tai_khoan_thanh_cong_hay_dang_nhap));
                host().popToRoot();
            } else {
                Ui.show(error, e);
            }
        });

        otp.post(() -> {
            if (!isAdded()) return;
            otp.requestFocus();
            InputMethodManager imm = (InputMethodManager) ctx().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(otp, InputMethodManager.SHOW_IMPLICIT);
        });
    }
}
