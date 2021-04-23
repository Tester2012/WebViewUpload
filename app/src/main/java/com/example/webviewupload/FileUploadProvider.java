package com.example.webviewupload;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.ProxyFileDescriptorCallback;
import android.os.storage.StorageManager;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class FileUploadProvider extends ContentProvider {

    public static final String PROVIDER_AUTHORITY = "com.example.webviewupload.fileuploadprovider";

    public static Uri getContentUri(String name) {
        return new Uri.Builder()
                .scheme("content")
                .authority(PROVIDER_AUTHORITY)
                .appendEncodedPath(name)
                .appendQueryParameter(OpenableColumns.DISPLAY_NAME, name)
                .build();
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        List<String> segments = uri.getPathSegments();
        File file = new File(getContext().getApplicationContext().getCacheDir(),
                TextUtils.join(File.separator, segments));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            StorageManager storageManager = (StorageManager) getContext().getSystemService(Context.STORAGE_SERVICE);
            ParcelFileDescriptor descriptor;
            try {
                descriptor = storageManager.openProxyFileDescriptor(
                        ParcelFileDescriptor.MODE_READ_ONLY,
                        new CustomProxyFileDescriptorCallback(file),
                        new Handler(Looper.getMainLooper()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return descriptor;
        }

        throw new RuntimeException("This is not supported for API < 26");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static class CustomProxyFileDescriptorCallback extends ProxyFileDescriptorCallback {
        private ByteArrayInputStream inputStream;
        private long length;

        public CustomProxyFileDescriptorCallback(File file) {
            try {
                byte[] fileBytes = Files.readAllBytes(file.toPath());
                length = fileBytes.length;
                inputStream = new ByteArrayInputStream(fileBytes);
            } catch (IOException e) {
                // do nothing here
            }

        }

        @Override
        public long onGetSize() {
            return length;
        }

        @Override
        public int onRead(long offset, int size, byte[] out) {
            inputStream.skip(offset);
            return inputStream.read(out,0, size);
        }

        @Override
        public void onRelease() {
            try {
                inputStream.close();
            } catch (IOException e) {
                //ignore this for now
            }
            inputStream = null;
        }
    }
}
