package de.autostarttv;

import android.content.Context;
import android.content.SharedPreferences;

/** Small wrapper around the stored settings. */
final class Settings {

    static final int[] DELAYS = {0, 5, 10, 20, 30};

    private static final String FILE = "settings";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_PACKAGE = "package";
    private static final String KEY_DELAY = "delay";

    private final SharedPreferences prefs;

    Settings(Context context) {
        prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
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
