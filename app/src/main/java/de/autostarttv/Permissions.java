package de.autostarttv;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

/** The "Display over other apps" permission, needed on Android 10+ to start apps after boot. */
final class Permissions {

    private Permissions() {
    }

    static boolean isNeeded() {
        return Build.VERSION.SDK_INT >= 23;
    }

    static boolean isMissing(Context context) {
        return Build.VERSION.SDK_INT >= 23 && !android.provider.Settings.canDrawOverlays(context);
    }

    /** Opens the system screen to grant it. Returns false if the device has no such screen. */
    @android.annotation.TargetApi(23)
    static boolean openSettings(Activity activity) {
        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + activity.getPackageName()));
        try {
            activity.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }
}
