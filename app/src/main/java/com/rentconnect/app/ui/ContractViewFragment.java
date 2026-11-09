package com.rentconnect.app.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rentconnect.app.Fmt;
import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.Contract;
import com.rentconnect.app.model.ContractStatus;
import com.rentconnect.app.model.Room;
import com.rentconnect.app.model.User;

/** FR06 – rà soát & xác nhận hợp đồng (fragment_contract_view.xml). */
public class ContractViewFragment extends BaseFragment {
    public ContractViewFragment() {
        super(R.layout.fragment_contract_view);
    }

    @Override
    public String title() {
        return getString(R.string.s_xac_nhan_hop_dong);
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
        User me = me();
        Contract k = Store.contract(arg());
        if (me == null || k == null) return;
        Room r = Store.room(k.roomId);

        v(R.id.pill_draft).setSelected(k.status == ContractStatus.DRAFT);
        v(R.id.pill_active).setSelected(k.status == ContractStatus.ACTIVE);
        kv(R.id.kv_parties, ContractListFragment.name(k.landlordId) + " / " + ContractListFragment.name(k.tenantId));
        kv(R.id.kv_room, k.roomId + " – " + (r == null ? "" : r.title));
        kv(R.id.kv_period, k.start + " → " + k.end);
        kv(R.id.kv_price, Fmt.formatMoney(k.rent) + " / " + Fmt.formatMoney(k.deposit));
        kv(R.id.kv_cycle, k.cycleMonths + " tháng/lần");
        kv(R.id.kv_billing, "Ngày " + k.billingDay + " hàng tháng");
        kv(R.id.kv_terms, k.terms == null || k.terms.trim().isEmpty() ? "—" : k.terms);

        boolean canConfirm = me.id.equals(k.tenantId) && k.status == ContractStatus.DRAFT;
        visible(R.id.btn_confirm, canConfirm);
        visible(R.id.info_confirm, canConfirm);
        click(R.id.btn_confirm, b -> {
            Store.confirmContract(k.id);
            toast(getString(R.string.s_hop_dong_da_co_hieu_luc));
        });
        visible(R.id.info_readonly, k.status == ContractStatus.ACTIVE);
        visible(R.id.info_waiting, k.status == ContractStatus.DRAFT && me.id.equals(k.landlordId));
    }
}
