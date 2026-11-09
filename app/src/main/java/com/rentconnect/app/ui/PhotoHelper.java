package com.rentconnect.app.ui;

import android.content.Context;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.rentconnect.app.Store;

import java.io.File;
import java.util.function.Consumer;

/**
 * Mở camera hệ thống / thư viện ảnh (thay cho rememberCamera, rememberGallery của Compose).
 * PHẢI được tạo khi khởi tạo Fragment (khai báo trường), trước khi Fragment STARTED.
 */
public class PhotoHelper {
    private final Fragment fragment;
    private Consumer<String> target;
    private File pending;

    private final ActivityResultLauncher<Uri> cameraLauncher;
    private final ActivityResultLauncher<String> galleryLauncher;

    public PhotoHelper(Fragment fragment) {
        this.fragment = fragment;
        cameraLauncher = fragment.registerForActivityResult(new ActivityResultContracts.TakePicture(), ok -> {
            File f = pending;
            pending = null;
            if (f == null) return;
            if (ok && target != null) target.accept(f.getAbsolutePath());
            else f.delete();
        });
        galleryLauncher = fragment.registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri == null || target == null) return;
            try (java.io.InputStream in = fragment.requireContext().getContentResolver().openInputStream(uri)) {
                if (in != null) target.accept(Store.fileCopy(in));
            } catch (Exception ignored) {
            }
        });
    }

    public void camera(Consumer<String> onPhoto) {
        Context ctx = fragment.requireContext();
        target = onPhoto;
        File f = new File(ctx.getFilesDir(), "img_" + System.currentTimeMillis() + ".jpg");
        pending = f;
        try {
            Uri uri = FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".fileprovider", f);
            cameraLauncher.launch(uri);
        } catch (Exception e) {
            Ui.toast(ctx, "Không mở được camera trên thiết bị này.");
        }
    }

    public void gallery(Consumer<String> onPhoto) {
        target = onPhoto;
        galleryLauncher.launch("image/*");
    }
}
