package com.rentconnect.app.ui;

import com.rentconnect.app.R;
import com.rentconnect.app.Store;
import com.rentconnect.app.model.Contract;
import com.rentconnect.app.model.ContractStatus;
import com.rentconnect.app.model.Room;
import com.rentconnect.app.model.User;

import java.util.List;

/** FR06 – danh sách hợp đồng. */
public class ContractListFragment extends RecyclerFragment {
    @Override
    public String title() {
        return getString(R.string.s_hop_dong);
    }

    static String name(String userId) {
        User u = Store.user(userId);
        return u == null ? "null" : u.name;
    }

    @Override
    protected void build(List<Row> rows) {
        User me = me();
        if (me == null) return;
        List<Contract> list = Store.contractsFor(me);
        if (list.isEmpty()) rows.add(Rows.empty(getString(R.string.s_chua_co_hop_dong)));
        for (Contract k : list) {
            rows.add(new Row(R.layout.item_contract, v -> {
                Room r = Store.room(k.roomId);
                Rows.set(v, R.id.tv_title, r == null ? k.roomId : r.title);
                Rows.set(v, R.id.tv_dates, k.start + " → " + k.end);
                Rows.set(v, R.id.tv_parties, name(k.landlordId) + " ↔ " + name(k.tenantId));
                Ui.setTag(v.findViewById(R.id.tag_status), k.status.label,
                        k.status == ContractStatus.ACTIVE ? Ui.Tone.GREEN : Ui.Tone.PRIMARY);
                v.setOnClickListener(b -> push(BaseFragment.withId(new ContractViewFragment(), k.id)));
            }));
        }
    }
}
