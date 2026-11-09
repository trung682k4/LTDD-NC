package com.rentconnect.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rentconnect.app.Fmt;
import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.Room;
import com.rentconnect.app.model.RoomStatus;

/** FR02 – Admin: phê duyệt / từ chối / yêu cầu bổ sung (fragment_moderate.xml). */
public class ModerateFragment extends BaseFragment {
    public ModerateFragment() {
        super(R.layout.fragment_moderate);
    }

    @Override
    public String title() {
        return getString(R.string.s_kiem_duyet_tin_dang);
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
        Room r = Store.room(arg());
        if (r == null) {
            text(R.id.tv_title, getString(R.string.s_khong_tim_thay_tin));
            return;
        }
        Ui.setTag(v(R.id.tag_status), r.status.label, Ui.Tone.PRIMARY);
        Ui.gallery(v(R.id.thumb1), v(R.id.thumb2), r.images, "Hình ảnh phòng");
        text(R.id.tv_title, r.title);
        kv(R.id.kv_address, r.getFullAddress());
        kv(R.id.kv_area, r.area + " m² / " + r.capacity + " người");
        kv(R.id.kv_price, Fmt.formatMoney(r.rent) + " / " + Fmt.formatMoney(r.deposit));
        kv(R.id.kv_utilities, Fmt.formatMoney(r.elecPrice) + " / " + Fmt.formatMoney(r.waterPrice));
        kv(R.id.kv_fees, RoomDetailFragment.feesText(r));
        kv(R.id.kv_amen, RoomDetailFragment.amenText(r));

        click(R.id.btn_approve, b -> {
            Store.moderate(r.id, RoomStatus.ACTIVE);
            toast(getString(R.string.s_da_phe_duyet));
            pop();
        });
        click(R.id.btn_reject, b -> askNote(r.id, RoomStatus.REJECTED));
        click(R.id.btn_need_info, b -> askNote(r.id, RoomStatus.NEED_INFO));
    }

    private void askNote(String roomId, RoomStatus status) {
        View form = LayoutInflater.from(ctx()).inflate(R.layout.dialog_note, null);
        EditText note = form.findViewById(R.id.et);
        note.setHint(getString(R.string.s_nhap_noi_dung_gui_cho_chu_tro));
        new MaterialAlertDialogBuilder(ctx())
                .setTitle(status == RoomStatus.REJECTED ? "Lý do từ chối" : "Nội dung cần bổ sung")
                .setView(form)
                .setPositiveButton(R.string.send, (d, w) -> {
                    Store.moderate(roomId, status, Ui.text(note));
                    pop();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
