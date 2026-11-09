package com.rentconnect.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rentconnect.app.Fmt;
import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.VietQr;
import com.rentconnect.app.model.Invoice;
import com.rentconnect.app.model.InvoiceStatus;
import com.rentconnect.app.model.User;

/** FR09 – Tenant: thanh toán bằng mã VietQR (fragment_pay.xml). */
public class PayFragment extends BaseFragment {
    public PayFragment() {
        super(R.layout.fragment_pay);
    }

    @Override
    public String title() {
        return getString(R.string.s_thanh_toan_hoa_don);
    }

    private static boolean blank(String s) {
        return s == null || s.trim().isEmpty();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bind();
    }

    @Override
    public void onStoreChanged() {
        if (getView() != null) bind();
    }

    private void bind() {
        Invoice inv = Store.invoice(arg());
        if (inv == null) {
            text(R.id.tv_code, getString(R.string.s_khong_tim_thay_hoa_don));
            return;
        }
        User landlord = Store.user(inv.landlordId);
        text(R.id.tv_code, "Mã hóa đơn: " + inv.getCode());
        kv(R.id.kv_total, Fmt.formatMoney(inv.total));

        boolean noBank = landlord == null || blank(landlord.bankBin) || blank(landlord.bankAcc);
        visible(R.id.no_bank_box, noBank);
        visible(R.id.bank_box, !noBank);
        if (noBank) {
            click(R.id.btn_chat_landlord, b -> {
                if (landlord != null) push(BaseFragment.withId(new ChatFragment(), landlord.id));
            });
        } else {
            String content = inv.getCode() + " " + inv.period.replace("/", "");
            ((ImageView) v(R.id.qr)).setImageBitmap(
                    Ui.qrBitmap(VietQr.build(landlord.bankBin, landlord.bankAcc, inv.total, content), 640));
            kv(R.id.kv_bank, landlord.bankName);
            kv(R.id.kv_acc, landlord.bankAcc);
            kv(R.id.kv_owner, landlord.name);
            kv(R.id.kv_content, content);
        }
        kv(R.id.kv_status, inv.status.label);
        visible(R.id.info_wait, inv.status == InvoiceStatus.ISSUED);
    }
}
