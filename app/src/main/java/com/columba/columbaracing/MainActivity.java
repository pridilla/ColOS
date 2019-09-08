package com.columba.columbaracing;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity{
    Context mContext;
    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "A");
    //NotificationCompat.Builder mBuilder2 = new NotificationCompat.Builder(this, "B");
    ColOSClient colosclient;
    final ImageView[] imageViews = new ImageView[15];
    final TextView[] textViews = new TextView[10];
    int degSpeed = 0;
    int degSteer = 0;
    int degTilt = 0;
    double bat = 100;
    //int bat2 = 180;
    //float svetlo = 1;
    RotateAnimation[] rotations = new RotateAnimation[15];
    //AlphaAnimation animation1;
    //ImageView[][][] parkIV = new ImageView[2][2][4];
    //float[][][] parkOpac = new float[2][2][4];
    //AlphaAnimation[][][] parkanim = new AlphaAnimation[2][2][4];
    //byte[][] oldParkIn = new byte[2][4];
    int mod = Profiles.M;
    boolean conn = false;
    Randomizer randomizer;
    int height_switch = -1;
    int draco_profile;
    int beams_mode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.Theme_AppCompat_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        imageViews[LocalView.AXLE] = findViewById(R.id.but_draco);
        imageViews[LocalView.STEER_RECTANGLE] = findViewById(R.id.steer_pointer_rectangle);
        imageViews[LocalView.STEER_NEEDLE] = findViewById(R.id.steer_pointer);
        imageViews[LocalView.SPEED_NEEDLE] = findViewById(R.id.speed_pointer);
        imageViews[LocalView.LIGHT] = findViewById(R.id.lights);
        imageViews[LocalView.TILT] = findViewById(R.id.degree_pointer);
        //ProgressBar progressBar = findViewById(R.id.circular_progress_bar);
        textViews[LocalView.TILT] = findViewById(R.id.rotate_text);
        imageViews[LocalView.BATTERY] = findViewById(R.id.battery);
        imageViews[LocalView.HEIGHT] = findViewById(R.id.but_clara);
        imageViews[LocalView.MOD] = findViewById(R.id.but_mods);
        imageViews[LocalView.SETTINGS] = findViewById(R.id.but_settings);

        final ImageView obstL = findViewById(R.id.obstL);
        final ImageView obstR = findViewById(R.id.obstR);

        /*
        parkIV[0][0][0] = findViewById(R.id.p1);
        parkIV[0][0][1] = findViewById(R.id.p3);
        parkIV[0][0][2] = findViewById(R.id.p5);
        parkIV[0][0][3] = findViewById(R.id.p7);

        parkIV[0][1][0] = findViewById(R.id.p2);
        parkIV[0][1][1] = findViewById(R.id.p4);
        parkIV[0][1][2] = findViewById(R.id.p6);
        parkIV[0][1][3] = findViewById(R.id.p8);

        parkIV[1][0][0] = findViewById(R.id.p01);
        parkIV[1][0][1] = findViewById(R.id.p03);
        parkIV[1][0][2] = findViewById(R.id.p05);
        parkIV[1][0][3] = findViewById(R.id.p07);

        parkIV[1][1][0] = findViewById(R.id.p02);
        parkIV[1][1][1] = findViewById(R.id.p04);
        parkIV[1][1][2] = findViewById(R.id.p06);
        parkIV[1][1][3] = findViewById(R.id.p08);

        for (int i = 0 ; i < 2; i ++)
        {
            for (int j = 0; j < 2; j++)
            {
                for (int k = 0; k < 4; k++)
                {
                    oldParkIn[j][k] = 0;
                    parkOpac[i][j][k] = 0f;
                }
            }
        }
        */

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Affogato-Regular.ttf");
        textViews[LocalView.TILT].setTypeface(typeface);

        //final Button btn = findViewById(R.id.button);

        /*getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );*/

        imageViews[LocalView.SETTINGS].setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View arg0) {

                //final Dialog dialog = new Dialog(mContext, R.style.mytheme);
                final Dialog dialog = new Dialog(mContext);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.setContentView(R.layout.layout_popup_settings);

                //finalDialogBlur(dialog);
                final Button dialogButton = dialog.findViewById(R.id.btnConnect);

                if(conn) dialogButton.setText("DISCONNECT");
                else dialogButton.setText("CONNECT");

                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView IPtext = dialog.findViewById(R.id.IPtext);
                        TextView porttext = dialog.findViewById(R.id.PortText);

                        String IP = IPtext.getText().toString();
                        int port = Integer.parseInt(porttext.getText().toString());
                        dialog.dismiss();

                        Log.d("UTI","Dialogove okno zatvorené");

                        try {
                            if (conn)
                            {
                                colosclient.stopThreads();
                                conn = false;
                                return;
                            }

                            colosclient = new ColOSClient(IP, port);
                            if(colosclient.connected()) conn = true;
                            imageViews[LocalView.AXLE].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    final Dialog dialog2 = new Dialog(mContext);
                                    Objects.requireNonNull(dialog2.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                    dialog2.setContentView(R.layout.layout_draco);
                                    Window window2 = dialog2.getWindow();
                                    window2.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                                    window2.setGravity(Gravity.CENTER);
                                    //finalDialogBlur(dialog2);
                                    dialog2.show();
                                    view.setClipToOutline(true);

                                    final ImageView draco_m = dialog2.findViewById(R.id.draco_mirror);
                                    final ImageView draco_o = dialog2.findViewById(R.id.draco_off);
                                    final ImageView draco_p = dialog2.findViewById(R.id.draco_parallel);
                                    final ImageView draco_Slider = dialog2.findViewById(R.id.draco_slider);
                                    changeParentView(draco_Slider, draco_profile);

                                    draco_m.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            draco_profile = R.id.draco_mirror;
                                            colosclient.setSteerMode(SteerMode.Normal);
                                            changeParentView(draco_Slider, R.id.draco_mirror);
                                            imageViews[LocalView.AXLE].setImageResource(R.drawable.wheels3);
                                            dialog2.dismiss();
                                        }
                                    });

                                    draco_o.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            draco_profile = R.id.draco_off;
                                            colosclient.setSteerMode(SteerMode.LockRear);
                                            changeParentView(draco_Slider, R.id.draco_off);
                                            imageViews[LocalView.AXLE].setImageResource(R.drawable.wheels1);
                                            dialog2.dismiss();
                                        }
                                    });

                                    draco_p.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            draco_profile = R.id.draco_parallel;
                                            colosclient.setSteerMode(SteerMode.Crab);
                                            changeParentView(draco_Slider, R.id.draco_parallel);
                                            imageViews[LocalView.AXLE].setImageResource(R.drawable.wheels2);
                                            dialog2.dismiss();
                                        }
                                    });
                                }
                            });

                            imageViews[LocalView.MOD].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final Dialog dialog2 = new Dialog(mContext);
                                    Objects.requireNonNull(dialog2.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                    dialog2.setContentView(R.layout.layout_mod);
                                    //finalDialogBlur(dialog2);
                                    dialog2.show();
                                    v.setClipToOutline(true);

                                    final TextView ivModM = dialog2.findViewById(R.id.profileM);
                                    final TextView ivModS = dialog2.findViewById(R.id.profileS);
                                    final TextView ivModP = dialog2.findViewById(R.id.profileP);
                                    final TextView ivModR = dialog2.findViewById(R.id.profileR);
                                    final TextView ivModH = dialog2.findViewById(R.id.profileH);
                                    final ImageView ivModSlider = dialog2.findViewById(R.id.profile_slider);
                                    changeParentView(ivModSlider,mod);

                                    ivModM.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mod = R.id.profileM;
                                            obstR.setImageAlpha(0);
                                            obstL.setImageAlpha(0);
                                            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                            changeParentView(ivModSlider,R.id.profileM);
                                            imageViews[LocalView.MOD].setImageResource(R.drawable.dashboardm);
                                            dialog2.dismiss();
                                        }
                                    });
                                    ivModS.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mod = R.id.profileS;
                                            obstR.setImageAlpha(0);
                                            obstL.setImageAlpha(0);
                                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                            changeParentView(ivModSlider,R.id.profileS);
                                            imageViews[LocalView.MOD].setImageResource(R.drawable.dashboards);
                                            dialog2.dismiss();
                                        }
                                    });
                                    ivModP.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mod = R.id.profileP;
                                            obstR.setImageAlpha(20);
                                            obstL.setImageAlpha(20);
                                            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                            imageViews[LocalView.MOD].setImageResource(R.drawable.dashboardp);
                                            changeParentView(ivModSlider,R.id.profileP);
                                            dialog2.dismiss();
                                        }
                                    });
                                    ivModR.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mod = R.id.profileR;
                                            obstR.setImageAlpha(0);
                                            obstL.setImageAlpha(0);
                                            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                            imageViews[LocalView.MOD].setImageResource(R.drawable.dashboardr);
                                            changeParentView(ivModSlider,R.id.profileR);
                                            dialog2.dismiss();
                                        }
                                    });

                                    ivModH.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mod = R.id.profileH;
                                            obstR.setImageAlpha(0);
                                            obstL.setImageAlpha(0);
                                            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                            imageViews[LocalView.MOD].setImageResource(R.drawable.dashboardh);
                                            changeParentView(ivModSlider,R.id.profileH);
                                            dialog2.dismiss();
                                        }
                                    });
                                }
                            });


                            Thread th = new Thread(new Runnable() {
                                public void run() {
                                    while (colosclient.connected()) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                applyReceivedData(textViews, imageViews);

                                            }
                                        });
                                        try {
                                            Thread.sleep(200);
                                            if (mod == R.id.profileR) Thread.sleep(900);
                                        }
                                        catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                            th.start();

                            imageViews[LocalView.HEIGHT].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v1) {
                                    final Dialog dialog2 = new Dialog(mContext);

                                    Objects.requireNonNull(dialog2.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                    dialog2.setContentView(R.layout.layout_height);
                                    //finalDialogBlur(dialog2);
                                    dialog2.show();
                                    v1.setClipToOutline(true);

                                    final byte[] coef = {50};

                                    ImageView hore = dialog2.findViewById(R.id.goUp);
                                    ImageView dole = dialog2.findViewById(R.id.goDown);
                                    final ImageView switch1 = dialog2.findViewById(R.id.height_switch);

                                    switch1.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (height_switch == -1)
                                            {
                                                coef[0] = 3;
                                                switch1.setImageResource(R.drawable.popon);

                                            }
                                            else
                                            {
                                                coef[0]=7;
                                                switch1.setImageResource(R.drawable.popoff);
                                            }
                                            height_switch*=(-1);
                                        }
                                    });


                                    hore.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            colosclient.setHeight(( byte)1, coef[0]);
                                        }
                                    });

                                    dole.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            colosclient.setHeight(( byte)-1, coef[0]);
                                        }
                                    });
                                }
                            });




                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
                dialog.show();
                arg0.setClipToOutline(true);
            }
        });


    }

    /*
    void changeTag(ImageView v, int d1, int d2)
    {
        if(v.getTag().equals(d1))
        {
            v.setTag(d2);
            v.setImageResource(d2);
        }
        else
        {
            v.setTag(d1);
            v.setImageResource(d1);
        }
    }

    void changeTag(ImageView v, int d1, int d2, int d3)
    {
        if(v.getTag().equals(d1))
        {
            v.setTag(d2);
            v.setImageResource(d2);
        }
        else if(v.getTag().equals(d2))
        {
            v.setTag(d3);
            v.setImageResource(d3);
        }
        else
        {
            v.setTag(d1);
            v.setImageResource(d1);
        }
    }
    */

    @SuppressLint("SetTextI18n")
    private void applyReceivedData(final TextView[] textViews, final ImageView[] imageViews) {
        //Log.d("SERVER", Arrays.toString(toRead));

        imageViews[LocalView.AXLE] = findViewById(R.id.but_draco);
        imageViews[LocalView.STEER_RECTANGLE] = findViewById(R.id.steer_pointer_rectangle);
        imageViews[LocalView.STEER_NEEDLE] = findViewById(R.id.steer_pointer);
        imageViews[LocalView.SPEED_NEEDLE] = findViewById(R.id.speed_pointer);
        imageViews[LocalView.LIGHT] = findViewById(R.id.lights);
        imageViews[LocalView.TILT] = findViewById(R.id.degree_pointer);
        ProgressBar progressBar = findViewById(R.id.circular_progress_bar);
        textViews[LocalView.TILT] = findViewById(R.id.rotate_text);
        imageViews[LocalView.BATTERY] = findViewById(R.id.battery);
        imageViews[LocalView.HEIGHT] = findViewById(R.id.but_clara);
        imageViews[LocalView.MOD] = findViewById(R.id.but_mods);
        imageViews[LocalView.SETTINGS] = findViewById(R.id.but_settings);

        final ImageView obstL = findViewById(R.id.obstL);
        final ImageView obstR = findViewById(R.id.obstR);

        short degrees = colosclient.getDegrees();
        boolean belowTreshold = colosclient.getBelowThreshold();
        int direction = colosclient.getSteer();
        int speed = colosclient.getThrottle();
        double battery = colosclient.getBatteryPercentage();

        if (mod == R.id.profileR)
        {
            randomizer = new Randomizer();
            randomizer.setData();
            degrees = randomizer.getDegrees();
            belowTreshold = randomizer.getBelowTreshold();
            direction = randomizer.getSteer();
            speed = randomizer.getThrottle();
            battery = randomizer.getBatteryPercentage()*100d;
            Log.d("UTI", "Randomized" + degrees);
        }

        int padR;
        int padL;
        float startMargin;

        if (direction > 0)
        {
            padL = 113;
            padR = 113 - direction*113/128;
        }
        else
        {
            padR = 113;
            padL = 113 + direction*113/128;
        }

        final double newLeftMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 319.5f + direction*113/128, getResources().getDisplayMetrics());

        ValueAnimator animatorR = ValueAnimator.ofInt(imageViews[LocalView.STEER_RECTANGLE].getPaddingRight(), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, padR, getResources().getDisplayMetrics()));
        animatorR.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator){
                imageViews[LocalView.STEER_RECTANGLE].setPadding(imageViews[LocalView.STEER_RECTANGLE].getPaddingLeft(), 0, (Integer) valueAnimator.getAnimatedValue(), 0);
            }
        });
        animatorR.setDuration(180);

        ValueAnimator animatorL = ValueAnimator.ofInt(imageViews[LocalView.STEER_RECTANGLE].getPaddingLeft(), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, padL, getResources().getDisplayMetrics()));
        animatorL.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator){
                imageViews[LocalView.STEER_RECTANGLE].setPadding( (Integer) valueAnimator.getAnimatedValue(), 0, imageViews[LocalView.STEER_RECTANGLE].getPaddingRight(), 0);
            }
        });
        animatorL.setDuration(180);

        animatorR.start();
        animatorL.start();

        if (belowTreshold) imageViews[LocalView.LIGHT].setImageResource(R.drawable.lightson);
        else imageViews[LocalView.LIGHT].setImageResource(R.drawable.lightsoff);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Affogato-Regular.ttf");

        textViews[LocalView.TILT].setTypeface(typeface);
        textViews[LocalView.TILT].setText(" " + String.valueOf(Math.abs(degrees)) + "°");
        degTilt = rorate_Clockwise(LocalView.TILT, degTilt, degrees);



        int angle;

        angle = (Math.abs(speed) * 180) / 100;

        ProgressBarAnimation progressBarAnimation = new ProgressBarAnimation(progressBar, progressBar.getProgress(), angle);
        progressBarAnimation.setDuration(180);
        progressBar.startAnimation(progressBarAnimation);
        degSpeed = rorate_Clockwise(LocalView.SPEED_NEEDLE, degSpeed, angle);


        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progressBar.setProgress(angle, true);
        }
        else progressBar.setProgress(degSpeed);*/

        if (battery>80) imageViews[LocalView.BATTERY].setImageResource(R.drawable.battery100);
        else if (battery>60) imageViews[LocalView.BATTERY].setImageResource(R.drawable.battery80);
        else if (battery>40) imageViews[LocalView.BATTERY].setImageResource(R.drawable.battery40);
        else if (battery>20) imageViews[LocalView.BATTERY].setImageResource(R.drawable.battery60);
        else if (battery>5) imageViews[LocalView.BATTERY].setImageResource(R.drawable.battery20);
        else imageViews[LocalView.BATTERY].setImageResource(R.drawable.battery0);

        int tresh = 140;

        if (mod == Profiles.P)
        {
            short[] parking = colosclient.getSensorData();
            if (parking[0] > tresh || parking[1] > tresh) obstL.setImageAlpha(200);
            else obstL.setImageAlpha(20);
            if (parking[2] > tresh || parking[3] > tresh) obstR.setImageAlpha(200);
            else obstR.setImageAlpha(20);
        }

        //if(mod == Profiles.P) setParking(parkIV, decodeBytes.getSensorData());

        /*if (bat > battery + 5 && mod != Profiles.S)
        {
            //mBuilder.setSmallIcon(R.drawable.ic_logo_plne);
            mBuilder.setContentTitle("Stav batérie auta");
            mBuilder.setAutoCancel(true);
            mBuilder.setContentText("Batéria má " + (int) (battery*100 )+ "%");
            mBuilder.setPriority(Notification.PRIORITY_HIGH);
            mBuilder.setTimeoutAfter(2000);

            bat = battery;

            Intent resultIntent = new Intent(this, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);

            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert mNotificationManager != null;
            mNotificationManager.notify(1, mBuilder.build());
        }*/

    }

    public int rorate_Clockwise(final int v, int a0, int a) {
        final View view = imageViews[v];
        if (a0 != a) {
            if (rotations[v] == null  || (rotations[v] != null && rotations[v].hasEnded())) rotations[v] = new RotateAnimation(a0, a, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,0.5f); else return a0;
            Log.d("ANIMATION", "Started from " + a0 + " to " +a);
            rotations[v].setFillAfter(true);
            rotations[v].setFillEnabled(true);
            rotations[v].setDuration(190);
            if (mod == Profiles.R) rotations[v].setDuration(500);
            rotations[v].setRepeatCount(0);
            rotations[v].setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    view.setLayerType(View.LAYER_TYPE_NONE, null);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.setAnimation(rotations[v]);
        }
        return a;
    }
    /*
    public float change_Opacity(final int v, float a0, float a) {
        final View view = imageViews[v];
        if (a0 != a) {
            /*
            if (animation1 == null  || (animation1 != null && animation1.hasEnded())) animation1 = new AlphaAnimation(a0, a);
            Log.d("OPACITY", "Started from " + a0 + " to " +a);
            //view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            animation1.setFillAfter(true);
            animation1.setFillEnabled(true);
            animation1.setDuration(190);
            if (mod == Mods.R) animation1.setDuration(500);
            animation1.setRepeatCount(0);
            animation1.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    view.setLayerType(View.LAYER_TYPE_NONE, null);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.setAnimation(animation1);

            view.setAlpha(a);
        }
        return a;
    }*/

    /*
    public float change_Opacity(ImageView v, float a0, float a, int m, int n, int o) {
        final View view = v;
        if (a0 != a) {
            if (parkanim[m][n][o] == null  || (parkanim[m][n][o] != null && parkanim[m][n][o].hasEnded())) parkanim[m][n][o] = new AlphaAnimation(a0, a);
            Log.d("ANIMATION", "Started from " + a0 + " to " +a);
            view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            parkanim[m][n][o].setFillAfter(true);
            parkanim[m][n][o].setFillEnabled(true);
            parkanim[m][n][o].setDuration(190);
            parkanim[m][n][o].setRepeatCount(0);
            parkanim[m][n][o].setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    view.setLayerType(View.LAYER_TYPE_NONE, null);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.setAnimation(parkanim[m][n][o]);
        }
        v.setAlpha(a);
        return a;
    } */
    /*
    public void setParking(ImageView[][][] iv, byte[][] input)
    {
        boolean upoz = false;
        for (int i = 0; i < 2; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                if (input[i][j] > 100)
                {
                    parkOpac[i][0][j] = change_Opacity(iv[i][0][j], parkOpac[i][0][j], 0.5f, i, 0, j);
                    parkOpac[i][1][j] = change_Opacity(iv[i][1][j], parkOpac[i][1][j], 0f, i, 1, j);
                    upoz = true;
                }
                else if(input[i][j] > 50)
                {
                    parkOpac[i][0][j] = change_Opacity(iv[i][0][j], parkOpac[i][0][j], 0f,i, 0, j);
                    parkOpac[i][1][j] = change_Opacity(iv[i][1][j], parkOpac[i][1][j], 0.5f, i, 1, j);
                }
                else
                {
                    parkOpac[i][0][j] = change_Opacity(iv[i][0][j], parkOpac[i][0][j], 0, i, 0, j);
                    parkOpac[i][1][j] = change_Opacity(iv[i][1][j], parkOpac[i][1][j], 0, i, 1, j);
                }
            }
        }
        if (upoz)
        {
            mBuilder2.setSmallIcon(R.drawable.ic_logo_plne);
            mBuilder2.setTicker("Varovanie");
            mBuilder2.setContentTitle("Varovanie");
            mBuilder2.setContentText("Pri aute je prekážka!");
            mBuilder2.setTimeoutAfter(2000);
            mBuilder2.setPriority(Notification.PRIORITY_HIGH);

            long[] v = {50,200};
            mBuilder2.setVibrate(v);

            Intent resultIntent = new Intent(this, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);

            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder2.setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert mNotificationManager != null;
            mNotificationManager.notify(2, mBuilder2.build());
        }
    }
    */

    void changeParentView(View v, int id)
    {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_START, id);
        params.addRule(RelativeLayout.ALIGN_END, id);
    }

    private static Bitmap takeScreenShot(Activity activity)
    {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay().getHeight();

        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height  - statusBarHeight);
        view.destroyDrawingCache();
        return b;
    }

    public Bitmap fastblur(Bitmap sentBitmap, int radius) {
        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }

    void finalDialogBlur(Dialog d)
    {
        Bitmap map=takeScreenShot(MainActivity.this);

        Bitmap fast=fastblur(map, 2);
        final Drawable draw=new BitmapDrawable(getResources(),fast);
        d.getWindow().setBackgroundDrawable(draw);
    }

    public class ProgressBarAnimation extends Animation{
        private ProgressBar progressBar;
        private float from;
        private float  to;

        public ProgressBarAnimation(ProgressBar progressBar, float from, float to) {
            super();
            this.progressBar = progressBar;
            this.from = from;
            this.to = to;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            float value = from + (to - from) * interpolatedTime;
            progressBar.setProgress((int) value);
        }

    }
}
