package com.example.webviewupload;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

        try {
            return ParcelFileDescriptorUtils.pipeFrom(new FileInputStream(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
