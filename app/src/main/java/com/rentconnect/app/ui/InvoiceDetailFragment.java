package com.rentconnect.app.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rentconnect.app.Billing;
import com.rentconnect.app.Fmt;
import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.Invoice;
import com.rentconnect.app.model.InvoiceStatus;
import com.rentconnect.app.model.LogEntry;
import com.rentconnect.app.model.Role;
import com.rentconnect.app.model.User;

/** FR08 – chi tiết / duyệt & xuất hóa đơn (fragment_invoice_detail.xml). */
public class InvoiceDetailFragment extends BaseFragment {
    private boolean negative = false;
    private TextView error;

    public InvoiceDetailFragment() {
        super(R.layout.fragment_invoice_detail);
    }

    @Override
    public String title() {
        Invoice inv = Store.invoice(arg());
        if (inv == null) return "Hóa đơn";
        return "Hóa đơn kỳ " + inv.period + " (" + (inv.status == InvoiceStatus.DRAFT ? "bản nháp" : "đã xuất") + ")";
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        error = v(R.id.error);
        click(R.id.pill_plus, b -> {
            negative = false;
            bind();
        });
        click(R.id.pill_minus, b -> {
            negative = true;
            bind();
        });
        ((EditText) v(R.id.adjust)).addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable e) {
                bind();
            }
        });
        bind();
    }

    @Override
    public void onStoreChanged() {
        if (getView() != null) bind();
    }

    private long adjustValue(Invoice inv, boolean isDraft) {
        if (!isDraft) return inv.adjust;
        long amount = Fmt.toLong(Ui.text(v(R.id.adjust)));
        return negative ? -amount : amount;
    }

    private void bind() {
        User me = me();
        Invoice inv = Store.invoice(arg());
        updateChrome();
        if (me == null || inv == null) return;
        boolean isDraft = inv.status == InvoiceStatus.DRAFT;
        long adj = adjustValue(inv, isDraft);
        long sum = isDraft ? Billing.total(inv.rentAmt, inv.elecAmt, inv.waterAmt, inv.feeAmt, adj) : inv.total;

        visible(R.id.info_tenant, me.role == Role.TENANT && inv.status == InvoiceStatus.ISSUED);
        kv(R.id.kv_rent, Fmt.formatMoney(inv.rentAmt));
        ((KeyValueView) v(R.id.kv_elec)).setKey("Tiền điện (" + inv.elecUsed + " kWh)");
        kv(R.id.kv_elec, Fmt.formatMoney(inv.elecAmt));
        ((KeyValueView) v(R.id.kv_water)).setKey("Tiền nước (" + inv.waterUsed + " khối)");
        kv(R.id.kv_water, Fmt.formatMoney(inv.waterAmt));
        kv(R.id.kv_fee, Fmt.formatMoney(inv.feeAmt));

        visible(R.id.kv_adjust, !isDraft);
        if (!isDraft) {
            boolean hasReason = inv.adjustReason != null && !inv.adjustReason.trim().isEmpty();
            kv(R.id.kv_adjust, Fmt.formatMoney(inv.adjust) + (hasReason ? " (" + inv.adjustReason + ")" : ""));
        }
        boolean canAdjust = isDraft && me.role == Role.LANDLORD;
        visible(R.id.adjust_box, canAdjust);
        v(R.id.pill_plus).setSelected(!negative);
        v(R.id.pill_minus).setSelected(negative);
        kv(R.id.kv_total, Fmt.formatMoney(sum));

        visible(R.id.btn_issue, canAdjust);
        click(R.id.btn_issue, b -> {
            String e = Store.issueInvoice(inv.id, adjustValue(inv, true), Ui.text(v(R.id.reason)));
            if (e == null) {
                toast(getString(R.string.s_da_xuat_hoa_don_va_thong_bao_cho_nguoi));
                pop();
            } else {
                Ui.show(error, e);
            }
        });
        visible(R.id.btn_pay, inv.status == InvoiceStatus.ISSUED && me.role == Role.TENANT);
        click(R.id.btn_pay, b -> push(BaseFragment.withId(new PayFragment(), inv.id)));
        boolean landlordIssued = inv.status == InvoiceStatus.ISSUED && me.role == Role.LANDLORD;
        visible(R.id.btn_paid, landlordIssued);
        visible(R.id.info_landlord, landlordIssued);
        click(R.id.btn_paid, b -> Store.markPaid(inv.id));

        LinearLayout history = v(R.id.history);
        history.removeAllViews();
        if (inv.history != null) for (LogEntry l : inv.history) Ui.keyValue(history, Fmt.formatTime(l.ts), l.text);
    }
}
