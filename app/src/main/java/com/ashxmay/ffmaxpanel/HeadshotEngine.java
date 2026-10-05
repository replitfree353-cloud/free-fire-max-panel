package com.ashxmay.ffmaxpanel;

import android.util.Log;

/**
 * Free Fire Max Headshot Engine
 * Core aim assist + ESP logic
 * Works with AccessibilityService for touch injection
 */
public class HeadshotEngine {

    private static final String TAG = "FFMaxHeadshot";

    private static boolean enabled = false;
    private static boolean esp = false;
    private static float fov = 80f;
    private static float smooth = 5f;

    // Simulated player positions (in real use these come from memory/ESP scan)
    public static class Player {
        public float x, y, z;
        public float health;
        public boolean isEnemy;
        public boolean isVisible;
        public float headX, headY; // screen coords of head bone

        public Player(float x, float y, float z, float health, boolean isEnemy) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.health = health;
            this.isEnemy = isEnemy;
            this.isVisible = true;
        }
    }

    public static void setEnabled(boolean val) {
        enabled = val;
        Log.d(TAG, "Headshot " + (val ? "ON" : "OFF"));
    }

    public static void setEsp(boolean val) {
        esp = val;
        Log.d(TAG, "ESP " + (val ? "ON" : "OFF"));
    }

    public static void setFov(float val) {
        fov = val;
    }

    public static void setSmooth(float val) {
        smooth = Math.max(1f, val);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isEsp() {
        return esp;
    }

    /**
     * Calculate aim delta toward nearest enemy head within FOV
     * Returns [dx, dy] to move touch/aim
     */
    public static float[] getAimDelta(float screenCenterX, float screenCenterY, Player[] players) {
        if (!enabled || players == null) return new float[]{0, 0};

        Player target = null;
        float bestDist = Float.MAX_VALUE;

        for (Player p : players) {
            if (!p.isEnemy || p.health <= 0) continue;

            float dx = p.headX - screenCenterX;
            float dy = p.headY - screenCenterY;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist < fov && dist < bestDist) {
                bestDist = dist;
                target = p;
            }
        }

        if (target == null) return new float[]{0, 0};

        float dx = (target.headX - screenCenterX) / smooth;
        float dy = (target.headY - screenCenterY) / smooth;

        return new float[]{dx, dy};
    }

    /**
     * Force headshot bone priority
     * In real implementation this writes to aim bone index or soft aim offset
     */
    public static void forceHeadBone() {
        if (!enabled) return;
        // In production: write to player controller aim bone = HEAD (usually index 0 or 6)
        Log.d(TAG, "Force Head Bone Active");
    }

    /**
     * Soft no-recoil compensation
     */
    public static float[] applyNoRecoil(float recoilX, float recoilY) {
        if (!enabled) return new float[]{recoilX, recoilY};
        // Counter vertical recoil
        return new float[]{recoilX * 0.3f, recoilY * 0.15f};
    }
}
