package com.rentconnect.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.Role;

public class RegisterFragment extends BaseFragment {
    private Role role = Role.TENANT;
    private TextView pillTenant;
    private TextView pillLandlord;

    public RegisterFragment() {
        super(R.layout.fragment_register);
    }

    @Override
    public String title() {
        return getString(R.string.s_dang_ky_tai_khoan);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pillTenant = v(R.id.pill_tenant);
        pillLandlord = v(R.id.pill_landlord);
        pillTenant.setOnClickListener(b -> setRole(Role.TENANT));
        pillLandlord.setOnClickListener(b -> setRole(Role.LANDLORD));
        setRole(role);

        TextView error = v(R.id.error);
        v(R.id.btn_register).setOnClickListener(b -> {
            String e = Store.startRegister(
                    Ui.text(v(R.id.name)).trim(), Ui.text(v(R.id.id_no)).trim(),
                    Ui.text(v(R.id.contact)).trim(), Ui.text(v(R.id.password)), role);
            if (e == null) push(new OtpFragment());
            else Ui.show(error, e);
        });
        v(R.id.link_login).setOnClickListener(b -> pop());
    }

    private void setRole(Role r) {
        role = r;
        pillTenant.setSelected(r == Role.TENANT);
        pillLandlord.setSelected(r == Role.LANDLORD);
    }
}
