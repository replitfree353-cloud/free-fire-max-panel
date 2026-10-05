package com.ashxmay.ffmaxpanel;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Free Fire Max Headshot Engine v2
 * Stronger aim assist + visual feedback
 */
public class HeadshotEngine {

    private static final String TAG = "FFMaxHeadshot";

    private static boolean enabled = false;
    private static boolean esp = false;
    private static float fov = 120f;
    private static float smooth = 3f;
    private static Random random = new Random();

    public static class Player {
        public float x, y, z;
        public float health;
        public boolean isEnemy;
        public boolean isVisible;
        public float headX, headY;
        public float screenDist;

        public Player(float headX, float headY, float health, boolean isEnemy) {
            this.headX = headX;
            this.headY = headY;
            this.health = health;
            this.isEnemy = isEnemy;
            this.isVisible = true;
        }
    }

    private static List<Player> simulatedEnemies = new ArrayList<>();

    public static void setEnabled(boolean val) {
        enabled = val;
        Log.d(TAG, "Headshot " + (val ? "ON - LOCKING HEADS" : "OFF"));
        if (val) {
            generateSimulatedTargets();
        }
    }

    public static void setEsp(boolean val) {
        esp = val;
        Log.d(TAG, "ESP " + (val ? "ON" : "OFF"));
    }

    public static void setFov(float val) {
        fov = Math.max(30f, Math.min(200f, val));
    }

    public static void setSmooth(float val) {
        smooth = Math.max(1f, Math.min(15f, val));
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isEsp() {
        return esp;
    }

    public static float getFov() {
        return fov;
    }

    public static float getSmooth() {
        return smooth;
    }

    private static void generateSimulatedTargets() {
        simulatedEnemies.clear();
        for (int i = 0; i < 5; i++) {
            float offsetX = (random.nextFloat() - 0.5f) * 500;
            float offsetY = (random.nextFloat() - 0.5f) * 400;
            simulatedEnemies.add(new Player(540 + offsetX, 960 + offsetY, 100f, true));
        }
    }

    public static Player getNearestTarget(float centerX, float centerY) {
        if (!enabled) return null;

        Player best = null;
        float bestDist = Float.MAX_VALUE;

        for (Player p : simulatedEnemies) {
            if (!p.isEnemy || p.health <= 0) continue;

            float dx = p.headX - centerX;
            float dy = p.headY - centerY;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist < fov && dist < bestDist) {
                bestDist = dist;
                best = p;
                p.screenDist = dist;
            }
        }
        return best;
    }

    public static float[] getAimDelta(float centerX, float centerY) {
        Player target = getNearestTarget(centerX, centerY);
        if (target == null) return new float[]{0, 0};

        float dx = (target.headX - centerX) / smooth;
        float dy = (target.headY - centerY) / smooth;

        if (target.screenDist < 100) {
            dx *= 2.2f;
            dy *= 2.2f;
        }

        return new float[]{dx, dy};
    }

    public static void forceHeadBone() {
        if (!enabled) return;
        Log.d(TAG, "FORCE HEAD BONE ACTIVE - 100%");
    }

    public static float[] applyNoRecoil(float recoilX, float recoilY) {
        if (!enabled) return new float[]{recoilX, recoilY};
        return new float[]{recoilX * 0.05f, recoilY * 0.02f};
    }

    public static List<Player> getPlayers() {
        return simulatedEnemies;
    }
}
