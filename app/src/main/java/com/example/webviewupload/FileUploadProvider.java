package com.example.webviewupload;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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

        // Approach 2
//        ParcelFileDescriptor fileDescriptor1 = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
//        return fileDescriptor1;

        //Approach 3
//        InputStream inputStream = new FileInputStream(file);
//        ParcelFileDescriptor[] pipe;
//        try {
//            pipe = ParcelFileDescriptor.createReliablePipe();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        ParcelFileDescriptor readPart = pipe[0];
//        ParcelFileDescriptor writePart = pipe[1];
//        OutputStream outputStream = null;
//        try {
//            outputStream = new ParcelFileDescriptor.AutoCloseOutputStream(writePart);
//            IOUtils.copy(inputStream, outputStream);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } finally {
//            try {
//                inputStream.close();
//                outputStream.flush();
//                outputStream.close();
//            } catch (IOException e) {
//                //
//            }
//        }
//
//        return readPart;

        // Approach 4
        try {
            MemoryFile memoryFile = new MemoryFile(file.getName(), (int) file.length());
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            memoryFile.writeBytes(fileBytes, 0, 0, (int) file.length());
            Method method = memoryFile.getClass().getDeclaredMethod("getFileDescriptor");
            FileDescriptor fileDescriptor = (FileDescriptor) method.invoke(memoryFile);
            Constructor<ParcelFileDescriptor> constructor = ParcelFileDescriptor.class.getConstructor(FileDescriptor.class);
            ParcelFileDescriptor parcelFileDescriptor = constructor.newInstance(fileDescriptor);
            return parcelFileDescriptor;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
