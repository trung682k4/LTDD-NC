package com.rentconnect.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.rentconnect.app.R;
import com.rentconnect.app.Seed;
import com.rentconnect.app.Store;

public class LoginFragment extends BaseFragment {
    private TextView error;

    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    @Override
    public String title() {
        return getString(R.string.s_dang_nhap);
    }

    @Override
    public boolean canGoBack() {
        return false;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        error = v(R.id.error);
        TextView demo = v(R.id.demo_info);
        demo.setText(getString(R.string.demo_accounts, Seed.DEMO_PASSWORD, Seed.LANDLORD, Seed.TENANT, Seed.ADMIN));

        v(R.id.btn_login).setOnClickListener(b -> {
            String contact = Ui.text(v(R.id.contact));
            String pass = Ui.text(v(R.id.password));
            Ui.show(error, Store.login(contact, pass));
        });
        v(R.id.btn_biometric).setOnClickListener(b -> biometric());
        v(R.id.link_register).setOnClickListener(b -> push(new RegisterFragment()));
    }

    private void biometric() {
        if (Store.biometricUser() == null) {
            Ui.show(error, getString(R.string.s_hay_dang_nhap_bang_mat_khau_mot_lan_de));
            return;
        }
        int ok = BiometricManager.from(ctx()).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK);
        if (ok != BiometricManager.BIOMETRIC_SUCCESS) {
            Ui.show(error, getString(R.string.s_thiet_bi_chua_co_hoac_chua_cai_dat_van));
            return;
        }
        BiometricPrompt prompt = new BiometricPrompt(this, ContextCompat.getMainExecutor(ctx()),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        Ui.show(error, Store.loginBiometric());
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        Ui.show(error, errString.toString());
                    }
                });
        prompt.authenticate(new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.s_dang_nhap_rentconnect))
                .setSubtitle("Dùng vân tay hoặc khuôn mặt")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .setNegativeButtonText("Hủy")
                .build());
    }
}
