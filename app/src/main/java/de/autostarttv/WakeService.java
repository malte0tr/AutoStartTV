package de.autostarttv;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

/**
 * Runs quietly in the background and opens the chosen app whenever the screen turns on,
 * i.e. when a TV stick wakes from standby. A boot alone does not cover this, because
 * most TV sticks never shut down: "off" only means standby.
 *
 * The screen-on event can only be received by a running component, hence this
 * (minimal) foreground service.
 */
public class WakeService extends Service {

    private static final String TAG = "AutoStartTV";
    private static final String CHANNEL = "background";
    private static final int NOTIFICATION_ID = 1;
    /** Ignore screen-on events that follow each other closely. */
    private static final long MIN_INTERVAL_MS = 10_000;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable launch = this::launchApp;
    private long lastLaunch = -MIN_INTERVAL_MS;

    private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                onScreenOn();
            } else {
                // Screen went off again before the delay ran out: do not launch.
                handler.removeCallbacks(launch);
            }
        }
    };

    /** Starts or stops the service to match the current settings. */
    static void sync(Context context) {
        Settings settings = new Settings(context);
        Intent intent = new Intent(context, WakeService.class);
        try {
            if (settings.isReady() && settings.isWakeEnabled()) {
                if (Build.VERSION.SDK_INT >= 26) {
                    context.startForegroundService(intent);
                } else {
                    context.startService(intent);
                }
            } else {
                context.stopService(intent);
            }
        } catch (RuntimeException e) {
            // Starting from the background can be refused on some Android versions.
            Log.w(TAG, "Could not start the background service", e);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startInForeground();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Settings settings = new Settings(this);
        if (!settings.isReady() || !settings.isWakeEnabled()) {
            stopSelf();
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(launch);
        unregisterReceiver(screenReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void onScreenOn() {
        Settings settings = new Settings(this);
        if (!settings.isReady() || !settings.isWakeEnabled()) {
            return;
        }
        handler.removeCallbacks(launch);
        handler.postDelayed(launch, settings.getDelaySeconds() * 1000L);
    }

    private void launchApp() {
        long now = SystemClock.elapsedRealtime();
        if (now - lastLaunch < MIN_INTERVAL_MS) {
            return;
        }
        lastLaunch = now;
        String packageName = new Settings(this).getPackage();
        if (!AppLauncher.launch(this, packageName)) {
            Log.w(TAG, "Could not start " + packageName);
        }
    }

    @SuppressWarnings("deprecation")
    private void startInForeground() {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL,
                    getString(R.string.notification_channel), NotificationManager.IMPORTANCE_MIN);
            channel.setShowBadge(false);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            builder = new Notification.Builder(this, CHANNEL);
        } else {
            builder = new Notification.Builder(this).setPriority(Notification.PRIORITY_MIN);
        }
        Notification notification = builder
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_text))
                .setOngoing(true)
                .setShowWhen(false)
                .build();
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFICATION_ID, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }
}
