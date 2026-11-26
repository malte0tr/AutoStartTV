package de.autostarttv;

import android.content.Context;
import android.content.SharedPreferences;

/** Small wrapper around the stored settings. */
final class Settings {

    static final int[] DELAYS = {0, 2, 5, 10, 20, 30};

    private static final String FILE = "settings";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_PACKAGE = "package";
    private static final String KEY_DELAY = "delay";
    private static final String KEY_WAKE = "wake";

    private final Context context;
    private final SharedPreferences prefs;

    Settings(Context context) {
        this.context = context.getApplicationContext();
        prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    /**
     * Also open the app when the device wakes from standby. On by default on TVs
     * (TV sticks usually go to standby instead of shutting down), off on phones.
     */
    boolean isWakeEnabled() {
        return prefs.getBoolean(KEY_WAKE, isTv());
    }

    void setWakeEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_WAKE, enabled).apply();
    }

    /** True when autostart is on and an app is chosen. */
    boolean isReady() {
        return isEnabled() && getPackage() != null;
    }

    private boolean isTv() {
        android.content.pm.PackageManager pm = context.getPackageManager();
        return pm.hasSystemFeature("android.software.leanback")
                || pm.hasSystemFeature("amazon.hardware.fire_tv");
    }

    boolean isEnabled() {
        return prefs.getBoolean(KEY_ENABLED, false);
    }

    void setEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    String getPackage() {
        return prefs.getString(KEY_PACKAGE, null);
    }

    void setPackage(String packageName) {
        prefs.edit().putString(KEY_PACKAGE, packageName).apply();
    }

    int getDelaySeconds() {
        return prefs.getInt(KEY_DELAY, 5);
    }

    void setDelaySeconds(int seconds) {
        prefs.edit().putInt(KEY_DELAY, seconds).apply();
    }
}
