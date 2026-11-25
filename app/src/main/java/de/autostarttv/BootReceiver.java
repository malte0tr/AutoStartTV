package de.autostarttv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

/** Starts the selected app after the device has booted. */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "AutoStartTV";

    /** Several boot actions may arrive; only launch once per boot. */
    private static long lastLaunchUptime = -1;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        Settings settings = new Settings(context);
        String packageName = settings.getPackage();
        if (!settings.isEnabled() || packageName == null) {
            return;
        }
        synchronized (BootReceiver.class) {
            if (lastLaunchUptime >= 0) {
                return;
            }
            lastLaunchUptime = SystemClock.uptimeMillis();
        }

        Context appContext = context.getApplicationContext();
        int delayMs = settings.getDelaySeconds() * 1000;
        PendingResult result = goAsync();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                if (!AppLauncher.launch(appContext, packageName)) {
                    Log.w(TAG, "Could not start " + packageName);
                }
            } finally {
                result.finish();
            }
        }, delayMs);
    }
}
