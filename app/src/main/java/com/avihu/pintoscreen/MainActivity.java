package com.avihu.pintoscreen;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button b;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 0);
        }

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        }else{
            this.finishAffinity();
        }

/*
        b = (Button)findViewById(R.id.btn);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(MainActivity.this,FloatingWindow.class));
            }
        });*/
    }


    void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            //Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            Intent in = new Intent(MainActivity.this,FloatingWindow.class);
            in.putExtra("imageUri", imageUri);
            startService(in);
            this.finishAffinity();
            //mImageView.setImageBitmap(bitmap1);

            // Update UI to reflect image being shared
        }
    }


}