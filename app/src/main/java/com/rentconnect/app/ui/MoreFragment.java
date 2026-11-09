package com.rentconnect.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.VietQr;
import com.rentconnect.app.model.User;

import java.util.ArrayList;
import java.util.List;

/** Tab "Thêm": hồ sơ, menu theo vai trò, công tắc ngoại tuyến, đăng xuất. */
public class MoreFragment extends RecyclerFragment {
    @Override
    public String title() {
        return getString(R.string.add);
    }

    @Override
    public boolean isTab() {
        return true;
    }

    @Override
    protected void build(List<Row> rows) {
        User user = me();
        if (user == null) return;
        rows.add(new Row(R.layout.item_profile, v -> {
            Rows.set(v, R.id.tv_name, user.name);
            Rows.set(v, R.id.tv_sub, user.contact + " · " + user.role.label);
        }));

        switch (user.role) {
            case TENANT:
                rows.add(Rows.menu(getString(R.string.s_lich_xem_phong_cua_toi), b -> push(new ViewingsFragment())));
                rows.add(Rows.menu(getString(R.string.s_hop_dong_cua_toi), b -> push(new ContractListFragment())));
                rows.add(Rows.menu(getString(R.string.s_dien_nuoc_cua_toi), b -> push(new MyMeterFragment())));
                break;
            case LANDLORD:
                rows.add(Rows.menu(getString(R.string.s_lich_hen_xem_phong), b -> push(new ViewingsFragment())));
                rows.add(Rows.menu(getString(R.string.s_hop_dong), b -> push(new ContractListFragment())));
                rows.add(Rows.menu(getString(R.string.s_chot_chi_so_dien_nuoc), b -> push(new MeterPickFragment())));
                String bank = user.bankAcc == null || user.bankAcc.trim().isEmpty()
                        ? " – chưa cập nhật" : " – " + user.bankName + " " + user.bankAcc;
                rows.add(Rows.menu("Tài khoản nhận tiền (VietQR)" + bank, b -> showBankDialog(user)));
                break;
            default:
                rows.add(Rows.menu(getString(R.string.s_tao_du_lieu_gia_lap_50_phong_150_nguoi), b -> {
                    if (Store.state().users.size() < 100) {
                        Store.generateMock();
                        toast(getString(R.string.s_da_tao_mock_dataset));
                    } else {
                        toast(getString(R.string.s_du_lieu_gia_lap_da_ton_tai));
                    }
                }));
        }
        rows.add(Rows.menu(getString(R.string.notifications), b -> push(new NotificationsFragment())));

        rows.add(new Row(R.layout.item_offline_toggle, v -> {
            MaterialSwitch sw = v.findViewById(R.id.offline_switch);
            sw.setOnCheckedChangeListener(null);
            sw.setChecked(Store.isForceOffline());
            sw.setOnCheckedChangeListener((b, checked) -> {
                Store.setForceOffline(checked);
                if (!checked) Store.syncPending();
            });
        }));

        int pending = Store.pendingCount();
        if (pending > 0) {
            rows.add(Rows.primary("Đồng bộ ngay (" + pending + " bản ghi)", b -> Store.syncPending()));
        }
        rows.add(Rows.info("Dữ liệu được lưu cục bộ và mã hóa AES-256 (Android Keystore).", Ui.Tone.PRIMARY));
        rows.add(Rows.outline(getString(R.string.s_dang_xuat), R.color.red, b -> Store.logout()));
    }

    private void showBankDialog(User user) {
        View form = LayoutInflater.from(ctx()).inflate(R.layout.dialog_bank, null);
        AutoCompleteTextView bankView = form.findViewById(R.id.bank);
        EditText acc = form.findViewById(R.id.account);
        List<String> names = new ArrayList<>();
        for (VietQr.Bank b : VietQr.BANKS) names.add(b.name);
        final String[] bank = {user.bankName == null ? "" : user.bankName};
        Ui.dropdown(bankView, names, bank[0], s -> bank[0] = s);
        acc.setText(user.bankAcc == null ? "" : user.bankAcc);

        new MaterialAlertDialogBuilder(ctx())
                .setTitle(getString(R.string.s_tai_khoan_nhan_tien))
                .setView(form)
                .setPositiveButton(R.string.save, (d, w) -> {
                    String bin = "";
                    for (VietQr.Bank b : VietQr.BANKS) if (b.name.equals(bank[0])) bin = b.bin;
                    String a = Ui.text(acc).trim();
                    if (!bin.isEmpty() && a.length() >= 6) Store.updateBank(bank[0], bin, a);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
