package com.rentconnect.app.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rentconnect.app.Fmt;
import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.Fee;
import com.rentconnect.app.model.Role;
import com.rentconnect.app.model.Room;
import com.rentconnect.app.model.RoomStatus;
import com.rentconnect.app.model.User;

import java.util.ArrayList;
import java.util.List;

/** FR03 – chi tiết phòng; layout fragment_room_detail.xml, nút hành động đổi theo vai trò. */
public class RoomDetailFragment extends BaseFragment {
    public RoomDetailFragment() {
        super(R.layout.fragment_room_detail);
    }

    @Override
    public String title() {
        return getString(R.string.s_chi_tiet_phong);
    }

    static String feesText(Room r) {
        if (r.fees == null || r.fees.isEmpty()) return "Không";
        List<String> parts = new ArrayList<>();
        for (Fee f : r.fees) parts.add(f.name + " " + Fmt.formatMoney(f.amount));
        return String.join("; ", parts);
    }

    static String amenText(Room r) {
        return r.amenities == null || r.amenities.isEmpty() ? "—" : String.join(", ", r.amenities);
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
        Room r = Store.room(arg());
        if (me == null) return;
        if (r == null) {
            text(R.id.tv_title, getString(R.string.s_khong_tim_thay_phong));
            return;
        }
        User owner = Store.user(r.ownerId);

        Ui.gallery(v(R.id.thumb1), v(R.id.thumb2), r.images, "Hình ảnh / Video");
        text(R.id.tv_title, r.title);
        boolean notTenant = me.role != Role.TENANT;
        visible(R.id.tag_status, notTenant);
        if (notTenant) Ui.setTag(v(R.id.tag_status), r.status.label, Ui.Tone.PRIMARY);
        kv(R.id.kv_price, Fmt.formatMoney(r.rent) + " / " + Fmt.formatMoney(r.deposit));
        kv(R.id.kv_area, r.area + " m² / " + r.capacity + " người");
        kv(R.id.kv_address, r.getFullAddress());
        kv(R.id.kv_utilities, Fmt.formatMoney(r.elecPrice) + "/kWh / " + Fmt.formatMoney(r.waterPrice) + "/khối");
        kv(R.id.kv_fees, feesText(r));
        kv(R.id.kv_amen, amenText(r));
        kv(R.id.kv_owner, owner == null ? "—" : owner.name + " · " + owner.contact);

        visible(R.id.btn_book, me.role == Role.TENANT);
        visible(R.id.btn_chat, me.role == Role.TENANT && owner != null);
        click(R.id.btn_book, b -> push(BaseFragment.withId(new BookingFragment(), r.id)));
        click(R.id.btn_chat, b -> {
            if (owner != null) push(BaseFragment.withId(new ChatFragment(), owner.id));
        });
        visible(R.id.btn_moderate, me.role == Role.ADMIN && r.status != RoomStatus.ACTIVE);
        click(R.id.btn_moderate, b -> push(BaseFragment.withId(new ModerateFragment(), r.id)));
        visible(R.id.btn_contract, me.role == Role.LANDLORD && me.id.equals(r.ownerId)
                && r.status == RoomStatus.ACTIVE && Store.activeContractOfRoom(r.id) == null);
        click(R.id.btn_contract, b -> push(BaseFragment.withId(new ContractFormFragment(), r.id)));

        boolean note = r.adminNote != null && !r.adminNote.trim().isEmpty();
        visible(R.id.tv_admin_note, note);
        if (note) text(R.id.tv_admin_note, "Ghi chú của Admin: " + r.adminNote);
    }
}
