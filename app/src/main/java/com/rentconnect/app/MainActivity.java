package com.rentconnect.app;

import android.Manifest;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.rentconnect.app.model.Role;
import com.rentconnect.app.model.User;
import com.rentconnect.app.ui.BaseFragment;
import com.rentconnect.app.ui.ChatListFragment;
import com.rentconnect.app.ui.InvoiceListFragment;
import com.rentconnect.app.ui.LoginFragment;
import com.rentconnect.app.ui.ModerationFragment;
import com.rentconnect.app.ui.MoreFragment;
import com.rentconnect.app.ui.MyRoomsFragment;
import com.rentconnect.app.ui.NotificationsFragment;
import com.rentconnect.app.ui.SearchFragment;
import com.rentconnect.app.ui.TicketListFragment;

/**
 * Activity duy nhất: thanh trên + vùng chứa Fragment + thanh điều hướng dưới.
 * Chưa đăng nhập -> Login/Register/OTP; đã đăng nhập -> các tab theo vai trò, màn hình con được push lên back stack.
 */
public class MainActivity extends AppCompatActivity implements Store.Listener {
    private static final String KEY_SHOWN = "shown_user";

    private final Handler main = new Handler(Looper.getMainLooper());
    private ConnectivityManager.NetworkCallback callback;

    private MaterialToolbar toolbar;
    private BottomNavigationView bottom;
    private TextView banner;
    private View notifAction;
    private TextView notifBadge;

    /** Người dùng mà giao diện hiện tại đang dựng cho ("" = chưa đăng nhập, null = chưa dựng). */
    private String shownUser;
    private int tab = 0;
    private Role navRole;

    private final ActivityResultLauncher<String> notifPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                // người dùng tự quyết định
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Store.init(getApplicationContext());
        Notifier.createChannel(this);
        if (Build.VERSION.SDK_INT >= 33) notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        watchNetwork();

        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        bottom = findViewById(R.id.bottom_nav);
        banner = findViewById(R.id.offline_banner);

        toolbar.inflateMenu(R.menu.menu_toolbar);
        MenuItem bell = toolbar.getMenu().findItem(R.id.action_notifications);
        notifAction = bell.getActionView();
        notifBadge = notifAction.findViewById(R.id.badge);
        notifAction.setOnClickListener(v -> push(new NotificationsFragment()));
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        bottom.setOnItemSelectedListener(item -> {
            int idx = indexOf(item);
            if (idx >= 0 && idx != tab) showTab(idx);
            return true;
        });

        if (savedInstanceState != null) shownUser = savedInstanceState.getString(KEY_SHOWN);
        syncRoot();
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putString(KEY_SHOWN, shownUser);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Store.addListener(this);
        syncRoot();
    }

    @Override
    protected void onStop() {
        Store.removeListener(this);
        super.onStop();
    }

    // ---------- Store thay đổi ----------
    @Override
    public void onStoreChanged() {
        if (syncRoot()) return;
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.container);
        if (f instanceof BaseFragment && f.isAdded()) {
            ((BaseFragment) f).onStoreChanged();
            applyChrome((BaseFragment) f);
        }
    }

    private String uid() {
        String id = Store.currentUserId();
        return id == null ? "" : id;
    }

    /** Nếu đăng nhập/đăng xuất thì dựng lại gốc điều hướng. Trả về true nếu đã dựng lại. */
    private boolean syncRoot() {
        String cur = uid();
        if (cur.equals(shownUser)) return false;
        shownUser = cur;
        tab = 0;
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        User me = Store.me();
        if (me == null) {
            fm.beginTransaction().replace(R.id.container, new LoginFragment()).commit();
        } else {
            navRole = null;
            fm.beginTransaction().replace(R.id.container, tabFragment(me.role, 0)).commit();
        }
        return true;
    }

    // ---------- điều hướng ----------
    public void push(Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, f)
                .addToBackStack(null)
                .commit();
    }

    public void pop() {
        getSupportFragmentManager().popBackStack();
    }

    /** Quay về màn hình gốc (xóa toàn bộ back stack). */
    public void popToRoot() {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    private void showTab(int i) {
        User me = Store.me();
        if (me == null) return;
        tab = i;
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction().replace(R.id.container, tabFragment(me.role, i)).commit();
    }

    private static Fragment tabFragment(Role role, int i) {
        switch (role) {
            case TENANT:
                switch (i) {
                    case 0: return new SearchFragment();
                    case 1: return new InvoiceListFragment();
                    case 2: return new TicketListFragment();
                    case 3: return new ChatListFragment();
                    default: return new MoreFragment();
                }
            case LANDLORD:
                switch (i) {
                    case 0: return new MyRoomsFragment();
                    case 1: return new InvoiceListFragment();
                    case 2: return new TicketListFragment();
                    case 3: return new ChatListFragment();
                    default: return new MoreFragment();
                }
            default: // ADMIN
                switch (i) {
                    case 0: return new ModerationFragment();
                    case 1: return new ChatListFragment();
                    default: return new MoreFragment();
                }
        }
    }

    private int indexOf(MenuItem item) {
        for (int i = 0; i < bottom.getMenu().size(); i++) {
            if (bottom.getMenu().getItem(i).getItemId() == item.getItemId()) return i;
        }
        return -1;
    }

    /** Cập nhật thanh trên, thanh dưới, chuông thông báo, banner ngoại tuyến theo màn hình đang hiện. */
    public void applyChrome(BaseFragment f) {
        toolbar.setTitle(f.title());
        if (f.canGoBack()) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationContentDescription(R.string.back);
        } else {
            toolbar.setNavigationIcon(null);
        }

        User me = Store.me();
        boolean showTabs = f.isTab() && me != null;
        if (showTabs) {
            if (navRole != me.role) {
                navRole = me.role;
                bottom.getMenu().clear();
                bottom.inflateMenu(me.role == Role.TENANT ? R.menu.nav_tenant
                        : me.role == Role.LANDLORD ? R.menu.nav_landlord : R.menu.nav_admin);
            }
            int idx = Math.min(tab, bottom.getMenu().size() - 1);
            bottom.getMenu().getItem(idx).setChecked(true);
        }
        bottom.setVisibility(showTabs ? View.VISIBLE : View.GONE);

        toolbar.getMenu().findItem(R.id.action_notifications).setVisible(showTabs);
        int n = me == null ? 0 : Store.unreadCount();
        notifBadge.setVisibility(n > 0 ? View.VISIBLE : View.GONE);
        notifBadge.setText(String.valueOf(n));

        banner.setVisibility(showTabs && !Store.isOnline() ? View.VISIBLE : View.GONE);
    }

    // ---------- mạng ----------
    /** Theo dõi mạng: khi có Internet trở lại thì tự đồng bộ dữ liệu điện nước lưu ngoại tuyến. */
    private void watchNetwork() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
        Store.setNetOnline(caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET));
        callback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                main.post(() -> {
                    Store.setNetOnline(true);
                    Store.syncPending();
                });
            }

            @Override
            public void onLost(Network network) {
                main.post(() -> Store.setNetOnline(false));
            }
        };
        cm.registerDefaultNetworkCallback(callback);
    }

    @Override
    protected void onDestroy() {
        if (callback != null) {
            ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).unregisterNetworkCallback(callback);
        }
        super.onDestroy();
    }
}
