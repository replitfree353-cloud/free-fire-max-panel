package com.ashxmay.ffmaxpanel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.core.app.NotificationCompat;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private View overlayView;
    private WindowManager.LayoutParams params;

    public static boolean headshotEnabled = false;
    public static boolean espEnabled = false;
    public static float fov = 80f;
    public static float smooth = 5f;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, buildNotification());

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_panel, null);

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 50;
        params.y = 200;

        setupPanel();
        makeDraggable();

        windowManager.addView(overlayView, params);
    }

    private void setupPanel() {
        Switch swHeadshot = overlayView.findViewById(R.id.swHeadshot);
        Switch swEsp = overlayView.findViewById(R.id.swEsp);
        SeekBar sbFov = overlayView.findViewById(R.id.sbFov);
        SeekBar sbSmooth = overlayView.findViewById(R.id.sbSmooth);
        TextView tvFov = overlayView.findViewById(R.id.tvFov);
        TextView tvSmooth = overlayView.findViewById(R.id.tvSmooth);
        Button btnClose = overlayView.findViewById(R.id.btnClose);

        swHeadshot.setOnCheckedChangeListener((btn, checked) -> {
            headshotEnabled = checked;
            HeadshotEngine.setEnabled(checked);
        });

        swEsp.setOnCheckedChangeListener((btn, checked) -> {
            espEnabled = checked;
            HeadshotEngine.setEsp(checked);
        });

        sbFov.setProgress(80);
        sbFov.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                fov = progress;
                tvFov.setText("FOV: " + progress);
                HeadshotEngine.setFov(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        sbSmooth.setProgress(5);
        sbSmooth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                smooth = progress;
                tvSmooth.setText("Smooth: " + progress);
                HeadshotEngine.setSmooth(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnClose.setOnClickListener(v -> stopSelf());
    }

    private void makeDraggable() {
        overlayView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(overlayView, params);
                        return true;
                }
                return false;
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "ff_panel",
                    "FF Max Panel",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Headshot Panel Running");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, "ff_panel")
                .setContentTitle("FF Max Headshot Panel")
                .setContentText("Panel Active - Headshot Ready")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null) {
            windowManager.removeView(overlayView);
        }
        HeadshotEngine.setEnabled(false);
    }
}
