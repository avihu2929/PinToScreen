package com.avihu.pintoscreen;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class FloatingWindow extends Service {

    private WindowManager wm;
    private LinearLayout ll;
    Uri uri;
    Button btn;
    View view;
    BitmapDrawable bd;
    WindowManager.LayoutParams params;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        ll = new LinearLayout(this);
        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        ll.setBackgroundColor(Color.argb(66,255,0,0));

        ll.setLayoutParams(llParams);

        LayoutInflater inflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        view = inflater.inflate(R.layout.test_dialog, null);
        btn = (Button) view.findViewById(R.id.button2);





        //    Button stop = new Button(this);

        //  stop.setText("X");
        //   ViewGroup.LayoutParams btnParameters = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        //  stop.setLayoutParams(btnParameters);



        uri = intent.getParcelableExtra("imageUri");
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
             bd = (BitmapDrawable) Drawable.createFromStream(inputStream, uri.toString() );


        } catch (FileNotFoundException e) {
            bd = (BitmapDrawable) getResources().getDrawable(R.drawable.test);
        }
        int h= bd.getBitmap().getHeight();
        int w=bd.getBitmap().getWidth();
        params = new WindowManager.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        params.x = 0;
        params.y = 0;
        params.gravity = Gravity.CENTER | Gravity.CENTER;
        view.setBackground(bd);
        ll.addView(view);

        wm.addView(ll,params);


        ll.setOnTouchListener(new View.OnTouchListener() {

            WindowManager.LayoutParams updatedParameters = params;
            double x;
            double y;
            double pressedX;
            double pressedY;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        x = updatedParameters.x;
                        y = updatedParameters.y;

                        pressedX = motionEvent.getRawX();
                        pressedY = motionEvent.getRawY();

                        break;

                    case MotionEvent.ACTION_MOVE:
                        updatedParameters.x = (int) (x + (motionEvent.getRawX() - pressedX));
                        updatedParameters.y = (int) (y + (motionEvent.getRawY() - pressedY));

                        wm.updateViewLayout(ll, updatedParameters);

                    default:
                        break;
                }


                return false;
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wm.removeView(ll);
              /*  String path = uri.uri.getPath();
                File fdelete = new File(uri.getPath());
                if (fdelete.exists()) {
                    if (fdelete.delete()) {
                        System.out.println("file Deleted :" + uri.getPath());
                    } else {
                        System.out.println("file not Deleted :" + uri.getPath());
                    }
                }*/
                stopSelf();
                //finishAffinity();
                System.exit(0);
            }
        });


        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onCreate() {
        super.onCreate();

       /* imageView.setImageBitmap();
        try {
         //   Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            ImageView imageView = new ImageView(this);
         //   imageView.setImageBitmap(bitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }*/



    }
}
