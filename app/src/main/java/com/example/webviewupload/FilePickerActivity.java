package com.example.webviewupload;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class FilePickerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_file_picker);

        Button filePicker = findViewById(R.id.single_file_view);
        File file = createTextFile();
        filePicker.setText(file.getName());

        filePicker.setOnClickListener(v -> {
            Intent intent = new Intent();
            Uri uri = FileUploadProvider.getContentUri(file.getName());
            intent.setData(uri);
            setResult(1, intent);
            finish();
        });
    }

    private File createTextFile() {
        File file = new File(getCacheDir(), SystemClock.uptimeMillis() + ".txt");
        try {
            file.createNewFile();
            Files.write(file.toPath(), Arrays.asList("This is a test"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return file;
    }
}
