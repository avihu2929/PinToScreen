package com.avihu.pintoscreen;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class FloatingWindow extends Service {

    private WindowManager wm;
    private LinearLayout ll;
    Uri uri;
    Button quitbtn;
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
        quitbtn = (Button) view.findViewById(R.id.btnquit);



        Button extractbtn = (Button) view.findViewById(R.id.btnextract);

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
        Handler handler = new Handler();

        ll.setOnTouchListener(new View.OnTouchListener() {

            WindowManager.LayoutParams updatedParameters = params;
            double x;
            double y;
            double pressedX;
            double pressedY;
            long mDeBounce = 0;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        handler.removeCallbacksAndMessages(null);
                        x = updatedParameters.x;
                        y = updatedParameters.y;

                        pressedX = motionEvent.getRawX();
                        pressedY = motionEvent.getRawY();

                        break;

                    case MotionEvent.ACTION_MOVE:
                        updatedParameters.x = (int) (x + (motionEvent.getRawX() - pressedX));
                        updatedParameters.y = (int) (y + (motionEvent.getRawY() - pressedY));

                        wm.updateViewLayout(ll, updatedParameters);
                    case MotionEvent.ACTION_UP:

                        if ( Math.abs(mDeBounce - motionEvent.getEventTime()) < 250) {
                            //Ignore if it's been less then 250ms since
                            //the item was last clicked
                            return false;
                        }

                        int intCurrentY = Math.round(motionEvent.getY());
                        int intCurrentX = Math.round(motionEvent.getX());
                        int intStartY = motionEvent.getHistorySize() > 0 ? Math.round(motionEvent.getHistoricalY(0)) : intCurrentY;
                        int intStartX = motionEvent.getHistorySize() > 0 ? Math.round(motionEvent.getHistoricalX(0)) : intCurrentX;

                        if((Math.abs(intCurrentX - intStartX) < 3) && (Math.abs(intCurrentY - intStartY) < 3)){
                            quitbtn.setVisibility(View.VISIBLE);
                            extractbtn.setVisibility(View.VISIBLE);

                            handler.postDelayed(new Runnable() {
                                public void run() {

                                    quitbtn.setVisibility(View.INVISIBLE);
                                    extractbtn.setVisibility(View.INVISIBLE);
                                }
                            }, 2500);
                            break;

                        }

                        break;

                    default:
                        break;
                }


                return false;
            }
        });


        extractbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runTextRecognition(bd);
            }
        });
        quitbtn.setOnClickListener(new View.OnClickListener() {
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
    private void runTextRecognition(BitmapDrawable bd) {
        InputImage image = InputImage.fromBitmap(bd.getBitmap(), 0);
        TextRecognizer recognizer = TextRecognition.getClient();


        // mTextButton.setEnabled(false);
        recognizer.process(image)
                .addOnSuccessListener(
                        new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text texts) {
                                // mTextButton.setEnabled(true);
                                processTextRecognitionResult(texts);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                //     mTextButton.setEnabled(true);
                                e.printStackTrace();
                            }
                        });
    }

    private void processTextRecognitionResult(Text texts) {
        List<Text.TextBlock> blocks = texts.getTextBlocks();
        Toast.makeText(getBaseContext(),texts.getText(),Toast.LENGTH_LONG).show();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", texts.getText());
        clipboard.setPrimaryClip(clip);

        wm.removeView(ll);
        stopSelf();
        System.exit(0);
       /* if (blocks.size() == 0) {
            showToast("No text found");
            return;
        }
        mGraphicOverlay.clear();
        for (int i = 0; i < blocks.size(); i++) {
            List<Text.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<Text.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    Graphic textGraphic = new TextGraphic(mGraphicOverlay, elements.get(k));
                    mGraphicOverlay.add(textGraphic);

                }
            }
        }*/
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
