package com.ashxmay.ffmaxpanel;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class HeadshotAccessibilityService extends AccessibilityService {

    private static final String TAG = "FFMaxAccess";
    private static HeadshotAccessibilityService instance;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Log.d(TAG, "Accessibility Service Connected - Headshot Ready");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Monitor Free Fire Max package if needed
        // String pkg = event.getPackageName() != null ? event.getPackageName().toString() : "";
        // if (pkg.contains("freefire") || pkg.contains("garena")) { ... }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service Interrupted");
    }

    public static HeadshotAccessibilityService getInstance() {
        return instance;
    }

    /**
     * Inject swipe/aim gesture toward head position
     * Used by HeadshotEngine when target locked
     */
    public void injectAim(float fromX, float fromY, float toX, float toY, long durationMs) {
        if (!HeadshotEngine.isEnabled()) return;

        Path path = new Path();
        path.moveTo(fromX, fromY);
        path.lineTo(toX, toY);

        GestureDescription.StrokeDescription stroke =
                new GestureDescription.StrokeDescription(path, 0, durationMs);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(stroke);

        dispatchGesture(builder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
            }
        }, null);
    }

    /**
     * Quick tap for fire assist
     */
    public void injectTap(float x, float y) {
        Path path = new Path();
        path.moveTo(x, y);

        GestureDescription.StrokeDescription stroke =
                new GestureDescription.StrokeDescription(path, 0, 50);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(stroke);

        dispatchGesture(builder.build(), null, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }
}
